package net.nyrfps.uix;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.nyrfps.NyrFpsChunkEngine;
import net.nyrfps.NyrFpsConfig;
import net.nyrfps.NyrFpsInitializer;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class NyrFpsPerformanceScreen extends Screen {

    private static final String[] PAGES = {
            "Overview",
            "Chunk Engine",
            "Graphics",
            "Vulkan",
            "Android"
    };

    private static final int NAV_WIDTH = 190;
    private static final int PADDING = 16;
    private static final int ROW_HEIGHT = 34;
    private static final int ROW_GAP = 8;

    private static final int COLOR_BG = 0xFF0B0F14;
    private static final int COLOR_NAV_BG = 0xFF10161D;
    private static final int COLOR_CONTENT_BG = 0xFF0D1218;
    private static final int COLOR_TITLE = 0xFFEAF2F7;
    private static final int COLOR_SUB = 0xFF8FA3B2;
    private static final int COLOR_HINT = 0xFF667B89;

    private final Screen parent;

    private int page;
    private NyrFpsConfig cfg;
    private Object vk;

    public NyrFpsPerformanceScreen(Component title, Screen parent) {
        super(title);

        this.parent = parent;
        this.page = 0;
        this.cfg = NyrFpsInitializer.CONFIG;
        this.vk = getVk();
    }

    @Override
    protected void init() {
        clearWidgets();

        if (cfg == null) {
            cfg = NyrFpsInitializer.CONFIG;
        }

        if (vk == null) {
            vk = getVk();
        }

        addNav();
        buildPage();
    }

    private void addNav() {
        int y = 70;

        for (int i = 0; i < PAGES.length; i++) {
            int idx = i;

            NyrControlWidget nav = new NyrControlWidget(
                    PADDING,
                    y,
                    NAV_WIDTH - PADDING * 2,
                    32,
                    PAGES[i],
                    () -> {
                        this.page = idx;
                        this.init();
                    }
            );

            addRenderableWidget(nav);

            y += 32 + 8;
        }
    }

    private void buildPage() {
        int contentX = NAV_WIDTH + PADDING;
        int contentW = Math.max(
                280,
                this.width - contentX - PADDING
        );

        int y = 70;

        switch (page) {

            case 0 -> {
                addBool(
                        contentX,
                        y,
                        contentW,
                        "Mobile profile (recommended on phones/tablets)",
                        () -> cfg.mobileProfile,
                        v -> {
                            cfg.mobileProfile = v;
                            cfg.save();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Adaptive chunk building",
                        () -> cfg.adaptiveBuilder,
                        v -> {
                            cfg.adaptiveBuilder = v;
                            cfg.save();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addRenderableWidget(
                        new NyrControlWidget(
                                contentX,
                                y,
                                contentW,
                                36,
                                "Apply & Close",
                                this::applyAndClose
                        )
                );
            }

            case 1 -> {
                addInt(
                        contentX,
                        y,
                        contentW,
                        "Builder threads",
                        () -> cfg.builderThreads,
                        this::setBuilderThreads,
                        1,
                        4,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addInt(
                        contentX,
                        y,
                        contentW,
                        "Frame queue",
                        () -> cfg.frameQueueSize,
                        this::setFrameQueueSize,
                        1,
                        4,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addInt(
                        contentX,
                        y,
                        contentW,
                        "Chunk build budget (ms)",
                        () -> cfg.chunkBuildBudgetMs,
                        v -> {
                            cfg.chunkBuildBudgetMs = v;
                            cfg.save();
                        },
                        1,
                        12,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addInt(
                        contentX,
                        y,
                        contentW,
                        "Preload radius",
                        () -> cfg.preloadRadius,
                        v -> {
                            cfg.preloadRadius = v;
                            cfg.save();
                        },
                        0,
                        6,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Adaptive scheduling",
                        () -> cfg.adaptiveBuilder,
                        v -> {
                            cfg.adaptiveBuilder = v;
                            cfg.save();
                        }
                );
            }

            case 2 -> {
                addBool(
                        contentX,
                        y,
                        contentW,
                        "Entity culling",
                        () -> getBool(vk, "entityCulling"),
                        v -> {
                            setBool(vk, "entityCulling", v);
                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Advanced culling",
                        () -> getInt(vk, "advCulling") != 0,
                        v -> {
                            setInt(vk, "advCulling", v ? 1 : 0);
                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Back-face culling",
                        () -> getBool(vk, "backFaceCulling"),
                        v -> {
                            setBool(vk, "backFaceCulling", v);
                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Texture animations",
                        () -> getBool(vk, "textureAnimations"),
                        v -> {
                            setBool(vk, "textureAnimations", v);
                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Indirect draw",
                        () -> getBool(vk, "indirectDraw"),
                        v -> {
                            setBool(vk, "indirectDraw", v);
                            saveVulkan();
                        }
                );
            }

            case 3 -> {
                addInt(
                        contentX,
                        y,
                        contentW,
                        "Frame queue size",
                        () -> vk != null
                                ? getInt(vk, "frameQueueSize")
                                : cfg.frameQueueSize,
                        this::setFrameQueueSize,
                        1,
                        4,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addInt(
                        contentX,
                        y,
                        contentW,
                        "Chunk builder threads",
                        () -> vk != null
                                ? getInt(vk, "builderThreads")
                                : cfg.builderThreads,
                        this::setBuilderThreads,
                        1,
                        4,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Unique opaque layer",
                        () -> getBool(vk, "uniqueOpaqueLayer"),
                        v -> {
                            setBool(vk, "uniqueOpaqueLayer", v);
                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Ambient occlusion",
                        () -> getInt(vk, "ambientOcclusion") != 0,
                        v -> {
                            setInt(vk, "ambientOcclusion", v ? 1 : 0);
                            saveVulkan();
                        }
                );
            }

            case 4 -> {
                addBool(
                        contentX,
                        y,
                        contentW,
                        "Mobile profile",
                        () -> cfg.mobileProfile,
                        v -> {
                            cfg.mobileProfile = v;
                            cfg.save();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addInt(
                        contentX,
                        y,
                        contentW,
                        "Worker threads",
                        () -> cfg.builderThreads,
                        this::setBuilderThreads,
                        1,
                        4,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addInt(
                        contentX,
                        y,
                        contentW,
                        "Chunk budget ms",
                        () -> cfg.chunkBuildBudgetMs,
                        v -> {
                            cfg.chunkBuildBudgetMs = v;
                            cfg.save();
                        },
                        1,
                        12,
                        1
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Adaptive scheduling",
                        () -> cfg.adaptiveBuilder,
                        v -> {
                            cfg.adaptiveBuilder = v;
                            cfg.save();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addRenderableWidget(
                        new NyrControlWidget(
                                contentX,
                                y,
                                contentW,
                                36,
                                "Reset mobile profile",
                                this::resetMobileProfile
                        )
                );
            }
        }

        addRenderableWidget(
                new NyrControlWidget(
                        this.width - 170,
                        this.height - 50,
                        146,
                        34,
                        "Close",
                        this::onClose
                )
        );
    }

    private void addBool(
            int x,
            int y,
            int w,
            String label,
            Supplier<Boolean> get,
            Consumer<Boolean> set
    ) {
        addRenderableWidget(
                new NyrControlWidget(
                        x,
                        y,
                        w,
                        ROW_HEIGHT,
                        label,
                        get,
                        set
                )
        );
    }

    private void addInt(
            int x,
            int y,
            int w,
            String label,
            IntSupplier get,
            IntConsumer set,
            int min,
            int max,
            int step
    ) {
        addRenderableWidget(
                new NyrControlWidget(
                        x,
                        y,
                        w,
                        ROW_HEIGHT,
                        label,
                        get,
                        set,
                        min,
                        max,
                        step
                )
        );
    }

    private void setBuilderThreads(int v) {
        cfg.builderThreads = v;

        NyrFpsChunkEngine.applyMobilePolicy(cfg);

        cfg.save();

        NyrFpsInitializer.syncVulkanConfig(cfg);
    }

    private void setFrameQueueSize(int v) {
        cfg.frameQueueSize = v;

        cfg.save();

        NyrFpsInitializer.syncVulkanConfig(cfg);
    }

    private void resetMobileProfile() {
        cfg.builderThreads = 2;
        cfg.frameQueueSize = 3;
        cfg.chunkBuildBudgetMs = 4;
        cfg.preloadRadius = 2;
        cfg.adaptiveBuilder = true;
        cfg.mobileProfile = true;

        NyrFpsChunkEngine.applyMobilePolicy(cfg);

        cfg.save();

        NyrFpsInitializer.syncVulkanConfig(cfg);

        init();
    }

    private void applyAndClose() {
        NyrFpsChunkEngine.applyMobilePolicy(cfg);

        cfg.save();

        NyrFpsInitializer.syncVulkanConfig(cfg);

        onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private Object getVk() {
        try {
            Class<?> vmInit =
                    Class.forName("net.vulkanmod.Initializer");

            return vmInit
                    .getField("CONFIG")
                    .get(null);

        } catch (Throwable t) {
            return null;
        }
    }

    private void saveVulkan() {
        if (vk == null) {
            return;
        }

        try {
            Class.forName(
                    "net.vulkanmod.config.Config"
            ).getMethod(
                    "write"
            ).invoke(vk);

        } catch (Throwable ignored) {
        }
    }

    private static boolean getBool(
            Object target,
            String field
    ) {
        if (target == null) {
            return false;
        }

        try {
            return target
                    .getClass()
                    .getField(field)
                    .getBoolean(target);

        } catch (Throwable t) {
            return false;
        }
    }

    private static int getInt(
            Object target,
            String field
    ) {
        if (target == null) {
            return 0;
        }

        try {
            return target
                    .getClass()
                    .getField(field)
                    .getInt(target);

        } catch (Throwable t) {
            return 0;
        }
    }

    private static void setBool(
            Object target,
            String field,
            boolean value
    ) {
        if (target == null) {
            return;
        }

        try {
            target
                    .getClass()
                    .getField(field)
                    .setBoolean(target, value);

        } catch (Throwable ignored) {
        }
    }

    private static void setInt(
            Object target,
            String field,
            int value
    ) {
        if (target == null) {
            return;
        }

        try {
            target
                    .getClass()
                    .getField(field)
                    .setInt(target, value);

        } catch (Throwable ignored) {
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor gfx,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        gfx.fill(
                0,
                0,
                this.width,
                this.height,
                COLOR_BG
        );

        gfx.fill(
                0,
                0,
                NAV_WIDTH,
                this.height,
                COLOR_NAV_BG
        );

        gfx.fill(
                NAV_WIDTH,
                0,
                this.width,
                this.height,
                COLOR_CONTENT_BG
        );

        super.extractRenderState(
                gfx,
                mouseX,
                mouseY,
                partialTick
        );

        gfx.text(
                this.font,
                "NyrFps",
                PADDING,
                20,
                COLOR_TITLE,
                true
        );

        gfx.text(
                this.font,
                "ANDROID PERFORMANCE",
                PADDING,
                36,
                COLOR_SUB,
                false
        );

        gfx.text(
                this.font,
                PAGES[this.page],
                NAV_WIDTH + PADDING,
                20,
                COLOR_TITLE,
                true
        );

        gfx.text(
                this.font,
                "Real interactive settings",
                NAV_WIDTH + PADDING,
                36,
                COLOR_SUB,
                false
        );

        gfx.text(
                this.font,
                "Tap a setting to change it",
                NAV_WIDTH + PADDING,
                this.height - 20,
                COLOR_HINT,
                false
        );
    }
}
