import { ElMessage } from "element-plus";

function mapType(type) {
  const t = (type || "").toLowerCase();
  if (t === "success") return "success";
  if (t === "warning") return "warning";
  if (t === "danger" || t === "error") return "error";
  if (t === "info") return "info";
  return "info";
}

export function toast(message, type = "success", opts = {}) {
  ElMessage({
    message: String(message ?? ""),
    type: mapType(type),
    duration: typeof opts?.delay === "number" ? opts.delay : 2200,
    showClose: true,
  });
}
