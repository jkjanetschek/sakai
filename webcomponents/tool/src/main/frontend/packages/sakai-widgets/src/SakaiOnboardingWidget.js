import { css, html, nothing } from "lit";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiOnboardingWidget extends SakaiDashboardWidget {

  static properties = {

    _courses: { state: true },
    _course: { state: true },
  };

  constructor() {

    super();

    this.widgetId = "onboarding-courses";
    this.loadTranslations(this.widgetId);
  }

  connectedCallback() {

    super.connectedCallback();

    this.loadData();
  }

  loadData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/onboarding-courses` : "/api/users/me/onboarding-courses";

    fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get onboarding courses from ${url}`);
      })
      .then(data => {

        if (this.siteId) {
          this._course = data.course || null;
        } else {
          this._courses = data.courses || [];
        }
      })
      .catch(error => console.error(error));
  }

  shouldUpdate() {
    return super.shouldUpdate() && this._i18n && (this.siteId ? this._course !== undefined : this._courses !== undefined);
  }

  renderProgress(course) {

    return html`
      <div class="onboarding-course-title">${course.title}</div>
      <div class="onboarding-course-status status-${course.status.toLowerCase()}">${this._i18n[`status_${course.status.toLowerCase()}`]}</div>
      <div class="onboarding-course-progress">
        <progress max="100" value="${course.percentComplete}"></progress>
        <span>${course.percentComplete}%</span>
      </div>
      ${course.dueDate ? html`
        <div class="onboarding-course-due">${this._i18n.due_date}: ${course.dueDate}</div>
      ` : nothing}
    `;
  }

  content() {

    if (this.siteId) {
      return this._course
        ? html`<div class="onboarding-course">${this.renderProgress(this._course)}</div>`
        : html`<div>${this._i18n.no_course}</div>`;
    }

    return this._courses.length
      ? html`
        <ul class="onboarding-course-list">
          ${this._courses.map(course => html`
            <li class="onboarding-course-list-item">${this.renderProgress(course)}</li>
          `)}
        </ul>
      `
      : html`<div>${this._i18n.no_courses}</div>`;
  }

  static styles = [
    SakaiDashboardWidget.styles,
    css`
      .onboarding-course-list {
        list-style: none;
        margin: 0;
        padding: 0;
      }
      .onboarding-course-list-item {
        padding-bottom: 12px;
        margin-bottom: 12px;
        border-bottom: solid 1px var(--sakai-dashboard-widget-border-color, rgb(224,224,224));
      }
      .onboarding-course-list-item:last-child {
        border-bottom: none;
      }
      .onboarding-course-title {
        font-weight: bold;
        margin-bottom: 4px;
      }
      .onboarding-course-status {
        font-size: 12px;
        margin-bottom: 4px;
      }
      .onboarding-course-progress {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .onboarding-course-due {
        font-size: 12px;
        margin-top: 4px;
      }
    `,
  ];
}
