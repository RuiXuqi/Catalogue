package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CatalogueListExtended<E extends GuiListExtended.IGuiListEntry> extends GuiListExtended {
    public boolean visible = true;
    protected boolean scrollBarVisible;
    private boolean scrolling;

    public CatalogueListExtended(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    // Values renamed by deepseek. Comments are handwrite.
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Customized background. Empty by default.
        this.drawBackground();

        this.bindAmountScrolled();
        int maxScroll = this.func_148135_f();
        this.scrollBarVisible = this.shouldShowScrollBar();

        RenderUtils.scissor(this.left, this.top, this.width, this.bottom - this.top);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tess = Tessellator.instance;

        // Shadowed dirt background. Scroll with the entries.
        this.drawContainerBackground(tess);

        // Customized header. Empty by default
        if (this.hasListHeader) this.drawListHeader(this.left + this.getListLeft(), this.getListTop(), tess);

        this.renderListItems(mouseX, mouseY, partialTicks, tess);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Scroll Bar
        if (this.scrollBarVisible) this.drawScrollBar(maxScroll, tess);

        // Customized decorations. Empty by default.
        this.func_148142_b(mouseX, mouseY);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawScrollBar(int maxScroll, Tessellator tess) {
        int scrollThumbHeight = this.getScrollThumbHeight();
        if (scrollThumbHeight <= 0) return;

        int viewHeight = this.bottom - this.top;
        int scrollThumbTop = (int) this.amountScrolled * (viewHeight - scrollThumbHeight) / maxScroll + this.top;
        scrollThumbTop = MathHelper.clamp_int(scrollThumbTop, this.top, this.bottom - scrollThumbHeight);

        int scrollBarLeft = this.getScrollBarLeft();
        int scrollBarRight = this.getScrollBarRight();
        tess.startDrawingQuads();

        // Background
        tess.setColorRGBA(0, 0, 0, 255);
        tess.addVertexWithUV(scrollBarLeft, this.bottom, 0, 0, 1);
        tess.setColorRGBA(0, 0, 0, 255);
        tess.addVertexWithUV(scrollBarRight, this.bottom, 0, 1, 1);
        tess.setColorRGBA(0, 0, 0, 255);
        tess.addVertexWithUV(scrollBarRight, this.top, 0, 1, 0);
        tess.setColorRGBA(0, 0, 0, 255);
        tess.addVertexWithUV(scrollBarLeft, this.top, 0, 0, 0);

        // Main
        tess.setColorRGBA(128, 128, 128, 255);
        tess.addVertexWithUV(scrollBarLeft, scrollThumbTop + scrollThumbHeight, 0, 0, 1);
        tess.setColorRGBA(128, 128, 128, 255);
        tess.addVertexWithUV(scrollBarRight, scrollThumbTop + scrollThumbHeight, 0, 1, 1);
        tess.setColorRGBA(128, 128, 128, 255);
        tess.addVertexWithUV(scrollBarRight, scrollThumbTop, 0, 1, 0);
        tess.setColorRGBA(128, 128, 128, 255);
        tess.addVertexWithUV(scrollBarLeft, scrollThumbTop, 0, 0, 0);

        // Border
        tess.setColorRGBA(192, 192, 192, 255);
        tess.addVertexWithUV(scrollBarLeft, scrollThumbTop + scrollThumbHeight - 1, 0, 0, 1);
        tess.setColorRGBA(192, 192, 192, 255);
        tess.addVertexWithUV(scrollBarRight - 1, scrollThumbTop + scrollThumbHeight - 1, 0, 1, 1);
        tess.setColorRGBA(192, 192, 192, 255);
        tess.addVertexWithUV(scrollBarRight - 1, scrollThumbTop, 0, 1, 0);
        tess.setColorRGBA(192, 192, 192, 255);
        tess.addVertexWithUV(scrollBarLeft, scrollThumbTop, 0, 0, 0);

        tess.draw();
    }

    protected void renderListItems(int mouseX, int mouseY, float partialTicks, Tessellator tess) {
        for (int index = 0; index < this.getSize(); ++index) {
            int rowLeft = this.left + this.getListLeft();
            int rowRight = this.left + this.getListRight();
            int rowTop = this.getRowTop(index);
            int rowBottom = this.getRowBottom(index) - 4;

            if (rowTop + this.slotHeight >= this.top && rowTop <= this.bottom) {
                this.renderItem(index, rowLeft, rowTop, rowRight, rowBottom, mouseX, mouseY, partialTicks, tess);
            }
        }
    }

    protected void renderItem(int slotIndex, int rowLeft, int rowTop, int rowRight, int rowBottom, int mouseX, int mouseY, float partialTicks, Tessellator tess) {
        this.drawSlot(slotIndex, rowLeft, rowTop, rowBottom - rowTop, tess, mouseX, mouseY);
    }

    public void handleMouseInput() {
        if (!this.visible) {
            this.initialClickY = -1;
            this.scrolling = false;
            return;
        }

        boolean hasScrollBar = this.shouldShowScrollBar();
        boolean mouseOverList = this.isMouseWithinListBounds(this.mouseX, this.mouseY);

        if (mouseOverList) {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                int listLeft = this.left + this.getListLeft();
                int listRight = this.left + this.getListRight();
                boolean beforeScrollBar = this.isMouseBeforeScrollBar(hasScrollBar, this.mouseX);

                int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                int slotIndex = relativeY / this.slotHeight;

                if (beforeScrollBar && slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                    this.elementClicked(slotIndex, false, this.mouseX, this.mouseY);
                    this.selectedElement = slotIndex;
                } else if (beforeScrollBar && this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                    this.func_148132_a(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                }
            }
        }

        if (Mouse.isButtonDown(0) && this.func_148125_i()) {
            if (this.initialClickY == -1) {
                if (mouseOverList) {
                    boolean clickedOnHeader = false;

                    int listLeft = this.left + this.getListLeft();
                    int listRight = this.left + this.getListRight();
                    int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                    int slotIndex = relativeY / this.slotHeight;
                    boolean beforeScrollBar = this.isMouseBeforeScrollBar(hasScrollBar, this.mouseX);

                    if (beforeScrollBar && slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                        boolean isDoubleClick = slotIndex == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                        this.elementClicked(slotIndex, isDoubleClick, this.mouseX, this.mouseY);
                        this.selectedElement = slotIndex;
                        this.lastClicked = Minecraft.getSystemTime();
                    } else if (beforeScrollBar && this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                        this.func_148132_a(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                        clickedOnHeader = true;
                    }

                    this.scrolling = !clickedOnHeader && this.isMouseOverScrollBar(hasScrollBar, this.mouseX);
                    if (this.scrolling) {
                        this.initialClickY = this.mouseY;
                    } else {
                        this.initialClickY = -2;
                    }
                } else {
                    this.scrolling = false;
                    this.initialClickY = -2;
                }
            } else if (this.initialClickY >= 0 && this.scrolling) {
                if (this.mouseY < this.top) {
                    this.setAmountScrolled(0.0F);
                } else if (this.mouseY > this.bottom) {
                    this.setAmountScrolled(this.func_148135_f());
                } else {
                    int maxScroll = Math.max(1, this.func_148135_f());
                    int viewHeight = this.bottom - this.top;
                    int scrollThumbHeight = this.getScrollThumbHeight();
                    int scrollRange = viewHeight - scrollThumbHeight;

                    this.scrollMultiplier = scrollRange > 0 ? Math.max(1.0F, (float) maxScroll / (float) scrollRange) : 0.0F;
                    this.setAmountScrolled(this.amountScrolled + (this.mouseY - this.initialClickY) * this.scrollMultiplier);
                }

                this.initialClickY = this.mouseY;
            } else {
                this.initialClickY = -2;
            }
        } else {
            this.initialClickY = -1;
            this.scrolling = false;
        }

        int wheelDelta = Mouse.getEventDWheel();

        if (wheelDelta != 0 && mouseOverList) {
            wheelDelta = wheelDelta > 0 ? -1 : 1;
            this.setAmountScrolled(this.amountScrolled + (float) (wheelDelta * this.slotHeight / 2));
        }
    }

    // mouseClicked
    @Override
    public boolean func_148179_a(int mouseX, int mouseY, int mouseButton) {
        if (!this.visible) return false;

        int slotIndex = this.func_148124_c(mouseX, mouseY);
        if (slotIndex >= 0) {
            int rowLeft = this.left + this.getListLeft();
            int rowTop = this.getRowTop(slotIndex);
            int relativeX = mouseX - rowLeft;
            int relativeY = mouseY - rowTop;
            if (this.getListEntry(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseButton, relativeX, relativeY)) {
                this.func_148143_b(false);
                return true;
            }
        }
        return false;
    }

    // mouseReleased
    @Override
    public boolean func_148181_b(int mouseX, int mouseY, int mouseButton) {
        if (!this.visible) {
            this.func_148143_b(true);
            return false;
        }

        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex) {
            int rowLeft = this.left + this.getListLeft();
            int rowTop = this.getRowTop(slotIndex);
            int relativeX = mouseX - rowLeft;
            int relativeY = mouseY - rowTop;
            this.getListEntry(slotIndex).mouseReleased(slotIndex, mouseX, mouseY, mouseButton, relativeX, relativeY);
        }
        this.func_148143_b(true);
        return false;
    }

    // getSlotIndexFromScreenCoords
    @Override
    public int func_148124_c(int mouseX, int mouseY) {
        if (!this.isMouseWithinListBounds(mouseX, mouseY)) return -1;

        boolean hasScrollBar = this.shouldShowScrollBar();
        int listLeft = this.left + this.getListLeft();
        int listRight = this.left + this.getListRight();
        int relativeY = MathHelper.floor_float(mouseY - this.top) - this.headerPadding + this.getAmountScrolled() - 4;
        int slotIndex = relativeY / this.slotHeight;
        boolean beforeScrollBar = this.isMouseBeforeScrollBar(hasScrollBar, mouseX);
        return beforeScrollBar && mouseX >= listLeft && mouseX <= listRight && slotIndex >= 0 && relativeY >= 0 && slotIndex < this.getSize() ? slotIndex : -1;
    }

    public boolean isMouseWithinListBounds(int mouseX, int mouseY) {
        return mouseX >= this.left && mouseX <= this.left + this.width && this.func_148141_e(mouseY);
    }

    private boolean shouldShowScrollBar() {
        return this.func_148135_f() > 0 && this.getContentHeight() > 0 && this.bottom > this.top;
    }

    private boolean isMouseOverScrollBar(boolean hasScrollBar, int mouseX) {
        return hasScrollBar && mouseX >= this.getScrollBarLeft() && mouseX < this.getScrollBarRight();
    }

    private boolean isMouseBeforeScrollBar(boolean hasScrollBar, int mouseX) {
        return !hasScrollBar || mouseX < this.getScrollBarLeft();
    }

    private int getScrollBarLeft() {
        return this.left + this.getScrollBarX();
    }

    private int getScrollBarRight() {
        return this.getScrollBarLeft() + 6;
    }

    public void setClampedAmountScrolled(float scroll) {
        this.amountScrolled = MathHelper.clamp_float(scroll, 0.0F, this.func_148135_f());
    }

    public void setAmountScrolled(float scroll) {
        this.setClampedAmountScrolled(scroll);
    }

    public void clampAmountScrolled() {
        this.setClampedAmountScrolled(this.getAmountScrolled());
    }

    public void setWidth(int width) {
        this.width = width;
        this.right = this.left + this.width;
    }

    public void setHeight(int height) {
        this.height = height;
        this.bottom = this.top + height;
    }

    protected int getScrollThumbHeight() {
        int viewHeight = this.bottom - this.top;
        int contentHeight = this.getContentHeight();
        if (viewHeight <= 0 || contentHeight <= 0) return 0;

        int thumbHeight = viewHeight * viewHeight / contentHeight;
        int maxThumbHeight = Math.max(1, viewHeight - 8);
        int minThumbHeight = Math.min(32, maxThumbHeight);
        return MathHelper.clamp_int(thumbHeight, minThumbHeight, maxThumbHeight);
    }

    /**
     * Returns the scrollbar x-coordinate relative to this list's left bound.
     */
    @Override
    protected int getScrollBarX() {
        return super.getScrollBarX();
    }

    /**
     * Returns the row content width in screen pixels. This is a size, not an x-coordinate.
     */
    @Override
    public int getListWidth() {
        return super.getListWidth();
    }

    /**
     * Returns the row content left edge relative to this list's left bound.
     */
    protected int getListLeft() {
        return this.width / 2 - this.getListWidth() / 2 + 2;
    }

    /**
     * Returns the row content right edge relative to this list's left bound.
     */
    protected int getListRight() {
        return this.getListLeft() + this.getListWidth();
    }

    /**
     * Returns the absolute screen y-coordinate where list content starts after scroll offset.
     */
    protected int getListTop() {
        return this.top + 4 - (int) this.amountScrolled;
    }

    /**
     * Returns the absolute screen y-coordinate of a row's top edge.
     */
    protected int getRowTop(int slotIndex) {
        return this.top + 4 - (int) this.amountScrolled + slotIndex * this.slotHeight + this.headerPadding;
    }

    /**
     * Returns the absolute screen y-coordinate of a row's bottom edge.
     */
    protected int getRowBottom(int slotIndex) {
        return this.getRowTop(slotIndex) + this.slotHeight;
    }

    /*
    Some helpers.
     */

    private final List<E> children = new ArrayList<>();

    public final List<E> children() {
        return this.children;
    }

    @Nonnull
    @Override
    public E getListEntry(int slotIndex) {
        return this.children().get(slotIndex);
    }

    @Override
    protected int getSize() {
        return this.children().size();
    }

    public void centerScrollOn(E entry) {
        this.setAmountScrolled((float) (this.children().indexOf(entry) * this.slotHeight + this.slotHeight / 2 - (this.bottom - this.top) / 2));
    }

    public void addEntry(E entry) {
        this.children().add(entry);
    }

    public void clearEntries() {
        this.children().clear();
    }

    public void replaceEntries(Collection<E> entries) {
        this.clearEntries();
        this.children().addAll(entries);
    }

    public void removeEntries(List<E> entries) {
        entries.forEach(this::removeEntry);
    }

    public void removeEntry(E entry) {
        this.children.remove(entry);
    }

    public void clearEntriesExcept(E entry) {
        this.children.removeIf(candidate -> candidate != entry);
    }

    @Deprecated
    @Override
    protected final void drawSelectionBox(int contentLeft, int contentTop, int mouseX, int mouseY) {
    }

    public interface IListEntry extends IGuiListEntry {
        /**
         * Called when the mouse is clicked within this entry.
         *
         * @param mouseX    the current mouse x position
         * @param mouseY    the current mouse y position
         * @param relativeX the current x position of the mouse relative to the top-left corner of the entry
         * @param relativeY the current y position of the mouse relative to the top-left corner of the entry
         * @return {@code true} means that something within this entry was clicked and the list should not be dragged.
         */
        @Override
        default boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
            return false;
        }

        /**
         * Called when the mouse button is released.
         *
         * @param mouseX    the current mouse x position
         * @param mouseY    the current mouse y position
         * @param relativeX the current x position of the mouse relative to the top-left corner of the entry
         * @param relativeY the current y position of the mouse relative to the top-left corner of the entry
         */
        @Override
        default void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
        }
    }
}
