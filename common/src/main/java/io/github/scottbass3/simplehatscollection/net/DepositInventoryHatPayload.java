package io.github.scottbass3.simplehatscollection.net;

import io.github.scottbass3.simplehatscollection.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S: deposit the hat currently in the given container-menu slot into the collection. */
public record DepositInventoryHatPayload(int slotId) implements CustomPacketPayload {

    public static final Type<DepositInventoryHatPayload> TYPE =
            new Type<>(Constants.id("deposit_inventory_hat"));

    public static final StreamCodec<ByteBuf, DepositInventoryHatPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(DepositInventoryHatPayload::new, DepositInventoryHatPayload::slotId);

    @Override
    public Type<DepositInventoryHatPayload> type() {
        return TYPE;
    }
}
