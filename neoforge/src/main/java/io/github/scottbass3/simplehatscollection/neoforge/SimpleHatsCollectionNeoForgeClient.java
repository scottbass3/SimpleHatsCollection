package io.github.scottbass3.simplehatscollection.neoforge;

import io.github.scottbass3.simplehatscollection.client.CollectionKeybinds;
import io.github.scottbass3.simplehatscollection.client.CollectionScreen;
import io.github.scottbass3.simplehatscollection.client.InventoryDepositHandler;
import io.github.scottbass3.simplehatscollection.core.Platform;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

final class SimpleHatsCollectionNeoForgeClient {

    private SimpleHatsCollectionNeoForgeClient() {}

    static void init(IEventBus modBus) {
        Platform.SEND_TO_SERVER = PacketDistributor::sendToServer;
        modBus.addListener(SimpleHatsCollectionNeoForgeClient::onRegisterKeys);
        NeoForge.EVENT_BUS.addListener(SimpleHatsCollectionNeoForgeClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(SimpleHatsCollectionNeoForgeClient::onScreenMouse);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(CollectionKeybinds.OPEN);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (CollectionKeybinds.OPEN.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                mc.setScreen(new CollectionScreen());
            }
        }
    }

    private static void onScreenMouse(ScreenEvent.MouseButtonPressed.Pre event) {
        if (InventoryDepositHandler.tryDeposit(event.getScreen(), event.getButton())) {
            event.setCanceled(true);
        }
    }
}
