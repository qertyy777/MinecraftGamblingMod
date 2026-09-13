package com.qertyy.gamblingmod.network;

import com.qertyy.gamblingmod.screen.RouletteManager;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpgradeResultPayload(boolean success) implements CustomPacketPayload {
    public static final Type<UpgradeResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("gamblingmod", "upgrade_result"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, UpgradeResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UpgradeResultPayload::success,
            UpgradeResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final UpgradeResultPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> RouletteManager.getInstance().onClientUpgradeResult(payload.success()));
    }
}
