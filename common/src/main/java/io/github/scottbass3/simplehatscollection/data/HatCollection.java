package io.github.scottbass3.simplehatscollection.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A player's hat collection: an unbounded multiset of {@link StoredHat} identities,
 * each with a stored count. Behaves like an infinite chest — depositing the same hat
 * twice increments its counter, withdrawing decrements it.
 */
public final class HatCollection {

    /** A single (hat identity, count) pair, used for serialization. */
    public record Counted(StoredHat hat, int count) {
        public static final Codec<Counted> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                StoredHat.CODEC.fieldOf("hat").forGetter(Counted::hat),
                Codec.INT.fieldOf("count").forGetter(Counted::count)
        ).apply(inst, Counted::new));

        public static final StreamCodec<ByteBuf, Counted> STREAM_CODEC = StreamCodec.composite(
                StoredHat.STREAM_CODEC, Counted::hat,
                ByteBufCodecs.VAR_INT, Counted::count,
                Counted::new
        );
    }

    public static final Codec<HatCollection> CODEC = Counted.CODEC.listOf()
            .xmap(HatCollection::fromCounted, HatCollection::toCounted);

    public static final StreamCodec<ByteBuf, HatCollection> STREAM_CODEC =
            Counted.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(HatCollection::fromCounted, HatCollection::toCounted);

    private final Map<StoredHat, Integer> entries;

    public HatCollection() {
        this.entries = new LinkedHashMap<>();
    }

    private HatCollection(Map<StoredHat, Integer> entries) {
        this.entries = entries;
    }

    public static HatCollection fromCounted(List<Counted> list) {
        Map<StoredHat, Integer> map = new LinkedHashMap<>();
        for (Counted c : list) {
            if (c.count() > 0) map.merge(c.hat(), c.count(), Integer::sum);
        }
        return new HatCollection(map);
    }

    public List<Counted> toCounted() {
        List<Counted> list = new ArrayList<>(entries.size());
        entries.forEach((hat, count) -> list.add(new Counted(hat, count)));
        return list;
    }

    public void add(StoredHat hat) {
        add(hat, 1);
    }

    public void add(StoredHat hat, int amount) {
        if (amount <= 0) return;
        entries.merge(hat, amount, Integer::sum);
    }

    /** Remove one of the given hat. Returns true if one was present and removed. */
    public boolean remove(StoredHat hat) {
        Integer current = entries.get(hat);
        if (current == null || current <= 0) return false;
        if (current == 1) entries.remove(hat);
        else entries.put(hat, current - 1);
        return true;
    }

    public int count(StoredHat hat) {
        return entries.getOrDefault(hat, 0);
    }

    /** Total number of stored hats sharing the given SimpleHats hat name (all variants/dyes). */
    public int totalForName(String hatName) {
        int total = 0;
        for (Map.Entry<StoredHat, Integer> e : entries.entrySet()) {
            if (e.getKey().hatName().equals(hatName)) total += e.getValue();
        }
        return total;
    }

    /** All distinct stored variants for a given hat name, in insertion order. */
    public List<Counted> variantsForName(String hatName) {
        List<Counted> list = new ArrayList<>();
        for (Map.Entry<StoredHat, Integer> e : entries.entrySet()) {
            if (e.getKey().hatName().equals(hatName)) list.add(new Counted(e.getKey(), e.getValue()));
        }
        return list;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public HatCollection copy() {
        return new HatCollection(new LinkedHashMap<>(entries));
    }
}
