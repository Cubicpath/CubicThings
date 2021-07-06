////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.container.ScannerContainer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class ScannerScreen extends ContainerScreen<ScannerContainer> {
    /** The location of the inventory background texture */
    protected static final ResourceLocation SCANNER_SCREEN_TEXTURE = new ResourceLocation(CubicThings.MODID, "textures/gui/container/scanner.png");

    protected LivingEntity entity;
    protected float entityYaw = 0.0F;
    protected int mouseX;
    protected int mouseY;

    public ScannerScreen(ScannerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = 176;
        this.ySize = 166;
    }


    @Override
    @ParametersAreNonnullByDefault
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        this.entity = getMinecraft().player;

        this.font.drawText(matrixStack, ((TextComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), (float)this.titleX, (float)this.titleY, 4210752);
        this.font.drawText(matrixStack, this.entity.getType().getName(), this.titleX + 81, this.titleY + 7, 0xBABABA);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        this.entity = getMinecraft().player;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bindTexture(SCANNER_SCREEN_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
        this.drawEntityOnScreen(i + 125, j + 95, this.ySize / 5);
    }

    //uses net.minecraft.client.gui.screen.inventory::drawEntityOnScreen math
    public void drawEntityOnScreen(int posX, int posY, int scale) {
        float f = (float)Math.atan(this.entityYaw / 40.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)posX, (float)posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(0);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        float renderYawOffset = this.entity.renderYawOffset;
        float rotationYaw = this.entity.rotationYaw;
        float rotationYawHead = this.entity.rotationYawHead;
        float prevRotationYawHead = this.entity.prevRotationYawHead;
        float rotationPitch = this.entity.rotationPitch;
        this.entity.renderYawOffset = 180.0F + f * 20.0F;
        this.entity.rotationYaw = 180.0F + f * 40.0F;
        this.entity.rotationYawHead = this.entity.rotationYaw;
        this.entity.prevRotationYawHead = this.entity.rotationYaw;
        this.entity.rotationPitch = 0.0F;
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderSystem.runAsFancy(() -> {
            entityrenderermanager.renderEntityStatic(this.entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        this.entity.renderYawOffset = renderYawOffset;
        this.entity.rotationYaw = rotationYaw;
        this.entity.rotationYawHead = rotationYawHead;
        this.entity.prevRotationYawHead = prevRotationYawHead;
        this.entity.rotationPitch = rotationPitch;
        RenderSystem.popMatrix();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
