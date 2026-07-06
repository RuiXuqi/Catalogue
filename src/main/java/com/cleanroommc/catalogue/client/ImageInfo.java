package com.cleanroommc.catalogue.client;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.util.ResourceLocation;

/**
 * Author: MrCrayfish
 */
@Desugar
public record ImageInfo(ResourceLocation resource, int width, int height, Runnable unregister) {
    public static final Runnable EMPTY_UNREGISTER = () -> {
    };

    public ImageInfo(ResourceLocation resource, int width, int height) {
        this(resource, width, height, EMPTY_UNREGISTER);
    }
}
