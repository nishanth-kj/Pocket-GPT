#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <algorithm>
#include <cctype>
#include <android/log.h>

#define LOG_TAG "PocketGPT_Native"

extern "C" JNIEXPORT jstring JNICALL
Java_com_pocketgpt_app_utils_NativeEngine_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Pocket GPT Native C++ Engine (Optimized High-Performance)";
    return env->NewStringUTF(hello.c_str());
}

/**
 * Fast SIMD/optimized C++ Cosine Similarity calculation between two float vectors.
 */
extern "C" JNIEXPORT jfloat JNICALL
Java_com_pocketgpt_app_utils_NativeEngine_nativeCosineSimilarity(
        JNIEnv* env,
        jclass /* clazz */,
        jfloatArray v1,
        jfloatArray v2) {
    if (v1 == nullptr || v2 == nullptr) return 0.0f;

    jsize len1 = env->GetArrayLength(v1);
    jsize len2 = env->GetArrayLength(v2);
    if (len1 != len2 || len1 == 0) return 0.0f;

    jfloat* p1 = env->GetFloatArrayElements(v1, nullptr);
    jfloat* p2 = env->GetFloatArrayElements(v2, nullptr);

    float dot = 0.0f;
    float norm1 = 0.0f;
    float norm2 = 0.0f;

    for (jsize i = 0; i < len1; i++) {
        float val1 = p1[i];
        float val2 = p2[i];
        dot += val1 * val2;
        norm1 += val1 * val1;
        norm2 += val2 * val2;
    }

    env->ReleaseFloatArrayElements(v1, p1, JNI_ABORT);
    env->ReleaseFloatArrayElements(v2, p2, JNI_ABORT);

    if (norm1 <= 0.0f || norm2 <= 0.0f) {
        return std::max(0.0f, std::min(1.0f, dot));
    }

    float sim = dot / (std::sqrt(norm1) * std::sqrt(norm2));
    return std::max(0.0f, std::min(1.0f, sim));
}

/**
 * Native C++ Feature Hashing & Embedding Generator.
 * Accelerates vector embedding creation by running tokenization & hashing in C++.
 */
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_pocketgpt_app_utils_NativeEngine_nativeFastEmbedding(
        JNIEnv* env,
        jclass /* clazz */,
        jstring text,
        jint dimension) {
    if (dimension <= 0) {
        return env->NewFloatArray(0);
    }

    std::vector<float> vec(dimension, 0.0f);

    if (text != nullptr) {
        const char* str = env->GetStringUTFChars(text, nullptr);
        if (str != nullptr) {
            std::string s(str);
            env->ReleaseStringUTFChars(text, str);

            // Normalize
            std::string normalized;
            normalized.reserve(s.size());
            for (char c : s) {
                if (std::isalnum(static_cast<unsigned char>(c))) {
                    normalized += static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
                } else {
                    normalized += ' ';
                }
            }

            // Tokenize
            std::vector<std::string> tokens;
            size_t start = 0, end = 0;
            while ((end = normalized.find(' ', start)) != std::string::npos) {
                if (end > start) {
                    tokens.push_back(normalized.substr(start, end - start));
                }
                start = end + 1;
            }
            if (start < normalized.length()) {
                tokens.push_back(normalized.substr(start));
            }

            // C++ Feature Hashing
            for (size_t i = 0; i < tokens.size(); i++) {
                const std::string& tok = tokens[i];
                if (tok.empty()) continue;

                // Hash 1
                unsigned int h1 = 0x9747b28c;
                for (char c : tok) h1 = (h1 * 31) ^ static_cast<unsigned char>(c);
                int idx1 = std::abs(static_cast<int>(h1)) % dimension;
                vec[idx1] += 1.5f;

                // Hash 2 (Bigram)
                if (i < tokens.size() - 1 && !tokens[i + 1].empty()) {
                    std::string bigram = tok + "_" + tokens[i + 1];
                    unsigned int h2 = 0x5bd1e995;
                    for (char c : bigram) h2 = (h2 * 31) ^ static_cast<unsigned char>(c);
                    int idx2 = std::abs(static_cast<int>(h2)) % dimension;
                    vec[idx2] += 2.0f;
                }
            }

            // L2 Normalization
            float norm = 0.0f;
            for (float v : vec) norm += v * v;

            if (norm > 0.0f) {
                float invNorm = 1.0f / std::sqrt(norm);
                for (int i = 0; i < dimension; i++) {
                    vec[i] *= invNorm;
                }
            }
        }
    }

    jfloatArray result = env->NewFloatArray(dimension);
    env->SetFloatArrayRegion(result, 0, dimension, vec.data());
    return result;
}

/**
 * Fast C++ Keyword Match Score calculation between query and chunk text.
 */
extern "C" JNIEXPORT jfloat JNICALL
Java_com_pocketgpt_app_utils_NativeEngine_nativeKeywordMatchScore(
        JNIEnv* env,
        jclass /* clazz */,
        jstring query,
        jstring chunkText) {
    if (query == nullptr || chunkText == nullptr) return 0.0f;

    const char* qStr = env->GetStringUTFChars(query, nullptr);
    const char* cStr = env->GetStringUTFChars(chunkText, nullptr);

    if (qStr == nullptr || cStr == nullptr) {
        if (qStr) env->ReleaseStringUTFChars(query, qStr);
        if (cStr) env->ReleaseStringUTFChars(chunkText, cStr);
        return 0.0f;
    }

    std::string q(qStr);
    std::string c(cStr);
    env->ReleaseStringUTFChars(query, qStr);
    env->ReleaseStringUTFChars(chunkText, cStr);

    if (q.empty() || c.empty()) return 0.0f;

    // Convert chunk to lowercase
    std::transform(c.begin(), c.end(), c.begin(), [](unsigned char ch){ return std::tolower(ch); });

    // Tokenize query
    std::string normalizedQ;
    normalizedQ.reserve(q.size());
    for (char ch : q) {
        if (std::isalnum(static_cast<unsigned char>(ch))) {
            normalizedQ += static_cast<char>(std::tolower(static_cast<unsigned char>(ch)));
        } else {
            normalizedQ += ' ';
        }
    }

    std::vector<std::string> queryTokens;
    size_t start = 0, end = 0;
    while ((end = normalizedQ.find(' ', start)) != std::string::npos) {
        if (end > start) {
            std::string tok = normalizedQ.substr(start, end - start);
            if (tok.length() > 2) queryTokens.push_back(tok);
        }
        start = end + 1;
    }
    if (start < normalizedQ.length()) {
        std::string tok = normalizedQ.substr(start);
        if (tok.length() > 2) queryTokens.push_back(tok);
    }

    if (queryTokens.empty()) return 0.0f;

    int matched = 0;
    for (const auto& tok : queryTokens) {
        if (c.find(tok) != std::string::npos) {
            matched++;
        }
    }

    return static_cast<float>(matched) / static_cast<float>(queryTokens.size());
}

/**
 * Fast C++ Document Chunker.
 * Splits raw document text into context-preserving chunks with word-boundary awareness.
 */
extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_pocketgpt_app_utils_NativeEngine_nativeChunkText(
        JNIEnv* env,
        jclass /* clazz */,
        jstring text,
        jint chunkSize,
        jint overlap) {
    if (text == nullptr || chunkSize <= 0) {
        jclass strClass = env->FindClass("java/lang/String");
        return env->NewObjectArray(0, strClass, nullptr);
    }

    const char* str = env->GetStringUTFChars(text, nullptr);
    if (str == nullptr) {
        jclass strClass = env->FindClass("java/lang/String");
        return env->NewObjectArray(0, strClass, nullptr);
    }

    std::string s(str);
    env->ReleaseStringUTFChars(text, str);

    std::vector<std::string> chunks;
    int length = static_cast<int>(s.length());
    int start = 0;

    while (start < length) {
        int end = std::min(start + chunkSize, length);

        if (end < length) {
            size_t lastSpace = s.rfind(' ', end);
            if (lastSpace != std::string::npos && lastSpace > static_cast<size_t>(start + overlap)) {
                end = static_cast<int>(lastSpace);
            }
        }

        std::string chunk = s.substr(start, end - start);
        size_t first = chunk.find_first_not_of(" \t\n\r");
        if (first != std::string::npos) {
            size_t last = chunk.find_last_not_of(" \t\n\r");
            chunks.push_back(chunk.substr(first, (last - first + 1)));
        }

        start = end - overlap;
        if (start <= end - chunkSize) {
            start = end;
        }
    }

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray resultArray = env->NewObjectArray(static_cast<jsize>(chunks.size()), stringClass, nullptr);

    for (size_t i = 0; i < chunks.size(); i++) {
        jstring js = env->NewStringUTF(chunks[i].c_str());
        env->SetObjectArrayElement(resultArray, static_cast<jsize>(i), js);
        env->DeleteLocalRef(js);
    }

    return resultArray;
}
