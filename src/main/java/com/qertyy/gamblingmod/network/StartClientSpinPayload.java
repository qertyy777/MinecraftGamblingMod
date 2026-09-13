package com.qertyy.gamblingmod.network;

import com.qertyy.gamblingmod.screen.RouletteManager;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StartClientSpinPayload(long seed, double targetSpeedX) implements CustomPacketPayload {
    public static final Type<StartClientSpinPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("gamblingmod", "start_client_spin"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, StartClientSpinPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, StartClientSpinPayload::seed,
            ByteBufCodecs.DOUBLE, StartClientSpinPayload::targetSpeedX,
            StartClientSpinPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final StartClientSpinPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> RouletteManager.getInstance().startClientSpin(payload.seed(), payload.targetSpeedX()));
    }
}
