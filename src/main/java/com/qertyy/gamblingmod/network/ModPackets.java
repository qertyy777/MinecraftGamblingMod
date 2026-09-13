package com.qertyy.gamblingmod.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.qertyy.gamblingmod.GamblingMod;

@EventBusSubscriber(modid = GamblingMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModPackets {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                RequestSpinPayload.TYPE,
                RequestSpinPayload.STREAM_CODEC,
                RequestSpinPayload::handle
        );

        registrar.playToClient(
                StartClientSpinPayload.TYPE,
                StartClientSpinPayload.STREAM_CODEC,
                StartClientSpinPayload::handle
        );

        registrar.playToServer(
                RequestUpgradePayload.TYPE,
                RequestUpgradePayload.STREAM_CODEC,
                RequestUpgradePayload::handle
        );

        registrar.playToClient(
                UpgradeResultPayload.TYPE,
                UpgradeResultPayload.STREAM_CODEC,
                UpgradeResultPayload::handle
        );
    }
}
