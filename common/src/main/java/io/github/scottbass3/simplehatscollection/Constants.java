package io.github.scottbass3.simplehatscollection;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {

    public static final String MOD_ID = "simplehatscollection";
    public static final String MOD_NAME = "SimpleHatsCollection";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Constants() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
