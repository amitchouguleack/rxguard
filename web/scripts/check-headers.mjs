// Copyright (c) 2026 Amit Chougule. All rights reserved.
// Fails if a source file under src/ or scripts/ lacks the copyright header.
import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';

const HEADER = /Copyright \(c\) \d{4} Amit Chougule\. All rights reserved\./;
const EXTENSIONS = /\.(ts|tsx|js|mjs|css)$/;

function walk(dir) {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? walk(path) : [path];
  });
}

const missing = ['src', 'scripts']
  .flatMap(walk)
  .concat(['eslint.config.js', 'vite.config.ts'])
  .filter((f) => EXTENSIONS.test(f))
  .filter((f) => !HEADER.test(readFileSync(f, 'utf8').split('\n', 2).join('\n')));

if (missing.length > 0) {
  console.error(`Missing copyright header:\n  ${missing.join('\n  ')}`);
  process.exit(1);
}
