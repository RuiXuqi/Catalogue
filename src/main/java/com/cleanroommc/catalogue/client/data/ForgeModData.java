package com.cleanroommc.catalogue.client.data;

import com.cleanroommc.catalogue.Catalogue;
import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.client.RenderUtils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourcePack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ForgeModData implements IModData {
//    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation("forge", "textures/gui/version_check_icons.png");
    private static final List<String> LIB_MODS = Arrays.asList(CatalogueConfig.libraryList);
    private static final List<String> IGNORED_DEPENDENCIES = Arrays.asList(CatalogueConfig.ignoredDependenciesList);
    private static final Map<String, Map<String, String>> CUSTOM_MOD_INFO = parseCustomModInfo();

    private static Map<String, Map<String, String>> parseCustomModInfo() {
        Map<String, Map<String, String>> result = new HashMap<>();
        if (CatalogueConfig.customModInfo == null) return result;
        for (String entry : CatalogueConfig.customModInfo) {
            int colon = entry.indexOf(':');
            if (colon <= 0) continue;
            String modId = entry.substring(0, colon);
            String content = entry.substring(colon + 1);
            Map<String, String> fieldMap = new HashMap<>();
            for (String field : content.split(",")) {
                int equal = field.indexOf('=');
                if (equal > 0) {
                    fieldMap.put(field.substring(0, equal), field.substring(equal + 1));
                }
            }
            result.put(modId, fieldMap);
        }
        return result;
    }

    private final ModContainer info;
    private final @Nullable ModMetadata metadata;
    private final Type type;
    private final Set<String> dependencies;
    private final Set<String> childMods;
    private final String modId;

    public ForgeModData(ModContainer info) {
        this.info = info;
        this.metadata = info.getMetadata();
        this.type = this.analyzeType(info);
        this.dependencies = analyzeDependencies(info);
        this.childMods = analyzeChildMods(info);
        this.modId = this.info.getModId();
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public String getDisplayName() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("name")) {
            return custom.get("name");
        }
        return this.info.getName();
    }

    @Override
    public String getVersion() {
        return this.info.getDisplayVersion();
    }

    @Override
    public String getInnerVersion() {
        return this.info.getVersion();
    }

    @Nullable
    @Override
    public String getDescription() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("description")) {
            return custom.get("description");
        }
        return this.metadata != null ? this.metadata.description : null;
    }

    @Nullable
    @Override
    public String getItemIcon() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("iconItem")) {
            return custom.get("iconItem");
        }
        Map<String,String> props = this.info.getCustomModProperties();
        return props != null ? props.get("iconItem") : null;
    }

    @Nullable
    @Override
    public String getImageIcon() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null) {
            if (custom.containsKey("iconFile")) return custom.get("iconFile");
            // Make customized iconItem replace the old one
            if (custom.containsKey("iconItem")) return null;
        }
        Map<String,String> props = this.info.getCustomModProperties();
        return props != null ? props.get("iconFile") : null;
    }

    @Nullable
    @Override
    public String getLicense() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("license")) {
            return custom.get("license");
        }
        Map<String,String> props = this.info.getCustomModProperties();
        return props != null ? props.get("license") : null;
    }

    @Nullable
    @Override
    public String getCredits() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("credits")) {
            return custom.get("credits");
        }
        return this.metadata != null ? this.metadata.credits : null;
    }

    @Nullable
    @Override
    public String getAuthors() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("authors")) {
            return custom.get("authors");
        }
        return this.metadata != null ? this.metadata.getAuthorList() : null;
    }

    @Nullable
    @Override
    public String getHomepage() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("url")) {
            return custom.get("url");
        }
        return this.metadata != null ? this.metadata.url : null;
    }

    @Nullable
    @Override
    public String getIssueTracker() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("issueTrackerUrl")) {
            return custom.get("issueTrackerUrl");
        }
        return this.info.getCustomModProperties().get("issueTrackerUrl");
    }

    @Nullable
    @Override
    public String getBanner() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("logoFile")) {
            return custom.get("logoFile");
        }
        return this.metadata != null ? this.metadata.logoFile : null;
    }

    @Nullable
    @Override
    public String getBackground() {
        Map<String, String> custom = CUSTOM_MOD_INFO.get(this.getModId());
        if (custom != null && custom.containsKey("backgroundFile")) {
            return custom.get("backgroundFile");
        }
        Map<String,String> props = this.info.getCustomModProperties();
        return props != null ? props.get("backgroundFile") : null;
    }

    @Nullable
    @Override
    public String getChildModNames() {
        return this.metadata != null ? this.metadata.getChildModList() : null;
    }

    @Nullable
    @Override
    public String getParentModName() {
        return this.metadata != null && this.metadata.parentMod != null ? this.metadata.parentMod.getName() : null;
    }

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public Set<String> getChildMods() {
        return this.childMods;
    }

    @Override
    public boolean hasConfig() {
        IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
        return (guiFactory != null && guiFactory.mainConfigGuiClass() != null) || this.hasGtnhLibConfig();
    }

    private boolean hasGtnhLibConfig() {
        try {
            Class<?> managerClass = Class.forName("com.gtnewhorizon.gtnhlib.config.ConfigurationManager");
            return Boolean.TRUE.equals(managerClass.getMethod("isModRegistered", String.class)
                    .invoke(null, this.getModId()));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        try {
            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
            if (guiFactory != null && guiFactory.mainConfigGuiClass() != null) {
                GuiScreen newScreen;
                try {
                    newScreen = guiFactory.mainConfigGuiClass().getConstructor(GuiScreen.class)
                            .newInstance(parent);
                } catch (NoSuchMethodException e) {
                    newScreen = (GuiScreen) guiFactory.getClass().getMethod("createConfigGui", GuiScreen.class)
                            .invoke(guiFactory, parent);
                }
                minecraft.displayGuiScreen(newScreen);
                return;
            }
        } catch (Exception e) {
            Catalogue.LOG.error("There was a critical issue trying to build the config GUI for {}", this.getModId());
        }
        GuiScreen gtnhLibScreen = this.createGtnhLibConfigScreen(parent);
        if (gtnhLibScreen != null) {
            minecraft.displayGuiScreen(gtnhLibScreen);
            //return;
        }
    }

    @Nullable
    private GuiScreen createGtnhLibConfigScreen(GuiScreen parent) {
        if (!this.hasGtnhLibConfig()) return null;

        try {
            Class<?> guiClass = Class.forName("com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig");
            return (GuiScreen) guiClass.getConstructor(GuiScreen.class, String.class, String.class)
                    .newInstance(parent, this.getModId(), this.getDisplayName());
        } catch (ReflectiveOperationException | LinkageError e) {
            Catalogue.LOG.error("Failed to create GTNHLib config GUI for {}", this.getModId(), e);
            return null;
        }
    }

    @Nullable
    @Override
    public CheckResult getCheckResult() {
//        ForgeVersion.CheckResult result = ForgeVersion.getResult(this.info);
//        if (result.status.shouldDraw()) {
//            return new CheckResult(
//                    result.status == ForgeVersion.Status.OUTDATED || result.status == ForgeVersion.Status.BETA_OUTDATED,
//                    result.status.isAnimated(),
//                    result.status.getSheetOffset(),
//                    VERSION_CHECK_ICONS,
//                    result.target != null ? result.target.toString() : null,
//                    result.url
//            );
//        }
        return null;
    }

    @Override
    public void drawCheckIcon(Minecraft minecraft, CheckResult result, int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        int vOffset = result.animated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
        minecraft.getTextureManager().bindTexture(result.textures());
        RenderUtils.drawModalRectWithCustomSizedTexture(x, y, result.texOffset() * 8, vOffset, 8, 8, 64, 16);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Nullable
    @Override
    public String getCheckText(CheckResult update) {
//        ForgeVersion.CheckResult result = ForgeVersion.getResult(this.info);
//        if (result == null) return null;
//
//        boolean hasPage = StringUtils.isNotBlank(update.url());
//        return switch (result.status) {
//            case BETA -> TextFormatting.GOLD + I18n.format("catalogue.gui.beta");
//            case AHEAD -> TextFormatting.LIGHT_PURPLE + I18n.format("catalogue.gui.ahead", update.latestFound());
//            case BETA_OUTDATED -> TextFormatting.GOLD + (hasPage ?
//                    I18n.format("catalogue.gui.beta_update_available", update.latestFound(), update.url()) :
//                    I18n.format("catalogue.gui.beta_update_available_no_page", update.latestFound()));
//            case OUTDATED -> TextFormatting.GREEN + (hasPage ?
//                    I18n.format("catalogue.gui.update_available", update.latestFound(), update.url()) :
//                    I18n.format("catalogue.gui.update_available_no_page", update.latestFound()));
//            default -> null;
//        };
        return null;
    }

    @Nullable
    @Override
    public IResourcePack getResourcePack() {
        return FMLClientHandler.instance().getResourcePackFor(this.getModId());
    }

    private Type analyzeType(ModContainer info) {
        if (this.metadata != null && this.metadata.parentMod != null) {
            return Type.CHILD;
        } else if (LIB_MODS.contains(info.getModId())) {
            return Type.LIBRARY;
        } else {
            return Type.DEFAULT;
        }
    }

    private static Set<String> analyzeDependencies(ModContainer source) {
        List<? extends ArtifactVersion> versions = source.getDependencies();
        return versions.stream()
                .map(ArtifactVersion::getLabel)
                .filter(modid -> !IGNORED_DEPENDENCIES.contains(modid))
                .collect(Collectors.toSet());
    }

    private static Set<String> analyzeChildMods(ModContainer source) {
        ModMetadata metadata = source.getMetadata();
        if (metadata == null) return Collections.emptySet();
        return metadata.childMods.stream()
                .filter(Objects::nonNull)
                .map(ModContainer::getModId)
                .collect(Collectors.toSet());
    }
}
