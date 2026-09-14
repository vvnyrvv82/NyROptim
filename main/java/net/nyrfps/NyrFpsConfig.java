package net.nyrfps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persistent NyrFps settings. Stored as a small hand-rolled JSON file
 * (no external JSON dependency) at {@code <configDir>/nyrfps.json}.
 *
 * This class was already working correctly in the original build — kept
 * as-is functionally, just tidied up.
 */
public class NyrFpsConfig {

    public int builderThreads = 2;
    public int frameQueueSize = 3;
    public int chunkBuildBudgetMs = 4;
    public int preloadRadius = 2;
    public boolean adaptiveBuilder = true;
    public boolean mobileProfile = true;

    private final Path path;

    public NyrFpsConfig(Path configDir) {
        this.path = configDir.resolve("nyrfps.json");
        load();
    }

    public void load() {
        try {
            if (!Files.exists(path, new LinkOption[0])) {
                return;
            }
            String json = Files.readString(path);
            builderThreads = readInt(json, "builderThreads", builderThreads);
            frameQueueSize = readInt(json, "frameQueueSize", frameQueueSize);
            chunkBuildBudgetMs = readInt(json, "chunkBuildBudgetMs", chunkBuildBudgetMs);
            preloadRadius = readInt(json, "preloadRadius", preloadRadius);
            adaptiveBuilder = readBool(json, "adaptiveBuilder", adaptiveBuilder);
            mobileProfile = readBool(json, "mobileProfile", mobileProfile);
        } catch (Exception ignored) {
            // Corrupt or unreadable config file — keep whatever values we already have.
        }
    }

    public void save() {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            String json = "{\n"
                    + "  \"builderThreads\": " + builderThreads + ",\n"
                    + "  \"frameQueueSize\": " + frameQueueSize + ",\n"
                    + "  \"chunkBuildBudgetMs\": " + chunkBuildBudgetMs + ",\n"
                    + "  \"preloadRadius\": " + preloadRadius + ",\n"
                    + "  \"adaptiveBuilder\": " + adaptiveBuilder + ",\n"
                    + "  \"mobileProfile\": " + mobileProfile + "\n"
                    + "}\n";
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // Best effort — a failed save shouldn't crash the game.
        }
    }

    private static int readInt(String json, String key, int fallback) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : fallback;
    }

    private static boolean readBool(String json, String key, boolean fallback) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)").matcher(json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : fallback;
    }
}
