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

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Information on how to render the Scanner's internal container.
 *
 * @since 0.2.2
 * @author Cubicpath
 */
@SuppressWarnings("deprecation")
public class ScannerScreen extends ContainerScreen<ScannerContainer> {
    /** The location of the inventory background texture */
    protected static final ResourceLocation SCANNER_SCREEN_TEXTURE = new ResourceLocation(CubicThings.MODID, "textures/gui/container/scanner.png");

    /** Non-persistent memory of the last entity selected */
    protected static LivingEntity entity;

    protected float entityYaw = 0.0F;
    protected int mouseX;
    protected int mouseY;

    protected static LivingEntity getDefaultEntity(World world){
        return new RemoteClientPlayerEntity((ClientWorld)world, new GameProfile(null, "Player"));
    }

    public ScannerScreen(ScannerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        entity = entity != null ? entity : getDefaultEntity(inv.player.world);
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void init() {
        super.init();
    }

    //uses net.minecraft.client.gui.screen.inventory::drawEntityOnScreen math
    public void drawEntityOnScreen(int posX, int posY, int scale) {
        if (entity != null) {
            float f = (float) Math.atan(entityYaw / 40.0F);
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) posX, (float) posY, 1050.0F);
            RenderSystem.scalef(1.0F, 1.0F, -1.0F);
            MatrixStack matrixstack = new MatrixStack();
            matrixstack.translate(0.0D, 0.0D, 1000.0D);
            matrixstack.scale((float) scale, (float) scale, (float) scale);
            Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
            Quaternion quaternion1 = Vector3f.XP.rotationDegrees(0);
            quaternion.multiply(quaternion1);
            matrixstack.rotate(quaternion);
            float renderYawOffset = entity.renderYawOffset;
            float rotationYaw = entity.rotationYaw;
            float rotationYawHead = entity.rotationYawHead;
            float prevRotationYawHead = entity.prevRotationYawHead;
            float rotationPitch = entity.rotationPitch;
            entity.renderYawOffset = 180.0F + f * 20.0F;
            entity.rotationYaw = 180.0F + f * 40.0F;
            entity.rotationYawHead = entity.rotationYaw;
            entity.prevRotationYawHead = entity.rotationYaw;
            entity.rotationPitch = 0.0F;
            EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
            quaternion1.conjugate();
            entityrenderermanager.setCameraOrientation(quaternion1);
            entityrenderermanager.setRenderShadow(false);
            IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            RenderSystem.runAsFancy(() -> {
                entityrenderermanager.renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
            });
            irendertypebuffer$impl.finish();
            entityrenderermanager.setRenderShadow(true);
            entity.renderYawOffset = renderYawOffset;
            entity.rotationYaw = rotationYaw;
            entity.rotationYawHead = rotationYawHead;
            entity.prevRotationYawHead = prevRotationYawHead;
            entity.rotationPitch = rotationPitch;
            RenderSystem.popMatrix();
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        this.font.drawText(matrixStack, ((TextComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), (float)this.titleX, (float)this.titleY, 0x404040);
        // Draw entity texts
        if (entity != null) {
            this.font.drawText(matrixStack, entity.getType().getName(), 89, 13, 0xBABABA);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bindTexture(SCANNER_SCREEN_TEXTURE);
        this.blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawEntityOnScreen(this.guiLeft + 125, this.guiTop + 95, this.ySize / 5);
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
