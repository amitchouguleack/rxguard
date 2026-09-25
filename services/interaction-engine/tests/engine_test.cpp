// Copyright (c) 2026 Amit Chougule. All rights reserved.
#include "rxguard/engine.h"

#include <gtest/gtest.h>

#include <string_view>

TEST(Engine, REQ_OPS_001_VersionIsReported) {
  const char* version = rxg_version();
  ASSERT_NE(version, nullptr);
  EXPECT_EQ(std::string_view(version), "0.0.1");
}
