package io.github.scottbass3.simplehatscollection.net;

import io.github.scottbass3.simplehatscollection.Constants;
import io.github.scottbass3.simplehatscollection.data.HatCollection;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** S2C: full snapshot of the player's collection. */
public record SyncCollectionPayload(HatCollection collection) implements CustomPacketPayload {

    public static final Type<SyncCollectionPayload> TYPE =
            new Type<>(Constants.id("sync_collection"));

    public static final StreamCodec<ByteBuf, SyncCollectionPayload> STREAM_CODEC =
            HatCollection.STREAM_CODEC.map(SyncCollectionPayload::new, SyncCollectionPayload::collection);

    @Override
    public Type<SyncCollectionPayload> type() {
        return TYPE;
    }
}
