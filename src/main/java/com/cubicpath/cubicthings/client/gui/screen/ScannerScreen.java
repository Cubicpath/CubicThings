////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.widget.AbstractTextList;
import com.cubicpath.cubicthings.client.gui.widget.TextListWidget;
import com.cubicpath.cubicthings.common.container.ScannerContainer;
import com.cubicpath.cubicthings.common.item.ScannerItem;
import com.cubicpath.cubicthings.common.network.CScannerModePacket;
import com.cubicpath.cubicthings.common.network.CScannerTargetPacket;
import com.cubicpath.cubicthings.core.init.NetworkInit;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Information on how to render the Scanner's internal container.
 *
 * @since 0.2.2
 * @author Cubicpath
 */
public class ScannerScreen extends AbstractContainerScreen<ScannerContainer> implements ITextListHolder {
    /** The location of the inventory background texture */
    protected static final ResourceLocation SCANNER_SCREEN_TEXTURE = new ResourceLocation(CubicThings.MODID, "textures/gui/container/scanner.png");

    /** Non-persistent memory of the last object selected */
    @Nullable
    protected static Object renderTarget = null;
    
    protected boolean clickedTargetField = false;
    protected float renderYaw = 0.0F;
    protected int mouseX, mouseY;
    protected final ItemStack itemStack;
    protected final ClientLevel world;
    protected Button targetAddButton, targetRemoveButton, targetsClearButton, targetModeButton;
    protected EditBox targetInputField;
    protected AbstractTextList<?> targetList;

    public ScannerScreen(ScannerContainer screenContainer, Inventory inventory, Component titleIn) {
        super(screenContainer, inventory, titleIn);
        this.itemStack = screenContainer.sourceItemStack;
        this.world = (ClientLevel) inventory.player.level;
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 9;
    }

    protected static LivingEntity createNewDummyPlayer(ClientLevel world){
        RemotePlayer dummyPlayer = new RemotePlayer(world, new GameProfile(null, "Player"));
        dummyPlayer.setCustomNameVisible(false);
        return dummyPlayer;
    }

    protected Set<String> getStoredTargetSet(){
        Set<String> targetSet = new HashSet<>();
        getScannerMode().getTargetList(this.itemStack).forEach(stringNBT -> targetSet.add(stringNBT.getAsString()));
        return targetSet;
    }

    protected ScannerItem.ScannerMode getScannerMode(){
        return ScannerItem.getScannerMode(this.itemStack);
    }

    protected Component currentTargetValue() {
        return new TranslatableComponent("gui.cubicthings.scannerMenu.target", getScannerMode().toTitleCase());
    }

    public Font getFontRenderer() {
        return this.font;
    }

    public <T extends AbstractSelectionList.Entry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<Component, String, T> newEntry) {
        getStoredTargetSet().stream().sorted().forEach(string -> textListViewConsumer.accept(newEntry.apply(Component.nullToEmpty(string), "true")));
    }

    public void updateWidgets(){
        boolean validInput = !this.targetInputField.getValue().trim().isEmpty() && ResourceLocation.isValidResourceLocation(this.targetInputField.getValue().toLowerCase().trim());
        ResourceLocation resourceLocation = validInput ? new ResourceLocation(this.targetInputField.getValue().toLowerCase().trim()) : null;

        if (!this.clickedTargetField && this.targetInputField.isFocused()) {
            this.targetInputField.setValue("");
            this.targetInputField.setTextColor(0xDDDD44);
            this.clickedTargetField = true;
        }

        switch (getScannerMode()) {
            case BLOCKS -> {
                Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                renderTarget = Objects.requireNonNullElse(block, Blocks.AIR);
            }
            case BIOMES -> {
                Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
                renderTarget = Objects.requireNonNullElse(biome, Biomes.THE_VOID);
            }
            case ENTITIES -> {
                EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(resourceLocation);
                if (entityType != null && resourceLocation != null) {
                    Entity newEntity = entityType.create(this.world);

                    if (resourceLocation.getNamespace().equals("minecraft") && resourceLocation.getPath().equals("player"))
                        renderTarget = createNewDummyPlayer(this.world);
                    else if (newEntity instanceof LivingEntity)
                        renderTarget = newEntity;
                } else renderTarget = EntityType.ITEM;
            }
        }

        this.targetAddButton.setMessage(new TranslatableComponent("gui.cubicthings.scannerMenu.add", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()));
        this.targetAddButton.active = validInput && getStoredTargetSet().stream().noneMatch((string) -> string.equals(this.targetInputField.getValue().toLowerCase().trim()) || string.replaceFirst("minecraft:", "").equals(this.targetInputField.getValue().toLowerCase().trim()));

        this.targetRemoveButton.setMessage(new TranslatableComponent("gui.cubicthings.scannerMenu.remove", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()));
        this.targetRemoveButton.active = validInput && getStoredTargetSet().stream().anyMatch((string) -> string.equals(this.targetInputField.getValue().toLowerCase().trim()) || string.replaceFirst("minecraft:", "").equals(this.targetInputField.getValue().toLowerCase().trim()));

        this.targetsClearButton.setMessage(new TranslatableComponent("gui.cubicthings.scannerMenu.clear", getScannerMode().toTitleCase()));
        this.targetsClearButton.active = getStoredTargetSet().size() > 0;

        this.targetList.refreshList();
    }

    //uses net.minecraft.client.gui.screen.inventory::drawEntityOnScreen math
    public void drawTargetOnScreen(int posX, int posY, int scale) {
        //TODO: Render 3D preview for Blocks
        //TODO: Render preview for Biomes

        this.renderYaw = this.renderYaw + 0.015F;
        if (renderTarget instanceof Block block) {
            getMinecraft().getItemRenderer().renderAndDecorateFakeItem(block.asItem().getDefaultInstance(),this.leftPos + 120, this.topPos + 55);
        } else if (renderTarget instanceof Entity entity) {
            // Setup position
            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.translate(posX, posY, 1050.0);
            poseStack.scale(1.0F, 1.0F, -1.0F);
            RenderSystem.applyModelViewMatrix();
            PoseStack poseStack1 = new PoseStack();
            poseStack1.translate(0.0D, 0.0D, 1000.0D);
            poseStack1.scale((float) scale, (float) scale, (float) scale);
            Quaternion quaternion = Vector3f.ZN.rotationDegrees(175.0F);
            Quaternion quaternion1 = Vector3f.XP.rotationDegrees(0);
            quaternion.mul(quaternion1);
            poseStack1.mulPose(quaternion);

            // Set entity rotation
            boolean isCustomNameVisible = entity.isCustomNameVisible();
            float renderYawOffset = 0.0F;
            float rotationYaw = entity.getXRot();
            float rotationYawHead = 0.0F;
            float rotationPitch = entity.getYRot();
            entity.setCustomNameVisible(false);
            entity.setYRot(180.0F + this.renderYaw * 40.0F);
            entity.setXRot(0.0F);
            if (entity instanceof LivingEntity livingEntity) {
                renderYawOffset = livingEntity.yBodyRot;
                rotationYawHead = livingEntity.yHeadRot;
                livingEntity.yBodyRot = 180.0F + this.renderYaw * 20.0F;
                livingEntity.yHeadRot = livingEntity.yBodyRot;
            }

            // Render entity
            EntityRenderDispatcher entityRenderDispatcher = getMinecraft().getEntityRenderDispatcher();
            quaternion1.conj();
            entityRenderDispatcher.overrideCameraOrientation(quaternion1);
            entityRenderDispatcher.setRenderShadow(false);
            MultiBufferSource.BufferSource multibuffersource$buffersource = getMinecraft().renderBuffers().bufferSource();
            entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack1, multibuffersource$buffersource, 0xF000F0);
            multibuffersource$buffersource.endBatch();
            entityRenderDispatcher.setRenderShadow(true);

            // Restore entity's original properties
            entity.setCustomNameVisible(isCustomNameVisible);
            entity.setXRot(rotationYaw);
            entity.setYRot(rotationPitch);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.yBodyRot = renderYawOffset;
                livingEntity.yHeadRot = rotationYawHead;
                livingEntity.yHeadRotO = rotationYawHead;
            }

            // End
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
        }
    }

    @Override
    protected void init() {
        super.init();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        this.targetInputField = new EditBox(this.font, this.leftPos + 10, this.topPos + 27, 72, 8, Component.nullToEmpty("Target Field"));
        this.targetInputField.setMaxLength(256);
        this.targetInputField.setTextColor(0x888888);
        this.targetInputField.setValue("Target Field");
        addRenderableWidget(this.targetInputField);

        int slotIndex = this.menu.inventory.findSlotMatchingItem(this.menu.sourceItemStack);

        this.targetAddButton = addRenderableWidget(new Button(this.leftPos + 8, this.topPos + 40, 76, 14, new TranslatableComponent("gui.cubicthings.scannerMenu.add", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getValue().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 1));
            ScannerItem.addTarget(this.itemStack, this.getScannerMode(), input);
            updateWidgets();

            button.active = false;
            this.targetRemoveButton.active = true;
        }));
        this.targetAddButton.active = false;

        this.targetRemoveButton = addRenderableWidget(new Button(this.leftPos + 8, this.topPos + 52, 76, 14, new TranslatableComponent("gui.cubicthings.scannerMenu.remove", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getValue().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 2));
            ScannerItem.removeTarget(this.itemStack, this.getScannerMode(), input);
            updateWidgets();

            button.active = false;
            this.targetAddButton.active = true;
        }));
        this.targetRemoveButton.active = false;

        this.targetsClearButton = addRenderableWidget(new Button(this.leftPos + 8, this.topPos + 66, 76, 14, new TranslatableComponent("gui.cubicthings.scannerMenu.clear", getScannerMode().toTitleCase()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getValue().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 3));
            getScannerMode().getTargetList(this.itemStack).clear();
            updateWidgets();

            this.targetInputField.setValue("");
        }));
        this.targetsClearButton.active = getStoredTargetSet().size() > 0;

        this.targetModeButton = addRenderableWidget(new Button(this.leftPos + 6, this.topPos + 84, 80, 20, currentTargetValue(), (button) -> {
            ScannerItem.ScannerMode scannerMode;
            try { scannerMode = ScannerItem.ScannerMode.values()[getScannerMode().ordinal() + 1];
            } catch (ArrayIndexOutOfBoundsException e) { scannerMode = ScannerItem.ScannerMode.values()[0]; }

            ScannerItem.setScannerMode(this.itemStack, scannerMode);
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerModePacket(scannerMode, slotIndex));
            button.setMessage(currentTargetValue());
            this.targetInputField.setValue("");
            updateWidgets();
        }));
        this.targetModeButton.active = true;

        this.targetList = new TextListWidget(this, 156, 50, this.leftPos + 7, this.topPos + 109, 2, -2 , this.font.lineHeight + 2, 0xCCCCCC, true, false, this.font, targetEntry -> {
            var oldValue = this.targetInputField.getValue().replaceFirst("minecraft:", "").trim();
            var newValue = targetEntry.getDisplayText().getString().replaceFirst("minecraft:", "").trim();
            if (!this.clickedTargetField) {
                this.targetInputField.setTextColor(0xDDDD44);
                this.clickedTargetField = true;
            }
            this.setFocused(targetInputField);
            this.targetInputField.setValue(targetEntry.getDisplayText().getString().replaceFirst("minecraft:", "").trim());
            if (!this.targetInputField.getValue().equals(oldValue))
                getMinecraft().getSoundManager().play(new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 0.25F, 1.0F, 0, 0, 0));
            if (newValue.length() > 12)
                this.targetInputField.moveCursorToEnd();
            else
                this.targetInputField.moveCursorToStart();
        });

    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.targetInputField.isFocused() && this.targetInputField.isVisible()){
            switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT_SHIFT:
                case GLFW.GLFW_KEY_LEFT_CONTROL:
                case GLFW.GLFW_KEY_LEFT_ALT:
                case GLFW.GLFW_KEY_LEFT_SUPER:
                case GLFW.GLFW_KEY_RIGHT_SHIFT:
                case GLFW.GLFW_KEY_RIGHT_CONTROL:
                case GLFW.GLFW_KEY_RIGHT_ALT:
                case GLFW.GLFW_KEY_RIGHT_SUPER: break;
                case GLFW.GLFW_KEY_LEFT: {
                    this.targetInputField.setCursorPosition(this.targetInputField.getCursorPosition() - 1);
                    break;
                }
                case GLFW.GLFW_KEY_RIGHT: {
                    this.targetInputField.setCursorPosition(this.targetInputField.getCursorPosition() + 1);
                    break;
                }
                case GLFW.GLFW_KEY_ENTER: {
                    if (this.targetAddButton.isActive())
                        this.targetAddButton.onPress();
                    else if (this.targetRemoveButton.isActive())
                        this.targetRemoveButton.onPress();

                    break;
                }
                case GLFW.GLFW_KEY_TAB: {
                    this.targetInputField.setFocus(false);
                    break;
                }
                case GLFW.GLFW_KEY_BACKSPACE: {
                    this.targetInputField.deleteChars(-1);
                    break;
                }
            }

            return keyCode != GLFW.GLFW_KEY_ESCAPE || super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int mouseX, int mouseY, int button){
        return super.keyReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.targetList.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.targetList.mouseScrolled(mouseX, mouseY, delta * 4);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.targetList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.targetList.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, ((MutableComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), (float)this.titleLabelX, (float)this.titleLabelY, 0x404040);
        // Draw object texts
        if (renderTarget instanceof Block)
            this.font.draw(poseStack, ((Block)renderTarget).getName(), 90, 16,  0xBABABA);
        else if (renderTarget instanceof Biome)
            this.font.draw(poseStack, new TranslatableComponent(("biome." + ((Biome) renderTarget).getRegistryName()).replace(':', '.')), 90, 16, 0xBABABA);
        else if (renderTarget instanceof Entity)
            this.font.draw(poseStack, ((Entity)renderTarget).getDisplayName(), 90, 16, 0xBABABA);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, SCANNER_SCREEN_TEXTURE);
        this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (renderTarget instanceof Block)
            drawTargetOnScreen(this.leftPos + 127, this.topPos + 98, 1);
        else if (renderTarget instanceof Biome)
            drawTargetOnScreen(this.leftPos + 127, this.topPos + 98, 1);
        else if (renderTarget instanceof Entity)
            drawTargetOnScreen(this.leftPos + 127, this.topPos + 98, (int) Mth.clamp(this.imageHeight / Math.max(((Entity)renderTarget).getDimensions(Pose.STANDING).width * 3, ((Entity)renderTarget).getDimensions(Pose.STANDING).height * 3), this.imageHeight / 10.0F, this.imageWidth / 4.0F));
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(poseStack);
        this.updateWidgets();
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.targetList.render(poseStack, mouseX, mouseY, partialTicks);
        //this.renderLabels(poseStack, mouseX, mouseY);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

}
