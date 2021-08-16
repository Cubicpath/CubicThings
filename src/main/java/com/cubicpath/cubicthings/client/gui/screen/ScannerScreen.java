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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
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
public class ScannerScreen extends ContainerScreen<ScannerContainer> implements ITextListHolder {
    /** The location of the inventory background texture */
    protected static final ResourceLocation SCANNER_SCREEN_TEXTURE = new ResourceLocation(CubicThings.MODID, "textures/gui/container/scanner.png");

    /** Non-persistent memory of the last object selected */
    @Nullable
    protected static Object renderTarget = null;
    
    protected boolean clickedTargetField = false;
    protected float renderYaw = 0.0F;
    protected int mouseX, mouseY;
    protected final ItemStack itemStack;
    protected final ClientWorld world;
    protected Button targetAddButton, targetRemoveButton, targetsClearButton, targetModeButton;
    protected TextFieldWidget targetInputField;
    protected AbstractTextList<?> targetList;

    public ScannerScreen(ScannerContainer screenContainer, PlayerInventory inventory, ITextComponent titleIn) {
        super(screenContainer, inventory, titleIn);
        this.itemStack = screenContainer.sourceItemStack;
        this.world = (ClientWorld) inventory.player.level;
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 9;
    }

    protected static LivingEntity createNewDummyPlayer(ClientWorld world){
        RemoteClientPlayerEntity dummyPlayer = new RemoteClientPlayerEntity(world, new GameProfile(null, "Player"));
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

    protected ITextComponent currentTargetValue() {
        return new TranslationTextComponent("gui.cubicthings.scannerMenu.target", getScannerMode().toTitleCase());
    }

    public FontRenderer getFontRenderer() {
        return this.font;
    }

    public <T extends AbstractList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<ITextComponent, String, T> newEntry) {
        getStoredTargetSet().stream().sorted().forEach(string -> textListViewConsumer.accept(newEntry.apply(ITextComponent.nullToEmpty(string), "true")));
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
            case BLOCKS: {
                Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                if (block != null) renderTarget = block;
                else renderTarget = Blocks.AIR;

                break;
            }
            case BIOMES: {
                Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
                if (biome != null) renderTarget = biome;
                else renderTarget = Biomes.THE_VOID;

                break;
            }
            case ENTITIES: {
                EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(resourceLocation);
                if (entityType != null && resourceLocation != null) {
                    Entity newEntity = entityType.create(this.world);

                    if (resourceLocation.getNamespace().equals("minecraft") && resourceLocation.getPath().equals("player"))
                        renderTarget = createNewDummyPlayer(this.world);
                    else if (newEntity instanceof LivingEntity)
                        renderTarget = newEntity;
                } else renderTarget = EntityType.ITEM;

                break;
            }
        }

        this.targetAddButton.setMessage(new TranslationTextComponent("gui.cubicthings.scannerMenu.add", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()));
        this.targetAddButton.active = validInput && getStoredTargetSet().stream().noneMatch((string) -> string.equals(this.targetInputField.getValue().toLowerCase().trim()) || string.replaceFirst("minecraft:", "").equals(this.targetInputField.getValue().toLowerCase().trim()));

        this.targetRemoveButton.setMessage(new TranslationTextComponent("gui.cubicthings.scannerMenu.remove", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()));
        this.targetRemoveButton.active = validInput && getStoredTargetSet().stream().anyMatch((string) -> string.equals(this.targetInputField.getValue().toLowerCase().trim()) || string.replaceFirst("minecraft:", "").equals(this.targetInputField.getValue().toLowerCase().trim()));

        this.targetsClearButton.setMessage(new TranslationTextComponent("gui.cubicthings.scannerMenu.clear", getScannerMode().toTitleCase()));
        this.targetsClearButton.active = getStoredTargetSet().size() > 0;

        this.targetList.refreshList();
    }

    @SuppressWarnings("deprecation")
    public void drawTargetOnScreen(int posX, int posY, int scale) {
        //TODO: Render 3D preview for Blocks
        //TODO: Render preview for Biomes

        this.renderYaw = this.renderYaw + 0.015F;
        if (renderTarget instanceof Block) {
            Block block = (Block)renderTarget;
            getMinecraft().getItemRenderer().renderAndDecorateFakeItem(block.asItem().getDefaultInstance(),this.getGuiLeft() + 120, this.getGuiTop() + 55);
        } else if (renderTarget instanceof Entity) {
            // Setup position
            Entity entity = (Entity)renderTarget;
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) posX, (float) posY, 1050.0F);
            RenderSystem.scalef(1.0F, 1.0F, -1.0F);
            MatrixStack matrixstack = new MatrixStack();
            matrixstack.translate(0.0D, 0.0D, 1000.0D);
            matrixstack.scale((float) scale, (float) scale, (float) scale);
            Quaternion quaternion = Vector3f.ZN.rotationDegrees(175.0F);
            Quaternion quaternion1 = Vector3f.XP.rotationDegrees(0);
            quaternion.mul(quaternion1);
            matrixstack.mulPose(quaternion);

            // Set entity rotation
            boolean isCustomNameVisible = entity.isCustomNameVisible();
            float renderYawOffset = 0.0F;
            float rotationYaw = entity.xRot;
            float rotationYawHead = 0.0F;
            float rotationPitch = entity.yRot;
            entity.setCustomNameVisible(false);
            entity.yRot = 180.0F + this.renderYaw * 40.0F;
            entity.xRot = 0.0F;
            if (entity instanceof LivingEntity) {
                renderYawOffset = ((LivingEntity) entity).yBodyRot;
                rotationYawHead = ((LivingEntity) entity).yHeadRot;
                ((LivingEntity) entity).yBodyRot = 180.0F + this.renderYaw * 20.0F;
                ((LivingEntity) entity).yHeadRot = ((LivingEntity) entity).yBodyRot;
            }

            // Render entity
            EntityRendererManager entityrenderermanager = getMinecraft().getEntityRenderDispatcher();
            quaternion1.conj();
            entityrenderermanager.overrideCameraOrientation(quaternion1);
            entityrenderermanager.setRenderShadow(false);
            IRenderTypeBuffer.Impl irendertypebuffer$impl = getMinecraft().renderBuffers().bufferSource();
            RenderSystem.runAsFancy(() -> {
                entityrenderermanager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 0xF000F0);
            });
            irendertypebuffer$impl.endBatch();
            entityrenderermanager.setRenderShadow(true);

            // Restore entity's original properties
            entity.setCustomNameVisible(isCustomNameVisible);
            entity.xRot = rotationYaw;
            entity.yRot = rotationPitch;
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).yBodyRot = renderYawOffset;
                ((LivingEntity) entity).yHeadRot = rotationYawHead;
            }
            RenderSystem.popMatrix();
        }
    }

    @Override
    protected void init() {
        super.init();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        this.targetInputField = new TextFieldWidget(this.font, this.leftPos + 10, this.topPos + 27, 72, 8, ITextComponent.nullToEmpty("Target Field"));
        this.targetInputField.setMaxLength(256);
        this.targetInputField.setTextColor(0x888888);
        this.targetInputField.setValue("Target Field");
        addButton(this.targetInputField);

        int slotIndex = this.menu.inventory.findSlotMatchingItem(this.menu.sourceItemStack);

        this.targetAddButton = addButton(new Button(this.leftPos + 8, this.topPos + 40, 76, 14, new TranslationTextComponent("gui.cubicthings.scannerMenu.add", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getValue().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 1));
            ScannerItem.addTarget(this.itemStack, this.getScannerMode(), input);
            updateWidgets();

            button.active = false;
            this.targetRemoveButton.active = true;
        }));
        this.targetAddButton.active = false;

        this.targetRemoveButton = addButton(new Button(this.leftPos + 8, this.topPos + 52, 76, 14, new TranslationTextComponent("gui.cubicthings.scannerMenu.remove", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getValue().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 2));
            ScannerItem.removeTarget(this.itemStack, this.getScannerMode(), input);
            updateWidgets();

            button.active = false;
            this.targetAddButton.active = true;
        }));
        this.targetRemoveButton.active = false;

        this.targetsClearButton = addButton(new Button(this.leftPos + 8, this.topPos + 66, 76, 14, new TranslationTextComponent("gui.cubicthings.scannerMenu.clear", getScannerMode().toTitleCase()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getValue().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 3));
            getScannerMode().getTargetList(this.itemStack).clear();
            updateWidgets();

            this.targetInputField.setValue("");
        }));
        this.targetsClearButton.active = getStoredTargetSet().size() > 0;

        this.targetModeButton = addButton(new Button(this.leftPos + 6, this.topPos + 84, 80, 20, currentTargetValue(), (button) -> {
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
            String oldValue = this.targetInputField.getValue().replaceFirst("minecraft:", "").trim();
            String newValue = targetEntry.getDisplayText().getString().replaceFirst("minecraft:", "").trim();
            if (!this.clickedTargetField) {
                this.targetInputField.setTextColor(0xDDDD44);
                this.clickedTargetField = true;
            }
            this.setFocused(targetInputField);
            this.targetInputField.setValue(targetEntry.getDisplayText().getString().replaceFirst("minecraft:", "").trim());
            if (!this.targetInputField.getValue().equals(oldValue))
                getMinecraft().getSoundManager().play(new SimpleSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.25F, 1.0F, 0, 0, 0));
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
                    if (this.targetAddButton.active)
                        this.targetAddButton.onPress();
                    else if (this.targetRemoveButton.active)
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
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, ((IFormattableTextComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), (float)this.titleLabelX, (float)this.titleLabelY, 0x404040);
        // Draw object texts
        if (renderTarget instanceof Block)
            this.font.draw(matrixStack, ((Block)renderTarget).getName(), 90, 16,  0xBABABA);
        else if (renderTarget instanceof Biome)
            this.font.draw(matrixStack, new TranslationTextComponent(("biome." + ((Biome) renderTarget).getRegistryName()).replace(':', '.')), 90, 16, 0xBABABA);
        else if (renderTarget instanceof Entity)
            this.font.draw(matrixStack, ((Entity)renderTarget).getDisplayName(), 90, 16, 0xBABABA);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft, "Minecraft cannot be null.").getTextureManager().bind(SCANNER_SCREEN_TEXTURE);
        this.blit(matrixStack, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.getXSize(), this.getYSize());
        if (renderTarget instanceof Block)
            drawTargetOnScreen(this.getGuiLeft() + 127, this.getGuiTop() + 98, 1);
        else if (renderTarget instanceof Biome)
            drawTargetOnScreen(this.getGuiLeft() + 127, this.getGuiTop() + 98, 1);
        else if (renderTarget instanceof Entity)
            drawTargetOnScreen(this.getGuiLeft() + 127, this.getGuiTop() + 98, (int) MathHelper.clamp(this.getYSize() / Math.max(((Entity)renderTarget).getDimensions(Pose.STANDING).width * 3, ((Entity)renderTarget).getDimensions(Pose.STANDING).height * 3), this.getYSize() / 10.0F, this.getYSize() / 4.0F));
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);
        this.updateWidgets();
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.targetList.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

}
