const fs = require("fs");
const path = require("path");

const ROOT_DIR = "src/main/java/com/retailmanagement";
const OUTPUT = "project-tree-with-code.txt";

const IGNORE = ["node_modules", ".git", "target", ".idea"];

function walk(dir, prefix = "") {
  let result = "";
  const items = fs.readdirSync(dir);

  items.forEach((item, index) => {
    if (IGNORE.includes(item)) return;

    const fullPath = path.join(dir, item);
    const isLast = index === items.length - 1;
    const pointer = isLast ? "└── " : "├── ";

    result += `${prefix}${pointer}${item}\n`;

    if (fs.statSync(fullPath).isDirectory()) {
      result += walk(fullPath, prefix + (isLast ? "    " : "│   "));
    } else {
      const content = fs.readFileSync(fullPath, "utf8");
      result += `${prefix}${isLast ? "    " : "│   "}---- CODE ----\n`;
      result += content
        .split("\n")
        .map(line => `${prefix}${isLast ? "    " : "│   "}  ${line}`)
        .join("\n");
      result += `\n${prefix}${isLast ? "    " : "│   "}---- END ----\n`;
    }
  });

  return result;
}

const output = walk(ROOT_DIR);
fs.writeFileSync(OUTPUT, output, "utf8");
console.log("Exported to", OUTPUT);

// lệnh chạy: node export-tree-with-code.js
