package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.Catalogue;
import com.cleanroommc.catalogue.client.data.ForgeModData;
import com.cleanroommc.catalogue.client.data.IModData;
import com.cleanroommc.catalogue.client.data.OptiFineModData;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public final class PlatformUtils {
    private PlatformUtils() {
    }

    public static List<IModData> getAllModData() {
        ArrayList<ModContainer> containerList = new ArrayList<>();
        FMLClientHandler.instance().addSpecialModEntries(containerList);
        containerList.addAll(Loader.instance().getModList());

        linkChildMods(containerList);

        List<IModData> dataList = new ArrayList<>(containerList.size());
        for (ModContainer container : containerList) {
            if (container == null) continue;
            if ("optifine".equals(container.getModId())) {
                dataList.add(new OptiFineModData(container));
                continue;
            }
            dataList.add(new ForgeModData(container));
        }
        return dataList;
    }

    private static void linkChildMods(List<ModContainer> containerList) {
        for (ModContainer container : containerList) {
            if (container == null) continue;
            ModMetadata metadata = container.getMetadata();
            if (metadata != null && metadata.parentMod == null && StringUtils.isNotBlank(metadata.parent)) {
                ModContainer parentContainer = Loader.instance().getIndexedModList().get(metadata.parent);
                ModMetadata parentMetadata = parentContainer != null ? parentContainer.getMetadata() : null;
                if (parentMetadata != null) {
                    metadata.parentMod = parentContainer;
                    if (!parentMetadata.childMods.contains(container)) {
                        parentMetadata.childMods.add(container);
                    }
                }
            }
        }
    }

    public static File getModDirectory() {
        return Loader.instance().getConfigDir().getParentFile().toPath().resolve("mods").toFile();
    }

    public static Path getConfigDirectory() {
        return Loader.instance().getConfigDir().toPath();
    }

    /// {@link net.minecraft.client.gui.GuiScreenResourcePacks#actionPerformed(GuiButton)}
    /// Works properly with java 25.
    @SuppressWarnings("JavadocReference")
    public static void openFile(File file) {
        String absolutePath = file.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX) {
            try {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", absolutePath});
                return;
            } catch (Exception e) {
                Catalogue.LOG.error("Failed to open '{}' via exec", absolutePath, e);
            }
        } else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            String openCommand = String.format("cmd.exe /C start \"Open file\" \"%s\"", absolutePath);
            try {
                Runtime.getRuntime().exec(openCommand);
                return;
            } catch (IOException e) {
                Catalogue.LOG.error("Failed to open '{}' via exec", absolutePath, e);
            }
        }

        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
            oclass.getMethod("open", File.class).invoke(object, file);
            return;
        } catch (Exception e) {
            Catalogue.LOG.error("Failed to open '{}' via java.awt.Desktop", absolutePath, e);
        }

        try {
            Sys.openURL("file://" + absolutePath);
        } catch (Exception e) {
            Catalogue.LOG.error("Failed to open '{}' via org.lwjgl.Sys", absolutePath, e);
        }
    }

    /**
     * Open uri directly using Desktop API, with lwjgl3ify fallback.
     *
     * @param uri the URI instance to open
     */
    public static void openURI(URI uri) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
            oclass.getMethod("browse", URI.class).invoke(object, uri);
            return;
        } catch (Exception e) {
            Catalogue.LOG.error("Failed to open '{}' via java.awt.Desktop", uri, e);
        }

        try {
            Class<?> desktopCls = Class.forName("me.eigenraven.lwjgl3ify.redirects.Desktop");
            Object desktop = desktopCls.getMethod("getDesktop").invoke(null);
            desktopCls.getMethod("browse", URI.class).invoke(desktop, uri);
            return;
        } catch (Exception e) {
            Catalogue.LOG.error("Failed to open '{}' via me.eigenraven.lwjgl3ify.redirects.Desktop", uri, e);
        }

        try {
            Class<?> sysX = Class.forName("org.lwjglx.Sys");
            if (Boolean.TRUE.equals(sysX.getMethod("openURL", String.class)
                    .invoke(null, uri.toString())
            )) return;
        } catch (Exception e) {
            Catalogue.LOG.error("Failed to open '{}' via org.lwjglx.Sys", uri, e);
        }

        try {
            Sys.openURL(uri.toString());
        } catch (Exception e) {
            Catalogue.LOG.error("Failed to open '{}' via org.lwjgl.Sys", uri, e);
        }
    }

    public static boolean isKeyCombo(int keyCode, int comboKeyCode) {
        return keyCode == comboKeyCode && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    public static boolean hasGTNHLibConfig(String modId) {
        try {
            Class<?> managerClass = Class.forName("com.gtnewhorizon.gtnhlib.config.ConfigurationManager");
            return Boolean.TRUE.equals(managerClass.getMethod("isModRegistered", String.class)
                    .invoke(null, modId));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    public static void openGTNHLibConfigScreen(Minecraft minecraft, GuiScreen parent, String modId, String modName) {
        try {
            Class<?> guiClass = Class.forName("com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig");
            GuiScreen configScreen = (GuiScreen) guiClass
                    .getConstructor(GuiScreen.class, String.class, String.class)
                    .newInstance(parent, modId, modName);
            minecraft.displayGuiScreen(configScreen);
        } catch (ReflectiveOperationException | LinkageError e) {
            Catalogue.LOG.error("Failed to build GTNHLib config GUI for mod '{}'", modId, e);
        }
    }
}
