package io.github.scottbass3.simplehatscollection.hat;

import fonnymunkey.simplehats.SimpleHatsCommon;
import fonnymunkey.simplehats.common.item.HatItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Thin bridge over the SimpleHats API for identifying and enumerating hats.
 * The hat list is populated by SimpleHats during mod init on both client and server,
 * so it is safe to read once the game is running.
 */
public final class HatSupport {

    private HatSupport() {}

    public static boolean isHat(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HatItem;
    }

    /** All hats registered by SimpleHats (includes the "special" hat). */
    public static List<HatItem> allHats() {
        return SimpleHatsCommon.MOD_REGISTRY.getHatList();
    }

    public static HatItem getHatByName(String name) {
        for (HatItem hat : allHats()) {
            if (hat.getHatEntry().getHatName().equals(name)) return hat;
        }
        return null;
    }
}
