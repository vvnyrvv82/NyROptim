package net.nyrfps;

import net.fabricmc.api.ClientModInitializer;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NyrFpsInitializer implements ClientModInitializer {

    public static NyrFpsConfig CONFIG;

    @Override
    public void onInitializeClient() {
        try {
            Path configDir = Paths.get(System.getProperty("user.dir", "."), "config");
            CONFIG = new NyrFpsConfig(configDir);
            CONFIG.save();
        } catch (Throwable t) {
            // Fall back to a plain relative path if we couldn't resolve/write the real one.
            CONFIG = new NyrFpsConfig(Paths.get("config"));
        }

        // Push our current settings into VulkanMod's live config right away.
        syncVulkanConfig(CONFIG);

        NyrFpsChunkEngine.applyMobilePolicy(CONFIG);
    }

    /**
     * Pushes our performance settings into VulkanMod's live config object and persists
     * them to disk.
     * <p>
     * This is the ONE place that talks to VulkanMod's config. In the previous build this
     * logic was copy-pasted separately into the "Chunk Engine", "Vulkan" and "Android"
     * settings tabs, and two of those three copies forgot to actually write the new value
     * into VulkanMod's object before saving — so those sliders looked like they worked
     * (the number changed, the file got written) but had no real effect on the renderer
     * until the game was restarted. Routing every tab through this single method removes
     * that whole class of bug.
     */
    public static void syncVulkanConfig(NyrFpsConfig cfg) {
        try {
            Class<?> vmInit = Class.forName("net.vulkanmod.Initializer");
            Object vkConfig = vmInit.getField("CONFIG").get(null);
            if (vkConfig == null) return;
            setIntField(vkConfig, "builderThreads", cfg.builderThreads);
            setIntField(vkConfig, "frameQueueSize", cfg.frameQueueSize);
            Class.forName("net.vulkanmod.config.Config").getMethod("write").invoke(vkConfig);
        } catch (Throwable ignored) {
            // VulkanMod missing or its API changed — NyrFps still works standalone.
        }
    }

    private static void setIntField(Object target, String name, int value) throws ReflectiveOperationException {
        target.getClass().getField(name).setInt(target, value);
    }
}
