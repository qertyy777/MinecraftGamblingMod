package com.qertyy.gamblingmod.network;

import com.qertyy.gamblingmod.screen.RouletteManager;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestUpgradePayload(int inputIndex, int outputIndex) implements CustomPacketPayload {
    public static final Type<RequestUpgradePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("gamblingmod", "request_upgrade"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, RequestUpgradePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestUpgradePayload::inputIndex,
            ByteBufCodecs.VAR_INT, RequestUpgradePayload::outputIndex,
            RequestUpgradePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final RequestUpgradePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                RouletteManager.getInstance().processServerUpgrade(serverPlayer, payload.inputIndex(), payload.outputIndex());
            }
        });
    }
}
