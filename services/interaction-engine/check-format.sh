#!/usr/bin/env bash
# Copyright (c) 2026 Amit Chougule. All rights reserved.
# Fails if any C++ source is unformatted or is missing the copyright header.
set -euo pipefail
cd "$(dirname "$0")"
CLANG_FORMAT="${CLANG_FORMAT:-clang-format}"
files=$(find include src tests -name '*.h' -o -name '*.cpp')
"${CLANG_FORMAT}" --dry-run --Werror ${files}
missing=0
for f in ${files}; do
  if ! head -1 "$f" | grep -q 'Copyright (c) [0-9]\{4\} Amit Chougule. All rights reserved.'; then
    echo "missing copyright header: $f"; missing=1
  fi
done
exit "${missing}"
