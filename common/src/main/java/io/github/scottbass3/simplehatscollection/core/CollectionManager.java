package io.github.scottbass3.simplehatscollection.core;

import io.github.scottbass3.simplehatscollection.data.HatCollection;
import io.github.scottbass3.simplehatscollection.data.StoredHat;
import io.github.scottbass3.simplehatscollection.hat.BagSupport;
import io.github.scottbass3.simplehatscollection.hat.HatSupport;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Server-authoritative operations on a player's collection. Every mutation persists
 * via {@link Platform#SET_COLLECTION} and re-syncs the client via {@link Platform#SYNC}.
 */
public final class CollectionManager {

    private CollectionManager() {}

    public static HatCollection get(ServerPlayer player) {
        return Platform.GET_COLLECTION.apply(player);
    }

    public static void sync(ServerPlayer player) {
        Platform.SYNC.accept(player, get(player));
    }

    /** Deposit a hat ItemStack (consumed by the caller). Returns false if it isn't a hat. */
    public static boolean deposit(ServerPlayer player, ItemStack hatStack) {
        if (!HatSupport.isHat(hatStack)) return false;
        HatCollection collection = get(player);
        StoredHat stored = StoredHat.fromStack(hatStack);
        collection.add(stored);
        Platform.SET_COLLECTION.accept(player, collection);
        Platform.SYNC.accept(player, collection);
        player.displayClientMessage(
                Component.translatable("message.simplehatscollection.deposited", hatStack.getHoverName()), true);
        return true;
    }

    /**
     * Deposit the hat rolled from a bag directly into the collection.
     * Returns true if a hat was rolled and stored.
     */
    public static boolean depositBagRoll(ServerPlayer player, ItemStack bagStack) {
        ItemStack rolled = BagSupport.rollBagResult(bagStack, player.getRandom());
        if (rolled.isEmpty()) return false;
        return deposit(player, rolled);
    }

    /** Withdraw one hat of the given identity back into the player's inventory. */
    public static void withdraw(ServerPlayer player, StoredHat stored) {
        HatCollection collection = get(player);
        if (!collection.remove(stored)) return;
        Platform.SET_COLLECTION.accept(player, collection);

        ItemStack stack = stored.toStack();
        if (!stack.isEmpty() && !player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        Platform.SYNC.accept(player, collection);
    }
}
