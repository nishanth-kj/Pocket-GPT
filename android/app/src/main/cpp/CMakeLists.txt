cmake_minimum_required(VERSION 3.22.1)

project("pocketgpt_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ---------------------------------------------------------------------------
# Vendored llama.cpp (git submodule) - real on-device GGUF LLM inference.
# Only the pieces we need are built: the core ggml/llama libraries and the
# "common" helper library (chat templating, sampling, tokenize utils).
# Tools/examples/server/tests are disabled to keep the build fast and lean,
# and network-fetch-only options (OpenSSL/HTTPS, curl) are disabled since
# this app already downloads models itself over plain HttpURLConnection.
# ---------------------------------------------------------------------------
set(LLAMA_SRC ${CMAKE_CURRENT_LIST_DIR}/llama.cpp)

set(LLAMA_BUILD_COMMON ON)
set(LLAMA_BUILD_TESTS OFF)
set(LLAMA_BUILD_TOOLS OFF)
set(LLAMA_BUILD_EXAMPLES OFF)
set(LLAMA_BUILD_SERVER OFF)
set(LLAMA_BUILD_APP OFF)
set(LLAMA_OPENSSL OFF)
set(LLAMA_SUBPROCESS OFF)
set(BUILD_SHARED_LIBS OFF)

set(GGML_NATIVE OFF)
set(GGML_OPENMP OFF)
set(GGML_LLAMAFILE OFF)
set(GGML_CPU_KLEIDIAI OFF)

add_subdirectory(${LLAMA_SRC} llama_build)

add_library(
        pocketgpt_native
        SHARED
        native_lib.cpp
        llama_bridge.cpp)

target_include_directories(
        pocketgpt_native
        PRIVATE
        ${LLAMA_SRC}/include
        ${LLAMA_SRC}/ggml/include
        ${LLAMA_SRC}/common)

target_compile_options(pocketgpt_native PRIVATE -O3)

# -ffast-math implies -ffinite-math-only, which is incompatible with ggml's
# CPU kernels (they explicitly require NaN/Inf-aware arithmetic), so it is
# only applied to our own standalone embedding/chunking utilities, never to
# llama_bridge.cpp or anything under the llama.cpp subdirectory.
set_source_files_properties(native_lib.cpp PROPERTIES COMPILE_OPTIONS "-ffast-math")

find_library(
        log-lib
        log)

target_link_libraries(
        pocketgpt_native
        llama
        llama-common
        ${log-lib})

# Required for compatibility with 16 KB memory page size devices (mandatory
# for Google Play as of late 2025). Without this the linker defaults to a
# 4 KB-aligned LOAD segment, which Android then only loads in a slower
# compatibility shim on 16 KB-page hardware.
target_link_options(
        pocketgpt_native
        PRIVATE
        "-Wl,-z,max-page-size=16384")
