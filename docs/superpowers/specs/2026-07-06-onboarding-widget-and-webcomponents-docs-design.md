# Web Components Documentation + Onboarding-Courses Example Widget

## Goal

Produce a single Claude Artifact (webpage) that teaches (a) web components/Lit fundamentals to developers new to the concept, (b) how Sakai's dashboard widget system is actually implemented, and (c) how to build a custom widget end to end — using a real, fully working "Onboarding Courses" widget as the worked example throughout.

## Background: how the dashboard widget system actually works

Verified against the current codebase (not assumed from prior descriptions):

- **Widget rendering is not dynamic.** `SakaiWidgetPanel.js` (`webcomponents/tool/src/main/frontend/packages/sakai-widgets/src/SakaiWidgetPanel.js`) has a hardcoded `switch` in `getWidget(r, index)` that renders one specific custom element per widget id. Adding a widget means adding a `case`.
- **`SakaiWidgets.js`'s catalog (`getIds()`/`getWidgets()`) is only consumed by `SakaiWidgetPicker.js`** (the "add a widget" UI), not by the panel itself. The panel's actual available-widget list comes from the server (see next point).
- **The real REST API lives in `webapi`, not in the `dashboard` module.** `webapi/src/main/java/org/sakaiproject/webapi/controllers/DashboardController.java` exposes:
  - `GET/PUT /api/users/{userId}/dashboard` (home dashboard: motd, widgets list, widgetLayout, etc.)
  - `GET/PUT /api/sites/{siteId}/dashboard` (course dashboard: same shape, plus site overview/programme/template)
  - In `@PostConstruct init()`, it reads `dashboard.home.widgets` and `dashboard.course.widgets` from `ServerConfigurationService`, falling back to hardcoded defaults if unset, and strips `tasks` if `portal.dashboard.tasks.enabled` is false.
- **Layout persistence** happens through the same GET/PUT dashboard endpoints, not a separate endpoint. User layout is stored as JSON under the `dashboard-config` preference key via `PreferencesService`. Site (course) layout is stored as JSON under the `dashboard-config` site-property key via `SiteService.save()`.
- **`dashboard/tool`'s `MainController.java`** only serves the two Thymeleaf shells (`home_dashboard.html`, `course_dashboard.html`) based on `siteService.isUserSite(siteId)` — it has no REST endpoints of its own.
- **Widgets fetch their own data independently.** `SakaiTasksWidget.js` renders `<sakai-tasks>`, which itself calls `/api/sites/{siteId}/tasks` (when `site-id` is set) or `/api/users/me/tasks` (when it isn't), backed by `webapi`'s `TasksController.java` and `kernel`'s `TaskService`. Other widgets (announcements, calendar, courses, grades, forums) follow the same wrap-an-existing-package pattern, each with its own `webapi` controller.
- **Course dashboard widgets receive both `site-id` and `user-id`.** Data is scoped to that site but still filtered to the current user (e.g. "my tasks in this course"), not a different user's data.
- Both dashboard shells nest `sakai-widget-panel` one level down inside `SakaiHomeDashboard`/`SakaiCourseDashboard`, which fetch `/api/.../dashboard`, and pass `.widgetIds=${data.widgets}` and `.layout=${data.widgetLayout}` into the panel.

## Deliverables

1. **Artifact webpage** (HTML, self-contained per Artifact constraints) covering:
   - **Section A — Web Components 101**: custom elements, Shadow DOM, reactive properties/lifecycle, why Lit exists, a minimal from-scratch Lit example. Written for developers with solid JS/CSS/HTML skills but no prior web-component/Lit exposure.
   - **Section B — Sakai's Widget Architecture**: class hierarchy diagram (`LitElement → SakaiShadowElement → SakaiDashboardWidget → concrete widget`), i18n loading (`loadProperties`), the catalog/switch/picker relationship, the `webapi` `DashboardController` + sakai.properties + layout-persistence flow, illustrated with a sequence diagram grounded in the Background section above.
   - **Section C — Build-Your-Own-Widget Guide**: numbered steps mirroring the actual PR below, with real file paths and real code snippets from the Onboarding widget, ending with the `npm run bundle` step.

2. **A fully working "Onboarding Courses" widget**, added for real:

   **Backend (`webapi` module)**
   - `OnboardingController.java` in `webapi/src/main/java/org/sakaiproject/webapi/controllers/`:
     - `GET /api/users/me/onboarding-courses` → full list for the current user (home dashboard view).
     - `GET /api/sites/{siteId}/onboarding-courses` → the single onboarding-course entry matching that site for the current user (course dashboard view); empty result if the site isn't an onboarding course.
   - `OnboardingCourse` response DTO: `id`, `title`, `siteId`, `status` (`NOT_STARTED|IN_PROGRESS|COMPLETED`), `percentComplete`, `dueDate`.
   - `OnboardingService`/`OnboardingServiceImpl`: returns deterministic (not random) sample data keyed by `userId` — 3–5 entries per user with varied statuses/percentages and plausible `siteId`-like identifiers (e.g. `onboarding-it-security`) so the per-site lookup path has something to match. This is an explicit seam for swapping in a real data source later; no persistence, no mock framework.

   **Frontend (`webcomponents` module)**
   - `packages/sakai-widgets/src/SakaiOnboardingWidget.js` extending `SakaiDashboardWidget`, plus `sakai-onboarding-widget.js` barrel export, matching `SakaiTasksWidget.js`'s structure.
   - `content()` fetches `/api/sites/${this.siteId}/onboarding-courses` when `site-id` is set (renders single-course progress + due date), else `/api/users/me/onboarding-courses` (renders the full list) — same branching pattern as `SakaiTasks.js`.
   - Register `{ id: "onboarding-courses", roles: [...], tag: "sakai-onboarding-widget" }` in `SakaiWidgets.js`; add the matching `case` in `SakaiWidgetPanel.js`'s `getWidget()`; add `onboarding-courses` to the `dashboard.home.widgets` and `dashboard.course.widgets` defaults; add an `onboarding-courses` i18n properties bundle with a `widget_title` key, following the existing widget convention.

## Testing

- Java unit test for `OnboardingController`/`OnboardingServiceImpl`, testing through the public REST-facing methods (no reflection into privates, per project convention).
- `web-test-runner` test for `SakaiOnboardingWidget.js` alongside the existing `sakai-widget-panel.test.js`, verifying it renders the list vs. single-course view based on `site-id` presence, with fetch mocked consistent with existing widget tests.
- No Playwright e2e test: this adds a new dashboard tile rather than changing an existing navigation/dialog/submission flow. This will be noted explicitly in the PR description per the project's UI-flow-change convention.

## Out of scope

- Any real onboarding-tracking data source (LMS completion data, gradebook-derived progress, etc.) — explicitly deferred; the service seam is designed to make that swap easy later.
- Changes to `SakaiWidgetPanel`'s hardcoded-switch architecture itself — out of scope for this work; documented as current-state in the Artifact, not redesigned.
