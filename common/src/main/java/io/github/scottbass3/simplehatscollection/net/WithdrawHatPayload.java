package io.github.scottbass3.simplehatscollection.net;

import io.github.scottbass3.simplehatscollection.Constants;
import io.github.scottbass3.simplehatscollection.data.StoredHat;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S: withdraw one hat of the given identity back into the player's inventory. */
public record WithdrawHatPayload(StoredHat hat) implements CustomPacketPayload {

    public static final Type<WithdrawHatPayload> TYPE =
            new Type<>(Constants.id("withdraw_hat"));

    public static final StreamCodec<ByteBuf, WithdrawHatPayload> STREAM_CODEC =
            StoredHat.STREAM_CODEC.map(WithdrawHatPayload::new, WithdrawHatPayload::hat);

    @Override
    public Type<WithdrawHatPayload> type() {
        return TYPE;
    }
}
