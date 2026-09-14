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

/**
 * NyrFps performance settings screen.
 *
 * Minecraft 26.2 compatible version.
 *
 * Contains:
 * - Overview
 * - Chunk Engine
 * - Graphics
 * - Vulkan
 * - Android
 */
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

    public NyrFpsPerformanceScreen(
            Component title,
            Screen parent
    ) {
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

    /*
     * ------------------------------------------------------------
     * Navigation
     * ------------------------------------------------------------
     */

    private void addNav() {
        int y = 70;

        for (int i = 0; i < PAGES.length; i++) {

            int idx = i;

            NyrControlWidget nav =
                    new NyrControlWidget(
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

    /*
     * ------------------------------------------------------------
     * Page content
     * ------------------------------------------------------------
     */

    private void buildPage() {

        int contentX =
                NAV_WIDTH + PADDING;

        int contentW =
                Math.max(
                        280,
                        this.width - contentX - PADDING
                );

        int y = 70;

        switch (page) {

            /*
             * OVERVIEW
             */
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

            /*
             * CHUNK ENGINE
             */
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

            /*
             * GRAPHICS
             */
            case 2 -> {

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Entity culling",
                        () -> getBool(vk, "entityCulling"),
                        v -> {
                            setBool(
                                    vk,
                                    "entityCulling",
                                    v
                            );

                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Advanced culling",
                        () -> getInt(
                                vk,
                                "advCulling"
                        ) != 0,
                        v -> {
                            setInt(
                                    vk,
                                    "advCulling",
                                    v ? 1 : 0
                            );

                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Back-face culling",
                        () -> getBool(
                                vk,
                                "backFaceCulling"
                        ),
                        v -> {
                            setBool(
                                    vk,
                                    "backFaceCulling",
                                    v
                            );

                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Texture animations",
                        () -> getBool(
                                vk,
                                "textureAnimations"
                        ),
                        v -> {
                            setBool(
                                    vk,
                                    "textureAnimations",
                                    v
                            );

                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Indirect draw",
                        () -> getBool(
                                vk,
                                "indirectDraw"
                        ),
                        v -> {
                            setBool(
                                    vk,
                                    "indirectDraw",
                                    v
                            );

                            saveVulkan();
                        }
                );
            }

            /*
             * VULKAN
             */
            case 3 -> {

                addInt(
                        contentX,
                        y,
                        contentW,
                        "Frame queue size",
                        () -> vk != null
                                ? getInt(
                                        vk,
                                        "frameQueueSize"
                                )
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
                                ? getInt(
                                        vk,
                                        "builderThreads"
                                )
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
                        () -> getBool(
                                vk,
                                "uniqueOpaqueLayer"
                        ),
                        v -> {
                            setBool(
                                    vk,
                                    "uniqueOpaqueLayer",
                                    v
                            );

                            saveVulkan();
                        }
                );

                y += ROW_HEIGHT + ROW_GAP;

                addBool(
                        contentX,
                        y,
                        contentW,
                        "Ambient occlusion",
                        () -> getInt(
                                vk,
                                "ambientOcclusion"
                        ) != 0,
                        v -> {
                            setInt(
                                    vk,
                                    "ambientOcclusion",
                                    v ? 1 : 0
                            );

                            saveVulkan();
                        }
                );
            }

            /*
             * ANDROID
             */
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

        /*
         * Close button
         */
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

    /*
     * ------------------------------------------------------------
     * Widget helpers
     * ------------------------------------------------------------
     */

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
