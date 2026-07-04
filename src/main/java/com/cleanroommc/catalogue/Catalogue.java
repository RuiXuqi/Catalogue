package com.cleanroommc.catalogue;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: MrCrayfish
 */
@Mod(
        modid = CatalogueConstants.MOD_ID,
        name = CatalogueConstants.MOD_NAME,
        version = CatalogueConstants.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        customProperties = {
                @Mod.CustomProperty(k = "license", v = "MIT"),
                @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/RuiXuqi/Catalogue-Vintage/issues"),
                @Mod.CustomProperty(k = "iconFile", v = "assets/catalogue/icon.png"),
                @Mod.CustomProperty(k = "backgroundFile", v = "assets/catalogue/background.png")
        }
)
public class Catalogue {
    public static final Logger LOG = LogManager.getLogger(CatalogueConstants.MOD_NAME);

    public static ResourceLocation resource(String name) {
        return new ResourceLocation(CatalogueConstants.MOD_ID, name);
    }
}
