# Copyright (c) 2026 Amit Chougule. All rights reserved.
cmake_minimum_required(VERSION 3.25)
project(rxguard_interaction_engine VERSION 0.0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

option(RXG_BUILD_TESTS "Build unit tests" ON)
option(RXG_SANITIZE "Build with AddressSanitizer + UndefinedBehaviorSanitizer" OFF)

if(RXG_SANITIZE)
  add_compile_options(-fsanitize=address,undefined -fno-omit-frame-pointer -fno-sanitize-recover=all)
  add_link_options(-fsanitize=address,undefined)
endif()

add_library(rxg_engine SHARED src/engine.cpp)
target_include_directories(rxg_engine PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)
target_compile_options(rxg_engine PRIVATE -Wall -Wextra -Wpedantic -Werror)
target_compile_definitions(rxg_engine PRIVATE RXG_VERSION="${PROJECT_VERSION}")
set_target_properties(rxg_engine PROPERTIES CXX_VISIBILITY_PRESET hidden)

if(RXG_BUILD_TESTS)
  include(FetchContent)
  FetchContent_Declare(
    googletest
    URL https://github.com/google/googletest/archive/refs/tags/v1.18.0.tar.gz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
  set(INSTALL_GTEST OFF CACHE BOOL "" FORCE)
  FetchContent_MakeAvailable(googletest)

  enable_testing()
  add_executable(rxg_tests tests/engine_test.cpp)
  target_link_libraries(rxg_tests PRIVATE rxg_engine GTest::gtest_main)
  target_compile_options(rxg_tests PRIVATE -Wall -Wextra -Werror)
  include(GoogleTest)
  gtest_discover_tests(rxg_tests)
endif()
