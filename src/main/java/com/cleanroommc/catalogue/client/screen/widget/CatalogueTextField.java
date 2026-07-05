package com.cleanroommc.catalogue.client.screen.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CatalogueTextField extends GuiTextField {
    private String suggestion = "";
    private String hint = "";
    @Nullable
    private Consumer<String> responder;
    @Nullable
    private BiFunction<String, Integer, String> formatter;

    public CatalogueTextField(FontRenderer font, int x, int y, int width, int height) {
        super(font, x, y, width, height);
    }

    @Override
    public void drawTextBox() {
        if (!this.getVisible()) return;

        boolean bordered = this.getEnableBackgroundDrawing();
        if (bordered) {
            drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0);
            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0xFF000000);
        }

        int color = this.isEnabled ? this.enabledColor : this.disabledColor;

        int cursorPosRel = this.cursorPosition - this.lineScrollOffset;
        int selectionEndRel = this.selectionEnd - this.lineScrollOffset;

        String visibleText = this.field_146211_a.trimStringToWidth(this.getText().substring(this.lineScrollOffset), this.getWidth());

        boolean cursorVisible = cursorPosRel >= 0 && cursorPosRel <= visibleText.length();
        boolean drawCursor = this.isFocused() && this.cursorCounter / 6 % 2 == 0 && cursorVisible;

        int textX = bordered ? this.xPosition + 4 : this.xPosition;
        int textY = bordered ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
        final int textStartX = textX;

        selectionEndRel = MathHelper.clamp_int(selectionEndRel, 0, visibleText.length());

        if (!visibleText.isEmpty()) {
            String beforeText = cursorVisible ? visibleText.substring(0, cursorPosRel) : visibleText;
            textX = this.field_146211_a.drawStringWithShadow(this.formatText(beforeText, this.lineScrollOffset), textStartX, textY, color);
        }

        boolean textTruncated = this.cursorPosition < this.getText().length() || this.getText().length() >= this.getMaxStringLength();
        int cursorX = textX;

        if (!cursorVisible) {
            cursorX = cursorPosRel > 0 ? textStartX + this.width : textStartX;
        } else if (textTruncated) {
            cursorX = textX - 1;
            --textX;
        }

        if (!visibleText.isEmpty() && cursorVisible && cursorPosRel < visibleText.length()) {
            String afterText = visibleText.substring(cursorPosRel);
            textX = this.field_146211_a.drawStringWithShadow(this.formatText(afterText, this.cursorPosition), textX, textY, color);
        }

        if (!this.hint.isEmpty() && visibleText.isEmpty() && !this.isFocused()) {
            this.field_146211_a.drawStringWithShadow(this.hint, textX, textY, color);
        }

        if (!textTruncated && !this.suggestion.isEmpty()) {
            int suggestionDrawX = this.getText().isEmpty() ? cursorX : cursorX - 1;
            String suggestion = this.field_146211_a.trimStringToWidth(this.suggestion, textStartX + this.getWidth() - suggestionDrawX);
            this.field_146211_a.drawStringWithShadow(suggestion, suggestionDrawX, textY, 0x808080);
        }

        if (drawCursor) {
            if (textTruncated) {
                drawRect(cursorX, textY - 1, cursorX + 1, textY + 1 + this.field_146211_a.FONT_HEIGHT, 0xFFCFCFD0);
            } else {
                this.field_146211_a.drawStringWithShadow("_", cursorX, textY, color);
            }
        }

        if (selectionEndRel != cursorPosRel) {
            int selectionEndX = textStartX + this.field_146211_a.getStringWidth(visibleText.substring(0, selectionEndRel));
            this.drawCursorVertical(cursorX, textY - 1, selectionEndX - 1, textY + 1 + this.field_146211_a.FONT_HEIGHT);
        }
    }

    private String formatText(String text, int cursorPos) {
        return this.formatter != null ? this.formatter.apply(text, cursorPos) : text;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void setResponder(@Nullable Consumer<String> responder) {
        this.responder = responder;
    }

    public void setFormatter(@Nullable BiFunction<String, Integer, String> formatter) {
        this.formatter = formatter;
    }

    protected void setResponderEntryValue(String text) {
        if (this.responder != null) this.responder.accept(text);
    }

    @Override
    public void writeText(String textToWrite) {
        String preText = this.getText();
        super.writeText(textToWrite);
        if (!preText.equals(this.getText())) this.setResponderEntryValue(this.getText());
    }

    @Override
    public void deleteFromCursor(int num) {
        String preText = this.getText();
        super.deleteFromCursor(num);
        if (!preText.equals(this.getText())) this.setResponderEntryValue(this.getText());
    }

    @Override
    public void setText(String text) {
        String preText = this.getText();
        super.setText(text);
        if (!preText.equals(this.getText())) this.setResponderEntryValue(this.getText());
    }

    @Override
    public void setMaxStringLength(int length) {
        String preText = this.getText();
        super.setMaxStringLength(length);
        if (!preText.equals(this.getText())) this.setResponderEntryValue(this.getText());
    }
}
