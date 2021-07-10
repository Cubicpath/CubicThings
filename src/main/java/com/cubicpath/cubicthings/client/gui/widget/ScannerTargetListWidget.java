////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ScannerScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

public class ScannerTargetListWidget extends ExtendedList<ScannerTargetListWidget.TargetEntry>{
    private final int left;
    private final int listWidth;
    private final ScannerScreen parent;

    public ScannerTargetListWidget(ScannerScreen parent, int listWidth, int left, int top, int bottom) {
        super(parent.getMinecraft(), listWidth, 0, top, bottom, parent.getFontRenderer().FONT_HEIGHT + 2);
        this.left = left;
        this.listWidth = listWidth;
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
        parent.buildTargetList(this::addEntry, (target) -> new ScannerTargetListWidget.TargetEntry(ITextComponent.getTextComponentOrEmpty(target), this.parent));
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void renderBackground(MatrixStack matrixStack) {
        this.parent.renderBackground(matrixStack);
        //this.func_244605_b(false);
        this.func_244606_c(false);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        setLeftPos(left);
    }

    public static class TargetEntry extends ExtendedList.AbstractListEntry<ScannerTargetListWidget.TargetEntry>{
        private final ITextComponent targetName;
        private final ScannerScreen parent;

        public TargetEntry(ITextComponent targetName, ScannerScreen parent) {
            this.targetName = targetName;
            this.parent = parent;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            FontRenderer font = this.parent.getFontRenderer();
            font.drawText(matrixStack, this.targetName, left + 2 , top - 2, 0xCCCCCC);
        }
    }
}
