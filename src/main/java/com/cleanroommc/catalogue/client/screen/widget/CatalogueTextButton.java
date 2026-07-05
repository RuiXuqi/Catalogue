package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.Catalogue;
import com.cleanroommc.catalogue.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CatalogueTextButton extends GuiButton {
    private static final ResourceLocation BUTTON = Catalogue.resource("textures/gui/sprites/widget/button.png");
    private static final ResourceLocation BUTTON_DISABLED = Catalogue.resource("textures/gui/sprites/widget/button_disabled.png");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = Catalogue.resource("textures/gui/sprites/widget/button_highlighted.png");
    private static final RenderUtils.NineSlice BUTTON_SLICE = new RenderUtils.NineSlice(200, 20, 3);
    private final @Nullable Consumer<CatalogueTextButton> onPress;

    public CatalogueTextButton(int x, int y, int width, int height, String text, @Nullable Consumer<CatalogueTextButton> onPress) {
        super(-1, x, y, width, height, text);
        this.onPress = onPress;
    }

    public void onClick() {
        if (this.onPress != null) this.onPress.accept(this);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;
        this.field_146123_n = RenderUtils.isMouseWithin(this.xPosition, this.yPosition, this.width, this.height, mouseX, mouseY);
        this.renderBackground(mc, mouseX, mouseY);
        this.mouseDragged(mc, mouseX, mouseY);
        this.renderContents(mc, mouseX, mouseY);
    }

    @SuppressWarnings("unused")
    protected void renderBackground(Minecraft mc, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        mc.getTextureManager().bindTexture(this.enabled ? (this.field_146123_n ? BUTTON_HIGHLIGHTED : BUTTON) : BUTTON_DISABLED);
        RenderUtils.blitNineSlicedSprite(BUTTON_SLICE, this.xPosition, this.yPosition, this.width, this.height);
    }

    @SuppressWarnings("unused")
    protected void renderContents(Minecraft mc, int mouseX, int mouseY) {
        this.renderScrollingString(mc.fontRenderer, 2, this.getFGColor());
    }

    @SuppressWarnings("SameParameterValue")
    protected void renderScrollingString(FontRenderer font, int xBorder, int color) {
        int boxLeft = this.xPosition + xBorder;
        int boxRight = this.xPosition + this.width - xBorder;
        RenderUtils.drawScrollingString(font, this.displayString, boxLeft, this.yPosition, boxRight, this.yPosition + this.height, color, true);
    }

    protected int getFGColor() {
        if (this.packedFGColour != 0) {
            return this.packedFGColour;
        } else if (!this.enabled) {
            return 0xA0A0A0;
        } else if (this.field_146123_n) {
            return 0xFFFFA0;
        } else {
            return 0xE0E0E0;
        }
    }
}
