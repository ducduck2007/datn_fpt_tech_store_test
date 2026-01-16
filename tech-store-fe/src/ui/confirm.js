// src/ui/confirm.js
import { Modal } from "bootstrap";

// Promise-based Bootstrap confirm modal (no window.bootstrap dependency)

const ID = "__ts_confirm_modal__";

function ensureModal() {
  let el = document.getElementById(ID);
  if (el) return el;

  el = document.createElement("div");
  el.id = ID;
  el.className = "modal fade";
  el.tabIndex = -1;
  el.innerHTML = `
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Confirm</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="__msg">Are you sure?</div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary __cancel" data-bs-dismiss="modal">Cancel</button>
          <button type="button" class="btn btn-danger __ok">OK</button>
        </div>
      </div>
    </div>
  `;
  document.body.appendChild(el);
  return el;
}

export function confirmModal(
  message,
  title = "Confirm",
  okText = "OK",
  danger = true
) {
  const el = ensureModal();

  const titleEl = el.querySelector(".modal-title");
  const msgEl = el.querySelector(".__msg");
  const okBtn = el.querySelector(".__ok");

  titleEl.textContent = title;
  msgEl.textContent = message;

  okBtn.textContent = okText;
  okBtn.className = `btn ${danger ? "btn-danger" : "btn-primary"} __ok`;

  return new Promise((resolve) => {
    let modal;

    try {
      modal = Modal.getOrCreateInstance(el, {
        backdrop: "static",
        keyboard: true,
      });
      modal.show();
    } catch (e) {
      // Fallback if Bootstrap JS isn't available for any reason
      resolve(window.confirm(message));
      return;
    }

    let settled = false;

    const cleanup = () => {
      okBtn.removeEventListener("click", onOk);
      el.removeEventListener("hidden.bs.modal", onHidden);
    };

    const onOk = () => {
      if (settled) return;
      settled = true;
      cleanup();
      resolve(true);
      modal.hide();
    };

    const onHidden = () => {
      if (settled) return;
      settled = true;
      cleanup();
      resolve(false);
    };

    okBtn.addEventListener("click", onOk);
    el.addEventListener("hidden.bs.modal", onHidden);
  });
}
