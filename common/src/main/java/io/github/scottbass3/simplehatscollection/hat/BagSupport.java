package io.github.scottbass3.simplehatscollection.hat;

import fonnymunkey.simplehats.SimpleHatsCommon;
import fonnymunkey.simplehats.common.init.IModRegistry;
import fonnymunkey.simplehats.common.item.BagItem;
import fonnymunkey.simplehats.common.item.HatItem;
import fonnymunkey.simplehats.util.HatEntry.HatSeason;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.List;

/**
 * Reimplements SimpleHats' {@link BagItem} loot selection using only the public API,
 * so the addon can roll a random hat when a bag is shift-used into the collection.
 * SimpleHats' own selection lives in private fields, hence this parallel implementation
 * (it mirrors BagItem#getBagResult: filter by rarity/season with weight &gt; 0, then weighted pick).
 */
public final class BagSupport {

    private BagSupport() {}

    private record BagKind(Rarity rarity, HatSeason season, boolean seasonal) {}

    public static boolean isBag(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BagItem;
    }

    private static BagKind classify(ItemStack stack) {
        if (!(stack.getItem() instanceof BagItem bag)) return null;
        IModRegistry r = SimpleHatsCommon.MOD_REGISTRY;
        if (bag == r.getHatBagCommon()) return new BagKind(Rarity.COMMON, HatSeason.NONE, false);
        if (bag == r.getHatBagUncommon()) return new BagKind(Rarity.UNCOMMON, HatSeason.NONE, false);
        if (bag == r.getHatBagRare()) return new BagKind(Rarity.RARE, HatSeason.NONE, false);
        if (bag == r.getHatBagEpic()) return new BagKind(Rarity.EPIC, HatSeason.NONE, false);
        if (bag == r.getHatBagEaster()) return new BagKind(Rarity.EPIC, HatSeason.EASTER, true);
        if (bag == r.getHatBagSummer()) return new BagKind(Rarity.EPIC, HatSeason.SUMMER, true);
        if (bag == r.getHatBagHalloween()) return new BagKind(Rarity.EPIC, HatSeason.HALLOWEEN, true);
        if (bag == r.getHatBagFestive()) return new BagKind(Rarity.EPIC, HatSeason.FESTIVE, true);
        return null;
    }

    /**
     * Roll a random base hat ItemStack for the given bag, or EMPTY if the bag is unknown
     * or has no eligible hats. Matches SimpleHats' eligibility rules and weighting.
     */
    public static ItemStack rollBagResult(ItemStack bagStack, RandomSource random) {
        BagKind kind = classify(bagStack);
        if (kind == null) return ItemStack.EMPTY;

        List<HatItem> eligible = new ArrayList<>();
        long totalWeight = 0;
        for (HatItem hat : HatSupport.allHats()) {
            int weight = hat.getHatEntry().getHatWeight();
            boolean rarityOk = kind.seasonal() || hat.getHatEntry().getHatRarity() == kind.rarity();
            boolean seasonOk = hat.getHatEntry().getHatSeason() == kind.season();
            if (weight > 0 && rarityOk && seasonOk) {
                eligible.add(hat);
                totalWeight += weight;
            }
        }
        if (eligible.isEmpty() || totalWeight <= 0) return ItemStack.EMPTY;

        long roll = (long) Math.floor(random.nextDouble() * totalWeight);
        for (HatItem hat : eligible) {
            roll -= hat.getHatEntry().getHatWeight();
            if (roll < 0) return new ItemStack(hat);
        }
        // Fallback (shouldn't happen): return the last eligible hat.
        return new ItemStack(eligible.get(eligible.size() - 1));
    }
}
