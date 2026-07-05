package com.cleanroommc.catalogue;

import com.cleanroommc.catalogue.client.ClientEventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: MrCrayfish
 */
@Mod(
        modid = CatalogueConstants.MOD_ID,
        name = CatalogueConstants.MOD_NAME,
        version = CatalogueConstants.VERSION,
        acceptableRemoteVersions = "*",
        guiFactory = "com.cleanroommc.catalogue.CatalogueConfigGuiFactory",
        customProperties = {
                @Mod.CustomProperty(k = "license", v = "MIT"),
                @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/RuiXuqi/Catalogue-Vintage/issues"),
                @Mod.CustomProperty(k = "iconFile", v = "assets/catalogue/icon.png"),
                @Mod.CustomProperty(k = "backgroundFile", v = "assets/catalogue/background.png")
        }
)
public class Catalogue {
    public static final Logger LOG = LogManager.getLogger(CatalogueConstants.MOD_NAME);

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!event.getSide().isClient()) return;
        CatalogueConfig.init(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        FMLCommonHandler.instance().bus().register(new ClientEventHandler());
    }

    public static ResourceLocation resource(String name) {
        return new ResourceLocation(CatalogueConstants.MOD_ID, name);
    }
}
