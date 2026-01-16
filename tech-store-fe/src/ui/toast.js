// src/ui/toast.js
import { Toast } from "bootstrap";

// Lightweight Bootstrap Toast helper (no window.bootstrap dependency)

const ID = "__ts_toast_container__";

function ensureContainer() {
  let el = document.getElementById(ID);
  if (el) return el;

  el = document.createElement("div");
  el.id = ID;
  el.className = "toast-container position-fixed top-0 end-0 p-3";
  el.style.zIndex = "1080";
  document.body.appendChild(el);
  return el;
}

function typeToBg(type) {
  switch ((type || "").toLowerCase()) {
    case "success":
      return "text-bg-success";
    case "warning":
      return "text-bg-warning";
    case "danger":
    case "error":
      return "text-bg-danger";
    case "info":
      return "text-bg-info";
    default:
      return "text-bg-dark";
  }
}

export function toast(message, type = "success", opts = {}) {
  const container = ensureContainer();
  const el = document.createElement("div");

  const bg = typeToBg(type);
  el.className = `toast align-items-center ${bg} border-0`;
  el.setAttribute("role", "alert");
  el.setAttribute("aria-live", "assertive");
  el.setAttribute("aria-atomic", "true");

  el.innerHTML = `
    <div class="d-flex">
      <div class="toast-body">${String(message || "")}</div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
  `;

  container.appendChild(el);

  const t = Toast.getOrCreateInstance(el, {
    delay: typeof opts.delay === "number" ? opts.delay : 2200,
    autohide: opts.autohide !== false,
  });

  t.show();

  el.addEventListener("hidden.bs.toast", () => {
    try {
      el.remove();
    } catch {}
  });
}
