import "../sakai-onboarding-widget.js";
import { onboardingI18nUrl, onboardingI18n, dashboardWidgetI18nUrl, dashboardWidgetI18n } from "./i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-onboarding-widget tests", () => {

  window.top.portal = { locale: "en_GB" };

  const listResponse = {
    courses: [
      { id: "onboarding-it-security", siteId: "onboarding-it-security", title: "IT Security Fundamentals", status: "COMPLETED", percentComplete: 100, dueDate: "2026-06-15" },
    ],
  };

  const siteResponse = {
    course: { id: "onboarding-benefits", siteId: "onboarding-benefits", title: "Benefits Overview", status: "NOT_STARTED", percentComplete: 0, dueDate: "2026-08-01" },
  };

  beforeEach(() => {

    fetchMock
      .get(onboardingI18nUrl, onboardingI18n, { overwriteRoutes: true })
      .get(dashboardWidgetI18nUrl, dashboardWidgetI18n, { overwriteRoutes: true })
      .get("/api/users/me/onboarding-courses", listResponse, { overwriteRoutes: true })
      .get("/api/sites/onboarding-benefits/onboarding-courses", siteResponse, { overwriteRoutes: true })
      .get("*", 500, { overwriteRoutes: true });
  });

  afterEach(() => fetchMock.restore());

  it("renders the course list when no site-id is set", async () => {

    const el = await fixture(html`<sakai-onboarding-widget user-id="adrian"></sakai-onboarding-widget>`);

    await waitUntil(() => el.shadowRoot.querySelector(".onboarding-course-list-item"));

    expect(el.shadowRoot.querySelectorAll(".onboarding-course-list-item").length).to.equal(1);
    expect(el.shadowRoot.textContent).to.include("IT Security Fundamentals");
  });

  it("renders the single course when site-id is set", async () => {

    const el = await fixture(html`<sakai-onboarding-widget user-id="adrian" site-id="onboarding-benefits"></sakai-onboarding-widget>`);

    await waitUntil(() => el.shadowRoot.querySelector(".onboarding-course"));
    await elementUpdated(el);

    expect(el.shadowRoot.textContent).to.include("Benefits Overview");
    expect(el.shadowRoot.querySelectorAll(".onboarding-course-list-item").length).to.equal(0);
  });
});
