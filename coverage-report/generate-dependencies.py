#!/usr/bin/env python3
"""Regenerates the <dependencies> block in coverage-report/pom.xml from every
coverage-bearing leaf module actually reachable in the Maven reactor (sakai/
plus the sibling repos it pulls in via the `all` profile's `<module>../...`
entries).

Walks the real <module> reference graph starting at the reactor root pom,
rather than globbing the filesystem for pom.xml files — a plain filesystem
walk would also pick up orphaned poms nobody's parent actually references
(e.g. a module commented out of its parent's <modules> list, or a leftover
directory like edu-services/sections-service/sections-impl/integration-test
that isn't listed in its parent's <modules> at all). Those are real files on
disk but not part of any Maven build, so a dependency on them fails to
resolve since Maven never places them in the reactor session.

Cwd-independent; run it from anywhere:
    python3 generate-dependencies.py

Re-run after adding, removing, or renaming a module.
"""
import sys
import xml.etree.ElementTree as ET  # parses pom.xml into Element trees
from pathlib import Path


SAKAI_REACTOR_ROOT = Path(__file__).resolve().parent.parent

# Must stay in sync with <profile><id>all</id> in the reactor root pom.xml (../pom.xml), which
# is where coverage-report itself is registered as a module. verify_active_profiles_exist()
ACTIVE_PROFILE_IDS = {"all"}


START_MARKER = "<!-- GENERATED-DEPENDENCIES-START -->"
END_MARKER = "<!-- GENERATED-DEPENDENCIES-END -->"


TYPE_FOR_PACKAGING = {
    "jar": None,
    "sakai-component": "sakai-component",
    "war": "war",
}



POM_PATH = Path(__file__).resolve().parent / "pom.xml"
ROOT_POM_PATH = SAKAI_REACTOR_ROOT / "pom.xml"


def local(tag: str) -> str:
    # ElementTree tag names come back namespaced as "{uri}tagname" when a pom
    # declares an xmlns. Plain string split on "}" strips that prefix so tag
    # comparisons below ("modules", "profile", ...) work regardless of
    # namespace.
    return tag.split("}")[-1] if "}" in tag else tag


def find_direct(elem, name):
    # elem is an ElementTree Element; iterating it yields only its direct
    # children (not all descendants)
    for child in elem:
        if local(child.tag) == name:
            return child
    return None


def has_java_source(module_dir: Path) -> bool:
    for java_file in module_dir.rglob("*.java"):
        if "target" not in java_file.parts:
            return True
    return False


def resolve_packaging(root_elem):
    packaging_el = find_direct(root_elem, "packaging")
    return packaging_el.text.strip() if packaging_el is not None else "jar"  # Maven default


def is_coverage_leaf(packaging, module_dir):
    """True if a leaf-shaped pom should count toward coverage: not itself pom-packaged (an
    aggregator with no children of its own), and containing actual Java source to cover."""
    return packaging != "pom" and has_java_source(module_dir)


def resolve_coordinates(root_elem, pom_path):
    """(group, artifact, version) for root_elem's Maven coordinates, falling back to its
    <parent> block for any of group/version it omits — the same inheritance Maven itself
    applies. Returns None (after printing a WARNING naming pom_path) if a coordinate can't be
    resolved, or if version is left as an unresolved ${...} property."""
    group_el = find_direct(root_elem, "groupId")
    artifact_el = find_direct(root_elem, "artifactId")
    version_el = find_direct(root_elem, "version")
    parent_el = find_direct(root_elem, "parent")

    group = group_el.text.strip() if group_el is not None else None
    version = version_el.text.strip() if version_el is not None else None
    if parent_el is not None:
        if group is None:
            pg = find_direct(parent_el, "groupId")
            group = pg.text.strip() if pg is not None else None
        if version is None:
            pv = find_direct(parent_el, "version")
            version = pv.text.strip() if pv is not None else None

    artifact = artifact_el.text.strip() if artifact_el is not None else None
    if not (group and artifact and version):
        print(f"WARNING: skipping {pom_path} — could not resolve groupId/artifactId/version",
              file=sys.stderr)
        return None
    if version.startswith("${"):
        print(f"WARNING: skipping {pom_path} — unresolved version property {version}",
              file=sys.stderr)
        return None
    return group, artifact, version


def module_refs(root_elem):
    """<module> paths this pom declares, from its default <modules> plus any
    profile whose id is in ACTIVE_PROFILE_IDS."""
    refs = []
    modules_el = find_direct(root_elem, "modules")
    if modules_el is not None:

        refs += [m.text.strip() for m in modules_el if local(m.tag) == "module" and m.text]

    profiles_el = find_direct(root_elem, "profiles")
    if profiles_el is not None:
        for profile_el in profiles_el:  # each profile_el is a <profile> Element
            if local(profile_el.tag) != "profile":
                continue
            id_el = find_direct(profile_el, "id")
            if id_el is None or id_el.text.strip() not in ACTIVE_PROFILE_IDS:
                continue
            profile_modules_el = find_direct(profile_el, "modules")
            if profile_modules_el is not None:
                refs += [m.text.strip() for m in profile_modules_el
                         if local(m.tag) == "module" and m.text]
    return refs


def verify_active_profiles_exist(root_pom_elem):
    """Fail loudly if ACTIVE_PROFILE_IDS names a profile id that no longer exists in the root
    pom's <profiles> block."""
    profiles_el = find_direct(root_pom_elem, "profiles")
    declared_ids = set()
    if profiles_el is not None:
        for profile_el in profiles_el:
            if local(profile_el.tag) != "profile":
                continue
            id_el = find_direct(profile_el, "id")
            if id_el is not None and id_el.text:
                declared_ids.add(id_el.text.strip())

    missing_ids = ACTIVE_PROFILE_IDS - declared_ids
    if missing_ids:
        print(f"ERROR: ACTIVE_PROFILE_IDS references profile id(s) {sorted(missing_ids)} that "
              f"do not exist in {ROOT_POM_PATH}'s <profiles> block (found: {sorted(declared_ids)}). "
              f"Update ACTIVE_PROFILE_IDS in generate-dependencies.py to match.", file=sys.stderr)
        sys.exit(1)


def discover_modules():
    """Walk the real <module> reference graph starting at ROOT_POM_PATH,
    rather than globbing the filesystem."""
    verify_active_profiles_exist(ET.parse(ROOT_POM_PATH).getroot())

    modules = []
    visited = set()
    queue = [ROOT_POM_PATH]  # list used as a stack (LIFO) via .append()/.pop()

    while queue:
        pom_path = queue.pop()
        pom_path = pom_path.resolve()
        if pom_path in visited:
            continue
        visited.add(pom_path)

        if not pom_path.is_file():
            print(f"WARNING: referenced module pom {pom_path} does not exist, skipping",
                  file=sys.stderr)
            continue

        tree = ET.parse(pom_path)
        r = tree.getroot()
        module_dir = pom_path.parent

        child_refs = module_refs(r)
        if child_refs:
            for ref in child_refs:
                queue.append((module_dir / ref / "pom.xml"))
            continue

        packaging = resolve_packaging(r)
        if not is_coverage_leaf(packaging, module_dir):
            continue

        coordinates = resolve_coordinates(r, pom_path)
        if coordinates is None:
            continue
        group, artifact, version = coordinates


        modules.append((group, artifact, version, packaging, str(pom_path)))
    return modules


def render_dependency(group, artifact, version, packaging):
    type_el = TYPE_FOR_PACKAGING.get(packaging, None)
    lines = [
        "        <dependency>",
        f"            <groupId>{group}</groupId>",
        f"            <artifactId>{artifact}</artifactId>",
        f"            <version>{version}</version>",
    ]
    if type_el:
        lines.append(f"            <type>{type_el}</type>")
    lines.append("        </dependency>")
    return "\n".join(lines)


def main():
    modules = discover_modules()
    # list.sort() with a key= lambda: sorts tuples by (group, artifact) so the
    # generated pom.xml is diff-friendly between runs (stable ordering).
    modules.sort(key=lambda m: (m[0], m[1]))


    unknown_packagings = {m[3] for m in modules} - set(TYPE_FOR_PACKAGING)
    if unknown_packagings:
        print(f"ERROR: unhandled packaging types found: {unknown_packagings}. "
              f"Add them to TYPE_FOR_PACKAGING after confirming the correct <type> "
              f"(see Task 2 of the implementation plan for the method).", file=sys.stderr)
        sys.exit(1)  # abort before writing anything — better to fail loudly than emit a wrong pom.xml


    body = "\n".join(render_dependency(g, a, v, p) for g, a, v, p, _ in modules)
    new_block = (
        f"{START_MARKER}\n"
        "    <!-- Populated by coverage-report/generate-dependencies.py. Do not hand-edit the entries\n"
        "         between these markers; they will be overwritten the next time the script runs. -->\n"
        "    <dependencies>\n"
        f"{body}\n"
        "    </dependencies>\n"
        f"    {END_MARKER}"
    )

    pom_text = POM_PATH.read_text()
    start_idx = pom_text.index(START_MARKER)
    end_idx = pom_text.index(END_MARKER) + len(END_MARKER)

    pom_text = pom_text[:start_idx] + new_block + pom_text[end_idx:]
    POM_PATH.write_text(pom_text)

    print(f"Wrote {len(modules)} dependencies to {POM_PATH}")


if __name__ == "__main__":
    main()
