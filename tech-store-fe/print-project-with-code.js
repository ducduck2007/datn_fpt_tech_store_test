// node print-project-with-code.js > project-tree-with-code.txt

import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

/**
 * ===============================
 * ESM __dirname FIX
 * ===============================
 */
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/**
 * ===============================
 * CONFIG
 * ===============================
 */
const ROOT_DIR = path.resolve(__dirname, "src");

const INCLUDE_FILES = [
  "api/auth.api.js",
  "api/http.js",

  //   "assets/vue.svg",

  "components/AuthLayout.vue",

  "pages/customer/CustomerDashboard.vue",
  "pages/customer/CustomerHome.vue",
  "pages/customer/CustomerLogin.vue",
  "pages/customer/CustomerRegister.vue",

  "pages/system/CustomerManager.vue",
  "pages/system/SystemDashboard.vue",
  "pages/system/SystemLogin.vue",
  "pages/system/SystemShell.vue",

  "router/index.js",
  "stores/auth.js",

  "ui/confirm.js",
  "ui/toast.js",

  "App.vue",
  "main.js",
  "style.css",
];

/**
 * ===============================
 * RUN
 * ===============================
 */
for (const relativePath of INCLUDE_FILES) {
  const absolutePath = path.join(ROOT_DIR, relativePath);

  if (!fs.existsSync(absolutePath)) {
    console.error(`❌ FILE NOT FOUND: ${relativePath}`);
    continue;
  }

  const content = fs.readFileSync(absolutePath, "utf8");

  console.log("==================================================");
  console.log(`FILE: src/${relativePath}`);
  console.log("--------------------------------------------------");
  console.log(content.trim());
  console.log("\n");
}
