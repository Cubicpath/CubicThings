////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ITextListHolder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;

public class TextListWidget extends ExtendedList<TextListWidget.TextEntry>{
    private final boolean renderDirtBackground;
    private final boolean renderDarkOutline;
    private final int left, listWidth, borderPaddingX, borderPaddingY, textColor;
    private final ITextListHolder parent;

    public TextListWidget(ITextListHolder parent, int listWidth, int listHeight, int x, int y, int borderPaddingX, int borderPaddingY, int textSpacing, int textColor, boolean renderDirtBackground, boolean renderDarkOutline) {
        super(Minecraft.getInstance(), listWidth, 0, y, y + listHeight, textSpacing);
        this.renderDirtBackground = renderDirtBackground;
        this.renderDarkOutline = renderDarkOutline;
        this.left = x;
        this.listWidth = listWidth;
        this.borderPaddingX = borderPaddingX;
        this.borderPaddingY = borderPaddingY;
        this.textColor = textColor;
        this.parent = parent;
        setLeftPos(x); // Fixes bug where text is rendered on the far left of the screen
        refreshList();
    }

    @Override
    protected int getScrollbarPosition() {
        return this.left + this.listWidth;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();
        this.parent.buildTextList(this::addEntry, (stringIn, colorIn) -> new TextEntry(ITextComponent.getTextComponentOrEmpty(stringIn), this.parent, this.borderPaddingX, this.borderPaddingY, colorIn != null ? colorIn : this.textColor));
    }

    @Override
    protected void renderBackground(MatrixStack matrixStack) {
        this.parent.renderBackground(matrixStack);
        this.func_244605_b(this.renderDirtBackground);
        this.func_244606_c(this.renderDarkOutline);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        setLeftPos(left);
    }

    public static class TextEntry extends ExtendedList.AbstractListEntry<TextEntry>{
        private final int xPadding, yPadding, color;
        private final ITextComponent displayText;
        private final ITextListHolder parent;

        public TextEntry(ITextComponent displayText, ITextListHolder parent, int xPadding, int yPadding, int color) {
            this.xPadding = xPadding;
            this.yPadding = yPadding;
            this.color = color;
            this.displayText = displayText;
            this.parent = parent;
        }

        @Override
        public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            FontRenderer font = this.parent.getFontRenderer();
            font.drawText(matrixStack, this.displayText, left + this.xPadding , top + this.yPadding, this.color);
        }
    }
}
