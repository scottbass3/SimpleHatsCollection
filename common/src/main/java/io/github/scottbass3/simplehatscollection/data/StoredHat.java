package io.github.scottbass3.simplehatscollection.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fonnymunkey.simplehats.common.item.HatItem;
import io.github.scottbass3.simplehatscollection.hat.HatSupport;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Identity key of a single collectible hat: the SimpleHats hat name, its visual
 * variant (CUSTOM_MODEL_DATA), and its dye colour (DYED_COLOR) when dyed.
 * Two hats with the same three values stack together in the collection.
 */
public record StoredHat(String hatName, int variant, boolean dyed, int dyeColor) {

    public static final Codec<StoredHat> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("hat").forGetter(StoredHat::hatName),
            Codec.INT.optionalFieldOf("variant", 0).forGetter(StoredHat::variant),
            Codec.BOOL.optionalFieldOf("dyed", false).forGetter(StoredHat::dyed),
            Codec.INT.optionalFieldOf("dyeColor", 0).forGetter(StoredHat::dyeColor)
    ).apply(inst, StoredHat::new));

    public static final StreamCodec<ByteBuf, StoredHat> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StoredHat::hatName,
            ByteBufCodecs.VAR_INT, StoredHat::variant,
            ByteBufCodecs.BOOL, StoredHat::dyed,
            ByteBufCodecs.INT, StoredHat::dyeColor,
            StoredHat::new
    );

    /** Derive the identity of a hat ItemStack. Caller must ensure it is a hat. */
    public static StoredHat fromStack(ItemStack stack) {
        HatItem hat = (HatItem) stack.getItem();
        String name = hat.getHatEntry().getHatName();
        int variant = stack.has(DataComponents.CUSTOM_MODEL_DATA)
                ? stack.get(DataComponents.CUSTOM_MODEL_DATA).value()
                : 0;
        boolean dyed = stack.has(DataComponents.DYED_COLOR);
        int color = dyed ? stack.get(DataComponents.DYED_COLOR).rgb() : 0;
        return new StoredHat(name, variant, dyed, color);
    }

    /** Rebuild a fresh ItemStack for this identity, or EMPTY if the hat is no longer registered. */
    public ItemStack toStack() {
        HatItem item = HatSupport.getHatByName(hatName);
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        if (variant > 0) stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(variant));
        if (dyed) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(dyeColor, true));
        return stack;
    }
}
