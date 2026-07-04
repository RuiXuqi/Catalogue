package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.Catalogue;
import com.cleanroommc.catalogue.client.data.IModData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

public enum ImageType {
    ICON("icon", IModData::getImageIcon) {
        @Override
        protected boolean validate(IModData data, BufferedImage image) {
            if (image.getWidth() != image.getHeight()) {
                Catalogue.LOG.error("Invalid icon image for mod '{}': image must be a square", data.getModId());
                return true;
            }
            return false;
        }
    },
    BANNER("banner", IModData::getBanner) {
        @Override
        protected BufferedImage readImage(IModData data, String resource) throws IOException {
            IResourcePack resourcePack = data.getResourcePack();
            if (resourcePack != null && !resource.startsWith("/")) {
                return resourcePack.getPackImage();
            }
            return super.readImage(data, resource);
        }
    },
    BACKGROUND("background", IModData::getBackground);

    private final String type;
    private final Function<IModData, String> resourceLocator;

    ImageType(String type, Function<IModData, String> resourceLocator) {
        this.type = type;
        this.resourceLocator = resourceLocator;
    }

    public Optional<ImageInfo> load(IModData data) {
        String resource = this.resourceLocator.apply(data);
        if (StringUtils.isBlank(resource)) return Optional.empty();

        try {
            BufferedImage image = this.readImage(data, resource);
            if (image == null) {
                Catalogue.LOG.error("Unable to locate the {} image resource '{}' for mod '{}'", this.type, resource, data.getModId());
                return Optional.empty();
            }
            return this.validate(data, image) ? Optional.of(this.registerTexture(data, image)) : Optional.empty();
        } catch (IOException e) {
            Catalogue.LOG.error("An error occurred when loading the {} image resource '{}' for mod '{}'", this.type, resource, data.getModId(), e);
            return Optional.empty();
        }
    }

    @Nullable
    protected BufferedImage readImage(IModData data, String resource) throws IOException {
        String normalizedResource = resource.startsWith("/") ? resource : "/" + resource;
        try (InputStream is = this.getClass().getResourceAsStream(normalizedResource)) {
            return is != null ? TextureUtil.readBufferedImage(is) : null;
        }
    }

    protected boolean validate(IModData data, BufferedImage image) {
        return true;
    }

    private ImageInfo registerTexture(IModData data, BufferedImage image) {
        ResourceLocation id = Catalogue.resource(String.format("%s/%s", this.type, data.getModId()));
        final TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        manager.loadTexture(id, new DynamicTexture(image));
        return new ImageInfo(id, image.getWidth(), image.getHeight(), () -> manager.deleteTexture(id));
    }
}
