package io.github.scottbass3.simplehatscollection.core;

import io.github.scottbass3.simplehatscollection.data.HatCollection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Loader-agnostic seams filled in by each loader's entrypoint. Using simple function
 * holders (set once at mod construction) avoids a ServiceLoader layer and keeps
 * client-only networking out of common/server code paths.
 */
public final class Platform {

    private Platform() {}

    /** Server: read the player's persisted collection (loader data attachment). */
    public static Function<Player, HatCollection> GET_COLLECTION = p -> new HatCollection();

    /** Server: write the player's collection back (marks the attachment dirty). */
    public static BiConsumer<Player, HatCollection> SET_COLLECTION = (p, c) -> {};

    /** Server: push the full collection to the owning client. */
    public static BiConsumer<ServerPlayer, HatCollection> SYNC = (p, c) -> {};

    /** Client: send a payload to the server. */
    public static Consumer<CustomPacketPayload> SEND_TO_SERVER = p -> {};
}
