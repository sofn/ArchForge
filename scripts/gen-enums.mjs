#!/usr/bin/env node
// Generates TypeScript enum constants for Admin / Web from spec/enums.yaml (M3.3).
//
//   node scripts/gen-enums.mjs            write generated files
//   node scripts/gen-enums.mjs --check    exit 1 if generated files would change
//
// Expects the sibling-repos layout: ../ArchForgeAdmin and ../ArchForgeWeb.
//
// Install the single dependency first (from this directory):
//   npm ci

import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { parse } from "yaml";

const specRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const checkOnly = process.argv.includes("--check");

// Sibling repos live next to the main checkout; when running from a worktree
// (.worktrees/<name>) the siblings are three levels up instead of one.
function findSibling(repo) {
  const candidates = [
    resolve(specRoot, "..", repo),
    resolve(specRoot, "..", "..", "..", repo)
  ];
  for (const candidate of candidates) {
    if (existsSync(candidate)) return candidate;
  }
  // Missing siblings are expected in single-repo CI checkouts — skip quietly.
  return null;
}

const targets = [
  { repo: "ArchForgeAdmin", file: "src/types/enums.generated.ts" },
  { repo: "ArchForgeWeb", file: "apps/web/src/types/enums.generated.ts" }
];

const header =
  "// Generated from ArchForge/spec/enums.yaml — do not edit by hand.\n" +
  "/* eslint-disable @typescript-eslint/no-redeclare */\n";

function renderEnum(name, spec) {
  const values = spec.values ?? [];
  const lines = [];
  const entries = values.map((v) => `  ${v.name}: ${JSON.stringify(v.value)},`).join("\n");
  const labels = values.map((v) => `  ${v.value}: ${JSON.stringify(v.label ?? v.name)},`).join("\n");
  lines.push(`export const ${name} = {\n${entries}\n} as const;`);
  lines.push(
    `export type ${name} = (typeof ${name})[keyof typeof ${name}];`
  );
  lines.push(`export const ${name}Label: Record<number, string> = {\n${labels}\n};`);
  return lines.join("\n\n");
}

const doc = parse(readFileSync(resolve(specRoot, "spec/enums.yaml"), "utf8"));
const enums = doc.enums ?? {};
if (Object.keys(enums).length === 0) {
  console.error("no enums found in spec/enums.yaml");
  process.exit(1);
}

const body = Object.entries(enums)
  .map(([name, spec]) => renderEnum(name, spec))
  .join("\n\n");

const output = `${header}\n${body}\n`;

let changed = false;
for (const target of targets) {
  const siblingRoot = findSibling(target.repo);
  if (!siblingRoot) continue;
  const outPath = resolve(siblingRoot, target.file);
  try {
    const existing = readFileSync(outPath, "utf8");
    if (existing !== output) changed = true;
  } catch {
    changed = true;
  }
  if (!checkOnly) {
    writeFileSync(outPath, output);
    console.log(`wrote ${outPath}`);
  }
}

if (checkOnly && changed) {
  console.error("enums.generated.ts is out of sync with spec/enums.yaml");
  process.exit(1);
}
console.log(checkOnly ? "enum files in sync" : "done");
