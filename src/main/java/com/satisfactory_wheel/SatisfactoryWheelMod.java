package com.satisfactory_wheel;

import com.mojang.brigadier.CommandDispatcher;
import com.satisfactory_wheel.client.ModClient;
import com.satisfactory_wheel.network.EquipToolPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(SatisfactoryWheelMod.MOD_ID)
public class SatisfactoryWheelMod {
    public static final String MOD_ID = "satisfactory_wheel";

    public SatisfactoryWheelMod(IEventBus modEventBus) {
        // Load config from disk (or generate template if absent)
        ModConfig.load();

        // Register custom packet payloads
        modEventBus.addListener(this::registerPayloads);

        // Register server / game events on NeoForge EVENT_BUS
        NeoForge.EVENT_BUS.register(VirtualToolServerManager.class);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        // Client-only setup
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ModClient::registerKeyMappings);
            NeoForge.EVENT_BUS.register(ModClient.class);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID).versioned("1.0.0").optional();
        registrar.playToServer(
            EquipToolPayload.TYPE,
            EquipToolPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                    VirtualToolServerManager.handleEquip(player, payload.toolId());
                }
            })
        );
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("toolwheel")
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    ModConfig.load();
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.satisfactory_wheel.reloaded"), true);
                    return 1;
                })
            )
        );
    }
}

