package com.cleanroommc.catalogue;

import cpw.mods.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class CatalogueConfig {
    private static final Map<String, Object> DEFAULT_VALUES = new HashMap<>();
    private static Configuration CONFIG;

    private static final String[] CUSTOM_MOD_INFO_FIELDS = new String[]{
            "name", "description", "url", "issueTrackerUrl",
            "logoFile", "iconFile", "iconItem", "backgroundFile", "license", "credits"
    };
    private static final Pattern CUSTOM_MOD_INFO_PATTERN = Pattern.compile(
            "^[^:]+:(" + String.join("|", CUSTOM_MOD_INFO_FIELDS) + ")=[^,]*(,(" + String.join("|", CUSTOM_MOD_INFO_FIELDS) + ")=[^,]*)*$"
    );

    public static boolean enable = true;
    public static String[] libraryList = new String[]{
            "Forge",
            "FML",
            "mcp",
            "gtnhlib",
            "gtnhextlib",
            "hodgepodge",
            "unimixins",
            "lwjgl3ify"
    };
    public static String[] ignoredDependenciesList = new String[]{
            "minecraft",
            "Forge",
            "FML",
            "mcp"
    };
    public static String[] forceDefaultIconList = new String[]{};
    public static String[] customModInfo = new String[]{};

    static void init(File configFile) {
        if (CONFIG != null) throw new IllegalStateException("Init have been performed!");
        CONFIG = new Configuration(configFile);
        CONFIG.load();
        buildConfig();
    }

    public static void buildConfig() {
        enable = CONFIG.get(
                Configuration.CATEGORY_GENERAL,
                "enable",
                loadDefault(Configuration.CATEGORY_GENERAL, "enable", enable),
                "Whether enable Catalogue."
                        + "\nSetting it false will stop Catalogue redirecting Forge's mod list calls."
        ).setLanguageKey("catalogue.config.enable").getBoolean();

        libraryList = CONFIG.get(
                Configuration.CATEGORY_GENERAL,
                "libraryList",
                loadDefault(Configuration.CATEGORY_GENERAL, "libraryList", libraryList),
                "The list of library mods' mod ids."
                        + "\nThey will have grey names in the mod list."
        ).setRequiresMcRestart(true).setLanguageKey("catalogue.config.library_list").getStringList();

        ignoredDependenciesList = CONFIG.get(
                Configuration.CATEGORY_GENERAL,
                "ignoredDependenciesList",
                loadDefault(Configuration.CATEGORY_GENERAL, "ignoredDependenciesList", ignoredDependenciesList),
                "The list of ignored dependencies' mod ids."
                        + "\nThey will not be displayed when searching for dependencies/dependants."
        ).setRequiresMcRestart(true).setLanguageKey("catalogue.config.ignored_dependencies_list").getStringList();

        forceDefaultIconList = CONFIG.get(
                Configuration.CATEGORY_GENERAL,
                "forceDefaultIconList",
                loadDefault(Configuration.CATEGORY_GENERAL, "forceDefaultIconList", forceDefaultIconList),
                "The list of mod ids that should always use the default item icon."
                        + "\nThey will not have random-picked item icons to avoid crashes."
        ).setRequiresMcRestart(true).setLanguageKey("catalogue.config.force_default_icon_list").getStringList();

        customModInfo = CONFIG.get(
                Configuration.CATEGORY_GENERAL,
                "customModInfo",
                loadDefault(Configuration.CATEGORY_GENERAL, "customModInfo", customModInfo),
                "Custom mod info entries. \nFormat: modid:field1=value,field2=value2 \nAvailable fields: name, description, url, issueTrackerUrl, logoFile, iconFile, iconItem, backgroundFile, license, credits"
        ).setRequiresMcRestart(true).setLanguageKey("catalogue.config.custom_mod_info").setValidationPattern(CUSTOM_MOD_INFO_PATTERN).getStringList();

        if (CONFIG.hasChanged()) CONFIG.save();
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static <T> T loadDefault(String category, String name, T currentValue) {
        return (T) DEFAULT_VALUES.computeIfAbsent(category + Configuration.CATEGORY_SPLITTER + name, k -> currentValue);
    }

    @SuppressWarnings("rawtypes")
    @Nonnull
    static List<IConfigElement> getRootElement() {
        return new ConfigElement<>(CONFIG.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements();
    }
}
