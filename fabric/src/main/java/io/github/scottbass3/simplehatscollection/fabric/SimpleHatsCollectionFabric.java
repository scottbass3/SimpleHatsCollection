package io.github.scottbass3.simplehatscollection.fabric;

import io.github.scottbass3.simplehatscollection.Constants;
import io.github.scottbass3.simplehatscollection.core.CollectionManager;
import io.github.scottbass3.simplehatscollection.core.Platform;
import io.github.scottbass3.simplehatscollection.data.HatCollection;
import io.github.scottbass3.simplehatscollection.hat.BagSupport;
import io.github.scottbass3.simplehatscollection.net.DepositInventoryHatPayload;
import io.github.scottbass3.simplehatscollection.net.RequestSyncPayload;
import io.github.scottbass3.simplehatscollection.net.ServerPayloadHandler;
import io.github.scottbass3.simplehatscollection.net.SyncCollectionPayload;
import io.github.scottbass3.simplehatscollection.net.WithdrawHatPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

public final class SimpleHatsCollectionFabric implements ModInitializer {

    public static final AttachmentType<HatCollection> COLLECTION = AttachmentRegistry.<HatCollection>builder()
            .initializer(HatCollection::new)
            .persistent(HatCollection.CODEC)
            .copyOnDeath()
            .buildAndRegister(Constants.id("collection"));

    @Override
    public void onInitialize() {
        registerPayloadTypes();

        Platform.GET_COLLECTION = player -> player.getAttachedOrCreate(COLLECTION);
        Platform.SET_COLLECTION = (player, collection) -> player.setAttached(COLLECTION, collection);
        Platform.SYNC = (player, collection) ->
                ServerPlayNetworking.send(player, new SyncCollectionPayload(collection));

        registerServerReceivers();
        registerEvents();
    }

    private static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(DepositInventoryHatPayload.TYPE, DepositInventoryHatPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(WithdrawHatPayload.TYPE, WithdrawHatPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestSyncPayload.TYPE, RequestSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncCollectionPayload.TYPE, SyncCollectionPayload.STREAM_CODEC);
    }

    private static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(DepositInventoryHatPayload.TYPE, (payload, context) ->
                onServer(context.player(), () -> ServerPayloadHandler.deposit(context.player(), payload.slotId())));
        ServerPlayNetworking.registerGlobalReceiver(WithdrawHatPayload.TYPE, (payload, context) ->
                onServer(context.player(), () -> ServerPayloadHandler.withdraw(context.player(), payload.hat())));
        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, (payload, context) ->
                onServer(context.player(), () -> ServerPayloadHandler.requestSync(context.player())));
    }

    private static void onServer(ServerPlayer player, Runnable task) {
        MinecraftServer server = player.getServer();
        if (server != null) server.execute(task);
    }

    private static void registerEvents() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && BagSupport.isBag(stack)) {
                if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    if (CollectionManager.depositBagRoll(serverPlayer, stack)) {
                        stack.shrink(1);
                    }
                }
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                CollectionManager.sync(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                CollectionManager.sync(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                CollectionManager.sync(player));
    }
}
