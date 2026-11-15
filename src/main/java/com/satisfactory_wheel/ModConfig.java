package com.satisfactory_wheel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class ToolEntry {
        private final String id;
        private final String name;

        public ToolEntry(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id != null ? id.trim() : "";
        }

        public String getName() {
            return name != null ? name.trim() : "";
        }
    }

    private static boolean holdToSelect = true;
    private static int wheelRadius = 95;
    private static List<ToolEntry> tools = new ArrayList<>();

    public static boolean isHoldToSelect() {
        return holdToSelect;
    }

    public static int getWheelRadius() {
        return wheelRadius;
    }

    public static List<ToolEntry> getTools() {
        return Collections.unmodifiableList(tools);
    }

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("satisfactory_wheel.json");
    }

    public static synchronized void load() {
        Path path = getConfigPath();
        try {
            if (!Files.exists(path)) {
                LOGGER.info("Config satisfactory_wheel.json not found, generating template...");
                generateTemplate(path);
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                if (root.has("hold_to_select")) {
                    holdToSelect = root.get("hold_to_select").getAsBoolean();
                } else {
                    holdToSelect = true;
                }

                if (root.has("wheel_radius")) {
                    wheelRadius = Math.max(50, Math.min(250, root.get("wheel_radius").getAsInt()));
                } else {
                    wheelRadius = 95;
                }

                List<ToolEntry> loadedTools = new ArrayList<>();
                if (root.has("tools") && root.get("tools").isJsonArray()) {
                    JsonArray array = root.getAsJsonArray("tools");
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i).isJsonObject()) {
                            JsonObject obj = array.get(i).getAsJsonObject();
                            String id = obj.has("id") ? obj.get("id").getAsString() : "";
                            String name = obj.has("name") ? obj.get("name").getAsString() : "";
                            if (!id.isEmpty()) {
                                loadedTools.add(new ToolEntry(id, name));
                            }
                        }
                    }
                }
                tools = loadedTools;
                LOGGER.info("Loaded {} tools from satisfactory_wheel.json", tools.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load satisfactory_wheel.json config: ", e);
        }
    }

    private static void generateTemplate(Path path) {
        try {
            Files.createDirectories(path.getParent());
            JsonObject template = new JsonObject();
            template.addProperty("hold_to_select", true);
            template.addProperty("wheel_radius", 95);

            JsonArray toolsArray = new JsonArray();
            JsonObject example1 = new JsonObject();
            example1.addProperty("id", "minecraft:compass");
            example1.addProperty("name", "Пример (Компас)");
            toolsArray.add(example1);

            JsonObject example2 = new JsonObject();
            example2.addProperty("id", "minecraft:clock");
            example2.addProperty("name", "Пример (Часы)");
            toolsArray.add(example2);

            template.add("tools", toolsArray);

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(template, writer);
            }
            LOGGER.info("Template config created at {}", path);
        } catch (Exception e) {
            LOGGER.error("Failed to generate default template config: ", e);
        }
    }
}

