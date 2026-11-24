package io.github.scottbass3.simplehatscollection.client;

import io.github.scottbass3.simplehatscollection.core.Platform;
import io.github.scottbass3.simplehatscollection.hat.HatSupport;
import io.github.scottbass3.simplehatscollection.net.DepositInventoryHatPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
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

        int slotId = serverSlotId(containerScreen, slot);
        if (slotId < 0) return false;

        Platform.SEND_TO_SERVER.accept(new DepositInventoryHatPayload(slotId));
        return true;
    }

    private static int serverSlotId(AbstractContainerScreen<?> screen, Slot slot) {
        if (!(screen instanceof CreativeModeInventoryScreen)) {
            return screen.getMenu().slots.indexOf(slot);
        }

        // The creative menu only exists client-side; the server still has the player's
        // inventory menu open, whose slot order differs. Match by inventory index instead.
        // Slots of the creative item grid belong to no real inventory and are skipped.
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || slot.container != player.getInventory()) return -1;

        for (Slot target : player.inventoryMenu.slots) {
            if (target.container == slot.container && target.getContainerSlot() == slot.getContainerSlot()) {
                return target.index;
            }
        }
        return -1;
    }
}
