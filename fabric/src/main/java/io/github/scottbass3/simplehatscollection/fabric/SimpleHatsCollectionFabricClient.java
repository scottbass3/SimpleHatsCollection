package io.github.scottbass3.simplehatscollection.fabric;

import io.github.scottbass3.simplehatscollection.client.ClientCollectionData;
import io.github.scottbass3.simplehatscollection.client.CollectionKeybinds;
import io.github.scottbass3.simplehatscollection.client.CollectionScreen;
import io.github.scottbass3.simplehatscollection.client.InventoryDepositHandler;
import io.github.scottbass3.simplehatscollection.core.Platform;
import io.github.scottbass3.simplehatscollection.net.SyncCollectionPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class SimpleHatsCollectionFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Platform.SEND_TO_SERVER = ClientPlayNetworking::send;

        KeyBindingHelper.registerKeyBinding(CollectionKeybinds.OPEN);

        ClientPlayNetworking.registerGlobalReceiver(SyncCollectionPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientCollectionData.set(payload.collection())));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (CollectionKeybinds.OPEN.consumeClick()) {
                if (client.screen == null && client.player != null) {
                    client.setScreen(new CollectionScreen());
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?>) {
                ScreenMouseEvents.allowMouseClick(screen).register((clickedScreen, mouseX, mouseY, button) ->
                        !InventoryDepositHandler.tryDeposit(clickedScreen, button));
            }
        });
    }
}
