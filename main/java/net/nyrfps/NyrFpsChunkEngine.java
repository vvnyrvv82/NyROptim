package net.nyrfps;

/** Lightweight mobile chunk policy bridge. Keeps tuning isolated from rendering. */
public final class NyrFpsChunkEngine {
    private NyrFpsChunkEngine() {}

    public static void applyMobilePolicy(NyrFpsConfig cfg) {
        if (cfg == null) return;
        cfg.builderThreads = Math.max(1, Math.min(cfg.builderThreads, 4));
        cfg.frameQueueSize = Math.max(1, Math.min(cfg.frameQueueSize, 6));
        cfg.chunkBuildBudgetMs = Math.max(1, Math.min(cfg.chunkBuildBudgetMs, 8));
        cfg.preloadRadius = Math.max(0, Math.min(cfg.preloadRadius, 4));
    }
}
