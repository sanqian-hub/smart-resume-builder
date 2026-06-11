package com.srb.backend.support;

import java.util.UUID;

public final class AiChatTelemetry {

    private final String requestId;
    private final Long resumeId;
    private final Long userId;
    private final boolean persist;
    private final String mode;
    private final int messageLength;
    private final int moduleCount;
    private final long requestStartedAt;
    private Long firstTokenLatencyMs;

    public AiChatTelemetry(Long resumeId, Long userId, boolean persist, String mode, int messageLength, int moduleCount) {
        this.requestId = UUID.randomUUID().toString().substring(0, 8);
        this.resumeId = resumeId;
        this.userId = userId;
        this.persist = persist;
        this.mode = mode;
        this.messageLength = messageLength;
        this.moduleCount = moduleCount;
        this.requestStartedAt = System.currentTimeMillis();
    }

    public String requestReceivedLog() {
        return "ai_chat.request_received"
                + " requestId=" + requestId
                + " resumeId=" + safeValue(resumeId)
                + " userId=" + safeValue(userId)
                + " persist=" + persist
                + " mode=" + safeValue(mode)
                + " messageLength=" + messageLength
                + " moduleCount=" + moduleCount;
    }

    public String promptBuiltLog(int systemPromptLength, int filledModuleCount, int memoryCount, int historyCount, int totalMessageCount) {
        return "ai_chat.prompt_built"
                + " requestId=" + requestId
                + " systemPromptLength=" + systemPromptLength
                + " filledModuleCount=" + filledModuleCount
                + " memoryCount=" + memoryCount
                + " historyCount=" + historyCount
                + " totalMessageCount=" + totalMessageCount;
    }

    public long markFirstToken() {
        if (firstTokenLatencyMs == null) {
            firstTokenLatencyMs = System.currentTimeMillis() - requestStartedAt;
        }
        return firstTokenLatencyMs;
    }

    public String firstTokenLog() {
        return "ai_chat.first_token"
                + " requestId=" + requestId
                + " latencyMs=" + (firstTokenLatencyMs == null ? -1 : firstTokenLatencyMs);
    }

    public String retryTriggeredLog(String reason) {
        return "ai_chat.retry_triggered"
                + " requestId=" + requestId
                + " reason=" + safeValue(reason);
    }

    public String retryCompletedLog(long latencyMs, boolean success, int suggestCount) {
        return "ai_chat.retry_completed"
                + " requestId=" + requestId
                + " latencyMs=" + latencyMs
                + " success=" + success
                + " suggestCount=" + suggestCount;
    }

    public String streamDoneLog(int outputLength, int suggestCount, boolean retried) {
        return "ai_chat.stream_done"
                + " requestId=" + requestId
                + " totalLatencyMs=" + (System.currentTimeMillis() - requestStartedAt)
                + " outputLength=" + outputLength
                + " suggestCount=" + suggestCount
                + " retried=" + retried;
    }

    public String memoryExtractDoneLog(long latencyMs, int insertedCount, int dedupedCount, boolean compressed, long totalBeforeCompress, int totalAfterCompress) {
        return "ai_chat.memory_extract_done"
                + " requestId=" + requestId
                + " latencyMs=" + latencyMs
                + " insertedCount=" + insertedCount
                + " dedupedCount=" + dedupedCount
                + " compressed=" + compressed
                + " totalBeforeCompress=" + totalBeforeCompress
                + " totalAfterCompress=" + totalAfterCompress;
    }

    public String memoryCompressDoneLog(long latencyMs, long totalBeforeCompress, int totalAfterCompress) {
        return "ai_chat.memory_compress_done"
                + " requestId=" + requestId
                + " latencyMs=" + latencyMs
                + " totalBeforeCompress=" + totalBeforeCompress
                + " totalAfterCompress=" + totalAfterCompress;
    }

    private static String safeValue(Object value) {
        return value == null ? "null" : String.valueOf(value);
    }
}
