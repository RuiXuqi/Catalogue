package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.Catalogue;
import com.cleanroommc.catalogue.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class CatalogueIconButton extends CatalogueTextButton {
    public static final ResourceLocation ICON_TEXTURE = Catalogue.resource("textures/gui/icons.png");
    private final int u, v;

    public CatalogueIconButton(int x, int y, int u, int v, @Nullable Consumer<CatalogueTextButton> onPress) {
        this(x, y, u, v, 20, "", onPress);
    }

    public CatalogueIconButton(int x, int y, int u, int v, int width, int height, @Nullable Consumer<CatalogueTextButton> onPress) {
        this(x, y, u, v, width, height, "", onPress);
    }

    public CatalogueIconButton(int x, int y, int u, int v, int width, String label, @Nullable Consumer<CatalogueTextButton> onPress) {
        this(x, y, u, v, width, 20, label, onPress);
    }

    public CatalogueIconButton(int x, int y, int u, int v, int width, int height, String label, @Nullable Consumer<CatalogueTextButton> onPress) {
        super(x, y, width, height, label, onPress);
        this.u = u;
        this.v = v;
    }

    @Override
    protected void renderContents(Minecraft mc, int mouseX, int mouseY) {
        FontRenderer font = mc.fontRenderer;
        int contentWidth = 10 + font.getStringWidth(this.displayString) + (!this.displayString.isEmpty() ? 4 : 0);
        int iconX = this.xPosition + (this.width - contentWidth) / 2;
        int iconY = this.yPosition + (this.height - 10) / 2;

        mc.getTextureManager().bindTexture(ICON_TEXTURE);
        float brightness = this.enabled ? 1.0F : 0.5F;
        GL11.glColor4f(brightness, brightness, brightness, 1.0F);
        RenderUtils.drawModalRectWithCustomSizedTexture(iconX, iconY, this.u, this.v, 10, 10, 64, 64);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawString(font, this.displayString, iconX + 14, iconY + 1, this.getFGColor());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
