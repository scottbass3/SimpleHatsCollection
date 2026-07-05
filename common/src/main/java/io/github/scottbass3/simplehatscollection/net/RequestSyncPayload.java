package io.github.scottbass3.simplehatscollection.net;

import io.github.scottbass3.simplehatscollection.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S: ask the server to (re)send the collection, e.g. when opening the screen. */
public record RequestSyncPayload() implements CustomPacketPayload {

    public static final RequestSyncPayload INSTANCE = new RequestSyncPayload();

    public static final Type<RequestSyncPayload> TYPE =
            new Type<>(Constants.id("request_sync"));

    public static final StreamCodec<ByteBuf, RequestSyncPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<RequestSyncPayload> type() {
        return TYPE;
    }
}
