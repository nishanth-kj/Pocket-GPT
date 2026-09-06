#include <jni.h>
#include <android/log.h>
#include <algorithm>
#include <mutex>
#include <string>
#include <vector>

#include "llama.h"
#include "common.h"
#include "sampling.h"
#include "chat.h"

#define LOG_TAG "PocketGPT_LlamaBridge"
#define LOGi(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGw(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGe(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

struct LlamaSession {
    llama_model *model = nullptr;
    llama_context *ctx = nullptr;
    llama_batch batch{};
    common_chat_templates_ptr chat_templates;
    int n_ctx = 0;
};

std::mutex g_backend_mutex;
bool g_backend_initialized = false;

void ensure_backend_init() {
    std::lock_guard<std::mutex> lock(g_backend_mutex);
    if (!g_backend_initialized) {
        llama_backend_init();
        g_backend_initialized = true;
    }
}

// Checks whether a UTF-8 byte sequence is a complete, well-formed string.
// Needed because llama tokens can decode to partial multi-byte UTF-8
// sequences that must be buffered until the remaining bytes arrive.
bool is_valid_utf8(const std::string &s) {
    const auto *bytes = reinterpret_cast<const unsigned char *>(s.c_str());
    int num;
    while (*bytes != 0x00) {
        if ((*bytes & 0x80) == 0x00) {
            num = 1;
        } else if ((*bytes & 0xE0) == 0xC0) {
            num = 2;
        } else if ((*bytes & 0xF0) == 0xE0) {
            num = 3;
        } else if ((*bytes & 0xF8) == 0xF0) {
            num = 4;
        } else {
            return false;
        }
        bytes++;
        for (int i = 1; i < num; i++) {
            if ((*bytes & 0xC0) != 0x80) return false;
            bytes++;
        }
    }
    return true;
}

// Decodes a batch of tokens, splitting into sub-batches of at most n_batch.
bool decode_tokens(llama_context *ctx, llama_batch &batch, const std::vector<llama_token> &tokens,
                    llama_pos start_pos, int n_batch, bool want_last_logit) {
    for (size_t i = 0; i < tokens.size(); i += n_batch) {
        size_t chunk = std::min(static_cast<size_t>(n_batch), tokens.size() - i);
        common_batch_clear(batch);
        for (size_t j = 0; j < chunk; j++) {
            bool isLast = want_last_logit && (i + j == tokens.size() - 1);
            common_batch_add(batch, tokens[i + j], start_pos + static_cast<llama_pos>(i + j), {0}, isLast);
        }
        if (llama_decode(ctx, batch) != 0) {
            LOGe("llama_decode failed while processing prompt");
            return false;
        }
    }
    return true;
}

} // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_com_pocketgpt_app_utils_LlamaEngine_nativeLoadModel(
        JNIEnv *env, jclass /*clazz*/, jstring jModelPath, jint nCtx, jint nThreads) {
    ensure_backend_init();

    const char *modelPathChars = env->GetStringUTFChars(jModelPath, nullptr);
    std::string modelPath = modelPathChars ? modelPathChars : "";
    if (modelPathChars) env->ReleaseStringUTFChars(jModelPath, modelPathChars);

    if (modelPath.empty()) {
        LOGe("nativeLoadModel: empty model path");
        return 0;
    }

    llama_model_params model_params = llama_model_default_params();
    llama_model *model = llama_model_load_from_file(modelPath.c_str(), model_params);
    if (!model) {
        LOGe("Failed to load model from %s", modelPath.c_str());
        return 0;
    }

    int trainedCtx = llama_model_n_ctx_train(model);
    int effectiveCtx = nCtx > 0 ? std::min(nCtx, trainedCtx) : trainedCtx;
    if (effectiveCtx <= 0) effectiveCtx = 2048;

    int threads = nThreads > 0 ? nThreads : 4;

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = effectiveCtx;
    ctx_params.n_batch = 512;
    ctx_params.n_ubatch = 512;
    ctx_params.n_threads = threads;
    ctx_params.n_threads_batch = threads;

    llama_context *ctx = llama_init_from_model(model, ctx_params);
    if (!ctx) {
        LOGe("Failed to create llama context");
        llama_model_free(model);
        return 0;
    }

    auto *session = new LlamaSession();
    session->model = model;
    session->ctx = ctx;
    session->batch = llama_batch_init(512, 0, 1);
    session->chat_templates = common_chat_templates_init(model, "");
    session->n_ctx = effectiveCtx;

    LOGi("Model loaded: %s (n_ctx=%d, threads=%d)", modelPath.c_str(), effectiveCtx, threads);
    return reinterpret_cast<jlong>(session);
}

extern "C" JNIEXPORT void JNICALL
Java_com_pocketgpt_app_utils_LlamaEngine_nativeGenerate(
        JNIEnv *env, jclass /*clazz*/, jlong handle, jstring jSystemPrompt, jstring jUserPrompt,
        jint maxTokens, jfloat temperature, jobject callback) {
    auto *session = reinterpret_cast<LlamaSession *>(handle);
    if (!session || !session->ctx || !session->model) {
        LOGe("nativeGenerate: invalid session handle");
        return;
    }

    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onToken", "(Ljava/lang/String;)Z");
    if (!onTokenMethod) {
        LOGe("nativeGenerate: TokenSink.onToken method not found");
        return;
    }

    const char *systemChars = jSystemPrompt ? env->GetStringUTFChars(jSystemPrompt, nullptr) : nullptr;
    std::string systemPrompt = systemChars ? systemChars : "";
    if (systemChars) env->ReleaseStringUTFChars(jSystemPrompt, systemChars);

    const char *userChars = env->GetStringUTFChars(jUserPrompt, nullptr);
    std::string userPrompt = userChars ? userChars : "";
    if (userChars) env->ReleaseStringUTFChars(jUserPrompt, userChars);

    // Fresh single-turn generation: clear any KV cache left over from a
    // previous question so each answer starts from a clean context.
    llama_memory_clear(llama_get_memory(session->ctx), true);

    std::vector<common_chat_msg> chat_msgs;
    bool hasTemplate = common_chat_templates_was_explicit(session->chat_templates.get());
    llama_pos pos = 0;
    const int n_batch = 512;
    const int safety_margin = std::max(8, maxTokens + 8);
    const int max_prompt_tokens = std::max(1, session->n_ctx - safety_margin);

    auto append_message = [&](const std::string &role, const std::string &content, bool add_ass) -> bool {
        common_chat_msg msg;
        msg.role = role;
        msg.content = content;
        std::string formatted = hasTemplate
                ? common_chat_format_single(session->chat_templates.get(), chat_msgs, msg, add_ass, false)
                : content;
        chat_msgs.push_back(msg);

        bool addSpecial = (pos == 0);
        std::vector<llama_token> tokens = common_tokenize(session->ctx, formatted, addSpecial, true);
        if (static_cast<int>(tokens.size()) > max_prompt_tokens) {
            int skipped = static_cast<int>(tokens.size()) - max_prompt_tokens;
            tokens.resize(max_prompt_tokens);
            LOGw("Prompt truncated, dropped %d tokens to fit context", skipped);
        }
        if (tokens.empty()) return true;

        bool wantLastLogit = false; // set by caller after both messages are appended
        (void) wantLastLogit;
        if (!decode_tokens(session->ctx, session->batch, tokens, pos, n_batch, false)) {
            return false;
        }
        pos += static_cast<llama_pos>(tokens.size());
        return true;
    };

    if (!systemPrompt.empty()) {
        if (!append_message("system", systemPrompt, false)) {
            LOGe("Failed to process system prompt");
            return;
        }
    }

    // Re-decode the last token of the user turn with logits enabled so we
    // can sample immediately after, matching llama.cpp's Android reference.
    {
        common_chat_msg msg;
        msg.role = "user";
        msg.content = userPrompt;
        std::string formatted = hasTemplate
                ? common_chat_format_single(session->chat_templates.get(), chat_msgs, msg, true, false)
                : userPrompt;
        chat_msgs.push_back(msg);

        bool addSpecial = (pos == 0);
        std::vector<llama_token> tokens = common_tokenize(session->ctx, formatted, addSpecial, true);
        if (static_cast<int>(tokens.size()) > max_prompt_tokens) {
            int skipped = static_cast<int>(tokens.size()) - max_prompt_tokens;
            tokens.resize(max_prompt_tokens);
            LOGw("User prompt truncated, dropped %d tokens to fit context", skipped);
        }
        if (tokens.empty()) {
            LOGe("User prompt tokenized to zero tokens");
            return;
        }
        if (!decode_tokens(session->ctx, session->batch, tokens, pos, n_batch, true)) {
            LOGe("Failed to process user prompt");
            return;
        }
        pos += static_cast<llama_pos>(tokens.size());
    }

    common_params_sampling sparams;
    sparams.temp = temperature > 0.0f ? temperature : 0.1f;
    common_sampler *sampler = common_sampler_init(session->model, sparams);
    if (!sampler) {
        LOGe("Failed to initialize sampler");
        return;
    }

    const llama_vocab *vocab = llama_model_get_vocab(session->model);
    std::string cachedChars;
    int generated = 0;
    int limit = maxTokens > 0 ? maxTokens : 256;
    int stop_position = session->n_ctx - 4;

    while (generated < limit && pos < stop_position) {
        llama_token newToken = common_sampler_sample(sampler, session->ctx, -1);
        common_sampler_accept(sampler, newToken, true);

        if (llama_vocab_is_eog(vocab, newToken)) {
            break;
        }

        common_batch_clear(session->batch);
        common_batch_add(session->batch, newToken, pos, {0}, true);
        if (llama_decode(session->ctx, session->batch) != 0) {
            LOGe("llama_decode failed during generation");
            break;
        }
        pos++;
        generated++;

        cachedChars += common_token_to_piece(session->ctx, newToken);
        if (!is_valid_utf8(cachedChars)) {
            continue; // wait for the rest of a multi-byte UTF-8 sequence
        }

        jstring jPiece = env->NewStringUTF(cachedChars.c_str());
        cachedChars.clear();
        jboolean shouldContinue = env->CallBooleanMethod(callback, onTokenMethod, jPiece);
        env->DeleteLocalRef(jPiece);

        if (env->ExceptionCheck()) {
            env->ExceptionDescribe();
            env->ExceptionClear();
            break;
        }
        if (!shouldContinue) {
            LOGi("Generation cancelled by callback");
            break;
        }
    }

    common_sampler_free(sampler);
}

extern "C" JNIEXPORT void JNICALL
Java_com_pocketgpt_app_utils_LlamaEngine_nativeUnload(JNIEnv * /*env*/, jclass /*clazz*/, jlong handle) {
    auto *session = reinterpret_cast<LlamaSession *>(handle);
    if (!session) return;

    session->chat_templates.reset();
    if (session->batch.token != nullptr || session->batch.embd != nullptr) {
        llama_batch_free(session->batch);
    }
    if (session->ctx) llama_free(session->ctx);
    if (session->model) llama_model_free(session->model);
    delete session;
}
