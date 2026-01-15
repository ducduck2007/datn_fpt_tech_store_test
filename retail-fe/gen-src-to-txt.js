import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const ROOT_DIR = path.join(__dirname, "src");
const OUTPUT_FILE = path.join(__dirname, "src-structure-and-code.txt");

let output = "";

function walkDir(dir, level = 0) {
  const indent = "  ".repeat(level);
  const items = fs.readdirSync(dir);

  for (const item of items) {
    const fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);

    if (stat.isDirectory()) {
      output += `${indent}${item}/\n`;
      walkDir(fullPath, level + 1);
    } else {
      output += `${indent}${item}\n`;
      output += `${indent}-------------------- SOURCE CODE --------------------\n`;

      try {
        const content = fs.readFileSync(fullPath, "utf8");
        output += content
          .split("\n")
          .map((line) => indent + line)
          .join("\n");
      } catch {
        output += `${indent}[Không đọc được file]\n`;
      }

      output += `\n${indent}----------------------------------------------------\n\n`;
    }
  }
}

output += "PROJECT STRUCTURE + SOURCE CODE\n";
output += "=============================================\n\n";
output += "src/\n";

walkDir(ROOT_DIR);

fs.writeFileSync(OUTPUT_FILE, output, "utf8");

console.log("Đã tạo file:", OUTPUT_FILE);
