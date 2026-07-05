package io.github.scottbass3.simplehatscollection.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Shared keybind instances; each loader registers {@link #OPEN} with its own API. */
public final class CollectionKeybinds {

    public static final String CATEGORY = "key.categories.simplehatscollection";

    public static final KeyMapping OPEN = new KeyMapping(
            "key.simplehatscollection.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY);

    private CollectionKeybinds() {}
}
