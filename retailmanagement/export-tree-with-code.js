// export-tree-with-code.js
// Chạy:
//   node export-tree-with-code.js
//   node export-tree-with-code.js --module=6
//   node export-tree-with-code.js --module=pricing --out=pricing.txt

const fs = require("fs");
const path = require("path");
const readline = require("readline");

const ROOT_DIR = "src/main/java/com/retailmanagement";
const DEFAULT_OUTPUT = "project-tree-with-code.txt";
const IGNORE_DIRS = new Set([
  "node_modules",
  ".git",
  "target",
  ".idea",
  ".vscode",
]);

const BASE_PACKAGE = "com.retailmanagement.";

/**
 * Preset theo đúng structure dự án bạn gửi.
 * Mỗi module = seedFiles (đường dẫn tương đối dưới ROOT_DIR).
 * Script sẽ:
 * 1) Include seedFiles
 * 2) Expand dependency bằng cách parse `import com.retailmanagement...`
 */
const MODULE_PRESETS = {
  all: {
    key: "1",
    label: "All",
    mode: "all",
    seedFiles: [],
  },

  catalog: {
    key: "2",
    label: "Catalog / Product",
    mode: "seed",
    seedFiles: [
      "controller/ProductController.java",
      "controller/CategoryController.java",
      "service/ProductService.java",
      "repository/ProductRepository.java",
      "repository/ProductVariantRepository.java",
      "repository/CategoryRepository.java",
      "repository/ProductCategoryRepository.java",

      // DTO core catalog
      "dto/request/ProductRequest.java",
      "dto/response/ProductResponse.java",
      "dto/response/ApiResponse.java", // ✅ add (khuyến nghị)

      // Entities core catalog
      "entity/Product.java",
      "entity/ProductVariant.java",
      "entity/Category.java",
      "entity/ProductCategory.java",
      "entity/ProductCategoryId.java",
      "entity/Image.java",
      "repository/ImageRepository.java",
      "entity/Tag.java",
      "entity/ProductTag.java",
      "entity/ProductTagId.java",

      // infra hay dùng chung
      "exception/ErrorResponse.java", // ✅ add (khuyến nghị)
      "exception/GlobalExceptionHandler.java", // ✅ add (khuyến nghị)
    ],
  },

  sales: {
    key: "3",
    label: "Sales / Order & Returns",
    mode: "seed",
    seedFiles: [
      "controller/OrderController.java",
      "service/OrderService.java",
      "service/OrderQueryService.java",
      "repository/OrderRepository.java",
      "repository/OrderItemRepository.java",

      "dto/request/CreateOrderRequest.java",
      "dto/request/CreateOrderItemRequest.java",
      "dto/request/UpdateOrderRequest.java", // ✅ add

      "dto/response/CreateOrderResponse.java",
      "dto/response/OrderListResponse.java",
      "dto/response/OrderDetailResponse.java", // ✅ add
      "dto/response/ApiResponse.java", // ✅ add (khuyến nghị)

      "entity/Order.java",
      "entity/OrderItem.java",
      "entity/Payment.java",
      "entity/Return.java",
      "entity/StockTransaction.java",
      "repository/StockTransactionRepository.java",

      // infra
      "exception/ErrorResponse.java",
      "exception/GlobalExceptionHandler.java",
    ],
  },

  customer: {
    key: "4",
    label: "Customer & Loyalty",
    mode: "seed",
    seedFiles: [
      "controller/CusController.java",
      "service/CustomerService.java",
      "dto/request/CustomerRequest.java",
      "dto/response/CustomerResponse.java",
      "dto/response/ApiResponse.java", // ✅ add (khuyến nghị)

      "entity/Customer.java",
      "entity/CustomerType.java",
      "entity/Customergroup.java",
      "entity/VipTier.java",

      // infra hay dùng
      "exception/ErrorResponse.java",
      "exception/GlobalExceptionHandler.java",
      "util/IpUtil.java", // ✅ add (khuyến nghị)
    ],
  },

  user: {
    key: "5",
    label: "User & Role / Audit",
    mode: "seed",
    seedFiles: [
      "controller/AuthController.java",
      "controller/UserController.java",
      "service/AuthService.java",
      "service/UserService.java",

      "security/CustomUserDetails.java",
      "security/CustomUserDetailsService.java",
      "security/JwtAuthenticationFilter.java",
      "security/JwtService.java",

      "config/SecurityConfig.java",
      "config/JwtProperties.java",

      "dto/request/LoginRequest.java",
      "dto/request/RegisterRequest.java",
      "dto/request/CreateUserRequest.java",
      "dto/request/UpdateUserRequest.java",

      "dto/response/AuthResponse.java",
      "dto/response/UserResponse.java",
      "dto/response/ApiResponse.java", // ✅ add (khuyến nghị)

      "entity/User.java",
      "entity/Role.java",
      "repository/UserRepository.java",

      // ✅ AUDIT package (đúng theo label module)
      "audit/Audit.java",
      "audit/AuditAction.java",
      "audit/AuditLog.java",
      "audit/AuditLogAspect.java",
      "audit/AuditLogRepository.java",
      "audit/AuditLogService.java",

      // infra
      "util/SecurityUtil.java",
      "util/IpUtil.java",
      "exception/ErrorResponse.java",
      "exception/GlobalExceptionHandler.java",
      "config/CorsConfig.java", // ✅ add (khuyến nghị)
      "config/WebConfig.java", // ✅ add (khuyến nghị)
      "config/ModelMapperConfig.java", // ✅ add (khuyến nghị)
    ],
  },

  pricing: {
    key: "6",
    label: "Pricing & Promotion",
    mode: "seed",
    seedFiles: [
      // controllers
      "controller/PriceController.java",
      "controller/PromotionController.java",

      // services
      "service/PricingService.java",
      "service/PromotionService.java",

      // repositories
      "repository/PriceHistoryRepository.java",
      "repository/PromotionRepository.java",

      // dto
      "dto/request/UpsertPriceRequest.java",
      "dto/request/PromotionRequest.java",
      "dto/response/VariantPriceResponse.java",
      "dto/response/ApiResponse.java", // ✅ add (khuyến nghị)

      // entities
      "entity/PriceHistory.java",
      "entity/Promotion.java",

      // pricing/promotion đụng tới product
      "entity/ProductVariant.java",
      "entity/Product.java",
      "repository/ProductVariantRepository.java",
      "repository/ProductRepository.java",

      // infra
      "exception/ErrorResponse.java",
      "exception/GlobalExceptionHandler.java",
    ],
  },

  // ✅ NEW MODULE: settings (bạn đang thiếu trong presets)
  settings: {
    key: "7",
    label: "Settings / System Config",
    mode: "seed",
    seedFiles: [
      "controller/SettingController.java",
      "service/SettingService.java",
      "repository/SystemSettingRepository.java",
      "entity/SystemSetting.java",
      "dto/request/SetDefaultCurrencyRequest.java",
      "dto/response/ApiResponse.java",

      // infra
      "exception/ErrorResponse.java",
      "exception/GlobalExceptionHandler.java",
      "config/WebConfig.java",
    ],
  },
};

// -------------------- utils --------------------
function exists(p) {
  try {
    fs.accessSync(p);
    return true;
  } catch {
    return false;
  }
}
function normalizeAbs(p) {
  return path.normalize(path.resolve(p));
}
function isIgnoredDir(name) {
  return IGNORE_DIRS.has(name);
}
function isJavaFile(p) {
  return p.endsWith(".java");
}
function readTextSafe(p) {
  try {
    return fs.readFileSync(p, "utf8");
  } catch {
    return "";
  }
}

function listAllFilesRec(dir) {
  let out = [];
  const items = fs.readdirSync(dir, { withFileTypes: true });
  for (const it of items) {
    const full = path.join(dir, it.name);
    if (it.isDirectory()) {
      if (isIgnoredDir(it.name)) continue;
      out = out.concat(listAllFilesRec(full));
    } else {
      out.push(full);
    }
  }
  return out;
}

function parseInternalImports(javaContent) {
  const imports = new Set();
  const re = /^\s*import\s+(?:static\s+)?(com\.retailmanagement\.[\w.]*\*?);/gm;
  let m;
  while ((m = re.exec(javaContent)) !== null) imports.add(m[1]);
  return [...imports];
}

function packageToJavaPath(importName) {
  if (!importName.startsWith(BASE_PACKAGE)) return null;
  const rel = importName.replace(BASE_PACKAGE, "").replace(/\./g, path.sep);
  return path.join(ROOT_DIR, rel) + ".java";
}

function wildcardToDir(importName) {
  if (!importName.endsWith(".*")) return null;
  if (!importName.startsWith(BASE_PACKAGE)) return null;
  const rel = importName
    .replace(BASE_PACKAGE, "")
    .replace(/\.\*$/, "")
    .replace(/\./g, path.sep);
  return path.join(ROOT_DIR, rel);
}

function isAncestorDir(dirAbs, fileAbs) {
  const d = dirAbs.endsWith(path.sep) ? dirAbs : dirAbs + path.sep;
  return fileAbs.startsWith(d);
}

// -------------------- build included set --------------------
function buildIncludedSet(preset) {
  const rootAbs = normalizeAbs(ROOT_DIR);
  const allFilesAbs = listAllFilesRec(ROOT_DIR).map(normalizeAbs);
  const allJavaAbs = allFilesAbs.filter(isJavaFile);

  const included = new Set();

  if (preset.mode === "all") {
    for (const f of allFilesAbs) included.add(f);
    return { included, rootAbs, allFilesAbs, allJavaAbs };
  }

  // seed files
  const queue = [];
  for (const rel of preset.seedFiles) {
    const abs = normalizeAbs(path.join(ROOT_DIR, rel));
    if (exists(abs)) {
      included.add(abs);
      if (isJavaFile(abs)) queue.push(abs);
    }
  }

  // dependency expand via import
  while (queue.length) {
    const fileAbs = queue.pop();
    const content = readTextSafe(fileAbs);
    if (!content) continue;

    const imports = parseInternalImports(content);

    for (const imp of imports) {
      if (imp.endsWith(".*")) {
        const dir = wildcardToDir(imp);
        if (!dir) continue;
        const dirAbs = normalizeAbs(dir);
        if (!exists(dirAbs)) continue;

        for (const jf of allJavaAbs) {
          if (isAncestorDir(dirAbs, jf) && !included.has(jf)) {
            included.add(jf);
            queue.push(jf);
          }
        }
      } else {
        const javaPath = packageToJavaPath(imp);
        if (!javaPath) continue;
        const depAbs = normalizeAbs(javaPath);
        if (exists(depAbs) && !included.has(depAbs)) {
          included.add(depAbs);
          queue.push(depAbs);
        }
      }
    }
  }

  return { included, rootAbs, allFilesAbs, allJavaAbs };
}

// -------------------- tree writer (only included) --------------------
function writeTreeWithCode(rootAbs, includedSet) {
  function dirHasIncluded(dirAbs) {
    const d = dirAbs.endsWith(path.sep) ? dirAbs : dirAbs + path.sep;
    for (const f of includedSet) if (f.startsWith(d)) return true;
    return false;
  }

  function walk(dirAbs, prefix = "") {
    let items = fs
      .readdirSync(dirAbs, { withFileTypes: true })
      .filter((it) => {
        const fullAbs = normalizeAbs(path.join(dirAbs, it.name));
        if (it.isDirectory()) {
          if (isIgnoredDir(it.name)) return false;
          return dirHasIncluded(fullAbs);
        }
        return includedSet.has(fullAbs);
      })
      .sort((a, b) => a.name.localeCompare(b.name));

    let result = "";
    items.forEach((it, idx) => {
      const isLast = idx === items.length - 1;
      const pointer = isLast ? "└── " : "├── ";
      const fullAbs = normalizeAbs(path.join(dirAbs, it.name));
      result += `${prefix}${pointer}${it.name}\n`;

      const childPrefix = prefix + (isLast ? "    " : "│   ");

      if (it.isDirectory()) {
        result += walk(fullAbs, childPrefix);
      } else {
        const content = readTextSafe(fullAbs);
        result += `${childPrefix}---- CODE ----\n`;
        result += content
          .split("\n")
          .map((line) => `${childPrefix}  ${line}`)
          .join("\n");
        result += `\n${childPrefix}---- END ----\n`;
      }
    });
    return result;
  }

  return walk(rootAbs);
}

// -------------------- CLI/menu --------------------
function parseArgs(argv) {
  const args = {};
  for (const a of argv.slice(2)) {
    if (a.startsWith("--module=")) args.module = a.split("=")[1];
    if (a.startsWith("--out=")) args.out = a.split("=")[1];
    if (a === "--help" || a === "-h") args.help = true;
  }
  return args;
}

function resolveModule(input) {
  if (!input) return null;
  const normalized = String(input).trim().toLowerCase();

  // by number
  for (const k of Object.keys(MODULE_PRESETS)) {
    if (MODULE_PRESETS[k].key === normalized) return k;
  }
  // by key
  if (MODULE_PRESETS[normalized]) return normalized;

  return null;
}

function printHelp() {
  console.log(`
Usage:
  node export-tree-with-code.js
  node export-tree-with-code.js --module=6
  node export-tree-with-code.js --module=pricing --out=pricing.txt

Modules:
  1. All
  2. Catalog / Product
  3. Sales / Order & Returns
  4. Customer & Loyalty
  5. User & Role / Audit
  6. Pricing & Promotion
`);
}

async function askMenu() {
  console.log("Chọn module để export:");
  const entries = Object.entries(MODULE_PRESETS).sort(
    (a, b) => Number(a[1].key) - Number(b[1].key)
  );
  for (const [, v] of entries) console.log(`${v.key}. ${v.label}`);

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });
  const answer = await new Promise((res) =>
    rl.question("Nhập lựa chọn (1-6): ", res)
  );
  rl.close();
  return answer;
}

// -------------------- main --------------------
(async function main() {
  const args = parseArgs(process.argv);
  if (args.help) {
    printHelp();
    process.exit(0);
  }

  if (!exists(ROOT_DIR)) {
    console.error(`ROOT_DIR not found: ${ROOT_DIR}`);
    process.exit(1);
  }

  let moduleKey = resolveModule(args.module);
  if (!moduleKey) {
    const ans = await askMenu();
    moduleKey = resolveModule(ans) || "all";
  }

  const preset = MODULE_PRESETS[moduleKey] || MODULE_PRESETS.all;

  const outFile = args.out
    ? args.out
    : moduleKey === "all"
    ? DEFAULT_OUTPUT
    : `project-tree-with-code.${moduleKey}.txt`;

  const { included, rootAbs } = buildIncludedSet(preset);
  const tree = writeTreeWithCode(rootAbs, included);

  fs.writeFileSync(outFile, tree, "utf8");
  console.log(`Exported module: ${preset.label}`);
  console.log(`Files included: ${included.size}`);
  console.log("Output:", outFile);
})();
