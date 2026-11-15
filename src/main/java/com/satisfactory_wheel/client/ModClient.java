package com.satisfactory_wheel.client;

import com.satisfactory_wheel.ModConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ModClient {
    public static final KeyMapping OPEN_KEY = new KeyMapping(
        "key.satisfactory_wheel.open",
        GLFW.GLFW_KEY_R,
        "key.categories.satisfactory_wheel"
    );

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (ModConfig.isHoldToSelect()) {
            if (OPEN_KEY.isDown() && mc.screen == null) {
                mc.setScreen(new ToolWheelScreen());
            }
        } else {
            while (OPEN_KEY.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new ToolWheelScreen());
                }
            }
        }
    }
}

