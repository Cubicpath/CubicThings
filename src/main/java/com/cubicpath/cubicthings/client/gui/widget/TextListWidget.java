////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ITextListHolder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;

public class TextListWidget extends ExtendedList<TextListWidget.TextEntry>{
    private final boolean renderDirtBackground;
    private final boolean renderDarkOutline;
    private final int left;
    private final int listWidth;
    private final int textColor;
    private final ITextListHolder parent;

    public TextListWidget(ITextListHolder parent, int listWidth, int left, int top, int bottom, int textColor, boolean renderDirtBackground, boolean renderDarkOutline) {
        super(parent.getMinecraft(), listWidth, 0, top, bottom, parent.getFontRenderer().FONT_HEIGHT + 2);
        this.renderDirtBackground = renderDirtBackground;
        this.renderDarkOutline = renderDarkOutline;
        this.left = left;
        this.listWidth = listWidth;
        this.textColor = textColor;
        this.parent = parent;
        setLeftPos(left); // Fixes bug where text is rendered on the far left of the screen
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
        this.parent.buildTextList(this::addEntry, (target) -> new TextEntry(ITextComponent.getTextComponentOrEmpty(target), this.parent, this.textColor));
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
        private final ITextComponent displayText;
        private final ITextListHolder parent;
        private final int textColor;

        public TextEntry(ITextComponent displayText, ITextListHolder parent, int textColor) {
            this.textColor = textColor;
            this.displayText = displayText;
            this.parent = parent;
        }

        @Override
        public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            FontRenderer font = this.parent.getFontRenderer();
            font.drawText(matrixStack, this.displayText, left + 2 , top - 2, this.textColor);
        }
    }
}
