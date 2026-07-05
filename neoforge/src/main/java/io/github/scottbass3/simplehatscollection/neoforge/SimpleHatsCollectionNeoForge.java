package io.github.scottbass3.simplehatscollection.neoforge;

import io.github.scottbass3.simplehatscollection.Constants;
import io.github.scottbass3.simplehatscollection.client.ClientCollectionData;
import io.github.scottbass3.simplehatscollection.core.CollectionManager;
import io.github.scottbass3.simplehatscollection.core.Platform;
import io.github.scottbass3.simplehatscollection.data.HatCollection;
import io.github.scottbass3.simplehatscollection.hat.BagSupport;
import io.github.scottbass3.simplehatscollection.net.DepositInventoryHatPayload;
import io.github.scottbass3.simplehatscollection.net.RequestSyncPayload;
import io.github.scottbass3.simplehatscollection.net.ServerPayloadHandler;
import io.github.scottbass3.simplehatscollection.net.SyncCollectionPayload;
import io.github.scottbass3.simplehatscollection.net.WithdrawHatPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public final class SimpleHatsCollectionNeoForge {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final Supplier<AttachmentType<HatCollection>> COLLECTION = ATTACHMENTS.register(
            "collection",
            () -> AttachmentType.builder(HatCollection::new)
                    .serialize(HatCollection.CODEC)
                    .copyOnDeath()
                    .build());

    public SimpleHatsCollectionNeoForge(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        modBus.addListener(this::registerPayloads);

        Platform.GET_COLLECTION = player -> player.getData(COLLECTION.get());
        Platform.SET_COLLECTION = (player, collection) -> player.setData(COLLECTION.get(), collection);
        Platform.SYNC = (player, collection) ->
                PacketDistributor.sendToPlayer(player, new SyncCollectionPayload(collection));

        NeoForge.EVENT_BUS.addListener(this::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(this::onLogin);
        NeoForge.EVENT_BUS.addListener(this::onRespawn);
        NeoForge.EVENT_BUS.addListener(this::onChangeDimension);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            SimpleHatsCollectionNeoForgeClient.init(modBus);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(DepositInventoryHatPayload.TYPE, DepositInventoryHatPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ServerPayloadHandler.deposit((ServerPlayer) context.player(), payload.slotId())));
        registrar.playToServer(WithdrawHatPayload.TYPE, WithdrawHatPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ServerPayloadHandler.withdraw((ServerPlayer) context.player(), payload.hat())));
        registrar.playToServer(RequestSyncPayload.TYPE, RequestSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ServerPayloadHandler.requestSync((ServerPlayer) context.player())));
        registrar.playToClient(SyncCollectionPayload.TYPE, SyncCollectionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ClientCollectionData.set(payload.collection())));
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!player.isShiftKeyDown() || !BagSupport.isBag(stack)) return;

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (CollectionManager.depositBagRoll(serverPlayer, stack)) {
                stack.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) CollectionManager.sync(serverPlayer);
    }

    private void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) CollectionManager.sync(serverPlayer);
    }

    private void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) CollectionManager.sync(serverPlayer);
    }
}
