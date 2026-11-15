package com.satisfactory_wheel.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EquipToolPayload(String toolId) implements CustomPacketPayload {
    public static final Type<EquipToolPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("satisfactory_wheel", "equip_tool"));

    public static final StreamCodec<ByteBuf, EquipToolPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        EquipToolPayload::toolId,
        EquipToolPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

