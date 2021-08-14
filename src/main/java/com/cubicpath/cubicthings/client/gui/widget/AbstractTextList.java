////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ITextListHolder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractTextList<E extends AbstractTextList.AbstractTextEntry<E>> extends AbstractSelectionList<E>{
    protected final int left, listWidth, borderPaddingX, borderPaddingY, textColor;
    protected final Consumer<AbstractTextEntry<E>> onEntryClicked;
    protected Font font;

    public AbstractTextList(int listWidth, int listHeight, int x, int y, int borderPaddingX, int borderPaddingY, int itemHeight, int textColor, boolean renderDirtBackground, boolean renderDarkOutline, Font font, Consumer<AbstractTextEntry<E>> onEntryClicked) {
        super(Minecraft.getInstance(), listWidth, 0, y, y + listHeight, itemHeight);
        super.setRenderBackground(renderDirtBackground);
        super.setRenderTopAndBottom(renderDarkOutline);
        super.setLeftPos(x); // Fixes bug where text is rendered on the far left of the screen
        this.left = x;
        this.listWidth = listWidth;
        this.borderPaddingX = borderPaddingX;
        this.borderPaddingY = borderPaddingY;
        this.textColor = textColor;
        this.font = font;
        this.onEntryClicked = onEntryClicked;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.left + this.listWidth;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public abstract void refreshList();

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.setSelected(this.getEntryAtPosition(mouseX, mouseY));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        super.setLeftPos(this.left);
    }

    @Override
    public void renderList(PoseStack poseStack, int x, int y, int mouseX, int mouseY, float partialTicks){
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for(int i = 0; i < this.getItemCount(); ++i) {
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowTop(i) + this.itemHeight;
            if (rowBottom >= this.y0 && rowTop <= this.y1 - this.itemHeight) { // Fixes entry being outside the bottom list border
                int top = y + (i * this.itemHeight) + this.headerHeight + 4;
                int rowHeight = this.itemHeight - 4;
                int rowWidth = this.getRowWidth();
                int rowLeft = this.getRowLeft();
                AbstractTextEntry<E> textEntry = this.getEntry(i);
                // Temporarily disabled
                if (false && textEntry.isActive() && this.getSelected() != null && textEntry.displayText.equals(this.getSelected().displayText)) {
                    int left = this.x0 + (this.width / 2) - rowWidth / 2;
                    int right = left + this.font.width(textEntry.displayText) + 10;
                    RenderSystem.disableTexture();
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                    float f = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.setShaderColor(f, f, f, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex(left, top + rowHeight + 3, 0.0D).endVertex();
                    bufferbuilder.vertex(right, top + rowHeight + 3, 0.0D).endVertex();
                    bufferbuilder.vertex(right, top - 3, 0.0D).endVertex();
                    bufferbuilder.vertex(left, top - 3, 0.0D).endVertex();
                    tesselator.end();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex(left + 1, top + rowHeight + 2, 0.0D).endVertex();
                    bufferbuilder.vertex(right - 1, top + rowHeight + 2, 0.0D).endVertex();
                    bufferbuilder.vertex(right - 1, top - 2, 0.0D).endVertex();
                    bufferbuilder.vertex(left + 1, top - 2, 0.0D).endVertex();
                    tesselator.end();
                    RenderSystem.enableTexture();
                }

                textEntry.render(poseStack, i, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, Objects.equals(getHovered(), textEntry), partialTicks);
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    public static abstract class AbstractTextEntry<E extends AbstractTextEntry<E>> extends AbstractSelectionList.Entry<E> {
        protected final int xPadding, yPadding, color;
        protected final Consumer<AbstractTextEntry<E>> onClicked;
        protected final Component displayText;
        protected final ITextListHolder parentScreen;

        @SuppressWarnings("unchecked")
        public E get(){
            return (E) this;
        }

        public AbstractTextEntry(Consumer<AbstractTextEntry<E>> onClicked, Component displayText, ITextListHolder parentScreen, int xPadding, int yPadding, int color) {
            this.onClicked = onClicked;
            this.xPadding = xPadding;
            this.yPadding = yPadding;
            this.color = color;
            this.displayText = displayText;
            this.parentScreen = parentScreen;
        }

        public Component getDisplayText() {
            return this.displayText;
        }

        public abstract boolean isActive();

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.onClicked.accept(this);
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void render(PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            Font font = this.parentScreen.getFontRenderer();
            font.draw(matrixStack, font.width(this.displayText) > this.list.getRowWidth() ? font.split(this.displayText, this.list.getRowWidth()).get(0) : this.displayText.getVisualOrderText(), left + this.xPadding , top + this.yPadding, this.color);

            if (super.isMouseOver(mouseX, mouseY)) {
                fill(matrixStack, left, left + width, top, top + height, 0xFFFFFF);
            }
        }
    }
}
