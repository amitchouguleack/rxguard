#!/usr/bin/env bash
# Copyright (c) 2026 Amit Chougule. All rights reserved.
# One-time setup after the dev container is created.
set -euo pipefail

echo "==> C++ toolchain (clang with libFuzzer, sanitizers, clang-tidy/format, gdb, ninja)"
sudo apt-get update -y
sudo apt-get install -y --no-install-recommends \
  cmake ninja-build clang clang-tidy clang-format lld gdb libclang-rt-18-dev

echo "==> Emscripten ${EMSDK_VERSION} (cached in the rxguard-emsdk volume)"
sudo mkdir -p "${EMSDK}" && sudo chown -R "$(id -u):$(id -g)" "${EMSDK}"
if [ ! -f "${EMSDK}/.installed-${EMSDK_VERSION}" ]; then
  if [ ! -d "${EMSDK}/.git" ]; then
    git clone --depth 1 https://github.com/emscripten-core/emsdk.git "${EMSDK}"
  fi
  "${EMSDK}/emsdk" install "${EMSDK_VERSION}"
  "${EMSDK}/emsdk" activate "${EMSDK_VERSION}"
  touch "${EMSDK}/.installed-${EMSDK_VERSION}"
fi
grep -q 'emsdk_env.sh' ~/.bashrc || echo "source ${EMSDK}/emsdk_env.sh >/dev/null 2>&1" >> ~/.bashrc

echo "==> Web dependencies"
(cd web && npm ci)

echo "==> phi-shield virtualenv"
(cd services/phi-shield && python3.12 -m venv .venv && .venv/bin/pip install -q -e ".[dev]")

echo "Done. Open a new terminal so emcc is on PATH."
