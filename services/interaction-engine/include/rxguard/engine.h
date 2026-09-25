// Copyright (c) 2026 Amit Chougule. All rights reserved.
// Public C ABI of the RxGuard interaction engine. Called from Java (FFM) and WebAssembly.
#ifndef RXGUARD_ENGINE_H
#define RXGUARD_ENGINE_H

#ifdef __cplusplus
extern "C" {
#endif

#if defined(__GNUC__) || defined(__clang__)
#define RXG_API __attribute__((visibility("default")))
#else
#define RXG_API
#endif

/// Returns the engine version as a static, NUL-terminated string. Never NULL.
RXG_API const char* rxg_version(void);

#ifdef __cplusplus
}
#endif

#endif  // RXGUARD_ENGINE_H
