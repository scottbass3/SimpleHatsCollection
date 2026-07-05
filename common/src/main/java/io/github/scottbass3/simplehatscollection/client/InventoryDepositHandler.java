package io.github.scottbass3.simplehatscollection.client;

import io.github.scottbass3.simplehatscollection.core.Platform;
import io.github.scottbass3.simplehatscollection.hat.HatSupport;
import io.github.scottbass3.simplehatscollection.net.DepositInventoryHatPayload;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Shared logic for the "shift + right-click a hovered hat" deposit gesture.
 * Loader code wires this into its screen mouse event and cancels the vanilla action
 * when this returns true.
 */
public final class InventoryDepositHandler {

    private InventoryDepositHandler() {}

    /** @return true if the gesture matched and a deposit request was sent (cancel vanilla). */
    public static boolean tryDeposit(Screen screen, int button) {
        if (button != 1 || !Screen.hasShiftDown()) return false;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return false;

        Slot slot = containerScreen.hoveredSlot;
        if (slot == null || !slot.hasItem()) return false;

        ItemStack stack = slot.getItem();
        if (!HatSupport.isHat(stack)) return false;

        int slotId = containerScreen.getMenu().slots.indexOf(slot);
        if (slotId < 0) return false;

        Platform.SEND_TO_SERVER.accept(new DepositInventoryHatPayload(slotId));
        return true;
    }
}
