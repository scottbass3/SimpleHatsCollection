package io.github.scottbass3.simplehatscollection.client;

import io.github.scottbass3.simplehatscollection.data.HatCollection;

/** Client-side cache of the player's collection, refreshed by the S2C sync payload. */
public final class ClientCollectionData {

    private static HatCollection current = new HatCollection();

    private ClientCollectionData() {}

    public static void set(HatCollection collection) {
        current = collection;
    }

    public static HatCollection get() {
        return current;
    }
}
