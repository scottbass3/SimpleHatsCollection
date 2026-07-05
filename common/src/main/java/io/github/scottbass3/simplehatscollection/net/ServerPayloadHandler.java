package io.github.scottbass3.simplehatscollection.net;

import io.github.scottbass3.simplehatscollection.core.CollectionManager;
import io.github.scottbass3.simplehatscollection.data.StoredHat;
import io.github.scottbass3.simplehatscollection.hat.HatSupport;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side handling of incoming payloads. Loader receivers extract the payload and
 * player, then dispatch here so the logic stays shared. Always invoked on the server thread.
 */
public final class ServerPayloadHandler {

    private ServerPayloadHandler() {}

    public static void deposit(ServerPlayer player, int slotId) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || slotId < 0 || slotId >= menu.slots.size()) return;

        Slot slot = menu.slots.get(slotId);
        if (!slot.mayPickup(player)) return;

        ItemStack stack = slot.getItem();
        if (!HatSupport.isHat(stack)) return;

        // Hats always stack to 1; deposit the whole slot.
        if (CollectionManager.deposit(player, stack.copy())) {
            slot.set(ItemStack.EMPTY);
            slot.setChanged();
            menu.broadcastChanges();
        }
    }

    public static void withdraw(ServerPlayer player, StoredHat hat) {
        CollectionManager.withdraw(player, hat);
    }

    public static void requestSync(ServerPlayer player) {
        CollectionManager.sync(player);
    }
}
