////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.widget.TextListWidget;
import com.cubicpath.cubicthings.common.container.ScannerContainer;
import com.cubicpath.cubicthings.common.item.ScannerItem;
import com.cubicpath.cubicthings.common.network.CScannerModePacket;
import com.cubicpath.cubicthings.common.network.CScannerTargetPacket;
import com.cubicpath.cubicthings.core.init.NetworkInit;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Information on how to render the Scanner's internal container.
 *
 * @since 0.2.2
 * @author Cubicpath
 */
@SuppressWarnings("deprecation")
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
    protected TextListWidget targetList;

    public ScannerScreen(ScannerContainer screenContainer, PlayerInventory inventory, ITextComponent titleIn) {
        super(screenContainer, inventory, titleIn);
        this.itemStack = screenContainer.sourceItemStack;
        this.world = (ClientWorld)inventory.player.world;
        this.xSize = 176;
        this.ySize = 166;
        this.titleX = 9;
    }

    protected static LivingEntity createNewDummyPlayer(ClientWorld world){
        RemoteClientPlayerEntity dummyPlayer = new RemoteClientPlayerEntity(world, new GameProfile(null, "Player"));
        dummyPlayer.setCustomNameVisible(false);
        return dummyPlayer;
    }

    protected Set<String> getStoredTargetSet(){
        Set<String> targetSet = new HashSet<>();
        getScannerMode().getTargetList(this.itemStack).forEach(stringNBT -> targetSet.add(stringNBT.getString()));
        return targetSet;
    }

    protected ScannerItem.ScannerMode getScannerMode(){
        return ScannerItem.getScannerMode(this.itemStack);
    }

    protected ITextComponent currentTargetText() {
        return new TranslationTextComponent("gui.cubicthings.scannerMenu.target", getScannerMode().toTitleCase());
    }

    public FontRenderer getFontRenderer()
    {
        return this.font;
    }

    public <T extends ExtendedList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, Function<String, T> newEntry) {
        getStoredTargetSet().stream().sorted().forEach(string -> textListViewConsumer.accept(newEntry.apply(string)));
    }

    public void updateWidgets(){
        boolean validInput = !this.targetInputField.getText().trim().isEmpty() && ResourceLocation.isResouceNameValid(this.targetInputField.getText().toLowerCase().trim());
        ResourceLocation resourceLocation = validInput ? new ResourceLocation(this.targetInputField.getText().toLowerCase().trim()) : null;

        if (!this.clickedTargetField && this.targetInputField.isFocused()) {
            this.targetInputField.setText("");
            this.targetInputField.setTextColor(0xDDDD44);
            this.clickedTargetField = true;
        }

        switch (getScannerMode()){
            case BLOCKS: {
                Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                if (block != null){
                    renderTarget = block;
                } else renderTarget = Blocks.AIR;

                break;
            }

            case BIOMES:
                Biome biome = ForgeRegistries.BIOMES.getValue(resourceLocation);
                if (biome != null){
                    renderTarget = biome;
                } else renderTarget = Biomes.THE_VOID;

                break;

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
        this.targetAddButton.active = validInput && getStoredTargetSet().stream().noneMatch((string) -> string.equals(this.targetInputField.getText().toLowerCase().trim()) || string.replaceFirst("minecraft:", "").equals(this.targetInputField.getText().toLowerCase().trim()));

        this.targetRemoveButton.setMessage(new TranslationTextComponent("gui.cubicthings.scannerMenu.remove", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()));
        this.targetRemoveButton.active = validInput && getStoredTargetSet().stream().anyMatch((string) -> string.equals(this.targetInputField.getText().toLowerCase().trim()) || string.replaceFirst("minecraft:", "").equals(this.targetInputField.getText().toLowerCase().trim()));

        this.targetsClearButton.setMessage(new TranslationTextComponent("gui.cubicthings.scannerMenu.clear", getScannerMode().toTitleCase()));
        this.targetsClearButton.active = getStoredTargetSet().size() > 0;

        this.targetList.refreshList();
    }

    //uses net.minecraft.client.gui.screen.inventory::drawEntityOnScreen math
    public void drawTargetOnScreen(int posX, int posY, int scale) {
        //TODO: Render 3D preview for Blocks
        //TODO: Render preview for Biomes

        this.renderYaw = this.renderYaw + 0.015F;
        if (renderTarget instanceof Block) {
            Block block = (Block)renderTarget;
            getMinecraft().getItemRenderer().renderItemAndEffectIntoGUI(block.asItem().getDefaultInstance(),this.guiLeft + 120, this.guiTop + 55);
        }
        else if (renderTarget instanceof Entity) {
            Entity entity = (Entity)renderTarget;

            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) posX, (float) posY, 1050.0F);
            RenderSystem.scalef(1.0F, 1.0F, -1.0F);
            MatrixStack matrixstack = new MatrixStack();
            matrixstack.translate(0.0D, 0.0D, 1000.0D);
            matrixstack.scale((float) scale, (float) scale, (float) scale);
            Quaternion quaternion = Vector3f.ZN.rotationDegrees(175.0F);
            Quaternion quaternion1 = Vector3f.XP.rotationDegrees(0);
            quaternion.multiply(quaternion1);
            matrixstack.rotate(quaternion);
            boolean isCustomNameVisible = entity.isCustomNameVisible();
            float renderYawOffset = 0.0F;
            float rotationYaw = entity.rotationYaw;
            float rotationYawHead = 0.0F;
            float rotationPitch = entity.rotationPitch;
            entity.setCustomNameVisible(false);
            entity.rotationYaw = 180.0F + this.renderYaw * 40.0F;
            entity.rotationPitch = 0.0F;
            if (entity instanceof LivingEntity) {
                renderYawOffset = ((LivingEntity) entity).renderYawOffset;
                rotationYawHead = ((LivingEntity) entity).rotationYawHead;
                ((LivingEntity) entity).renderYawOffset = 180.0F + this.renderYaw * 20.0F;
                ((LivingEntity) entity).rotationYawHead = ((LivingEntity) entity).renderYawOffset;
            }
            EntityRendererManager entityrenderermanager = getMinecraft().getRenderManager();
            quaternion1.conjugate();
            entityrenderermanager.setCameraOrientation(quaternion1);
            entityrenderermanager.setRenderShadow(false);
            IRenderTypeBuffer.Impl irendertypebuffer$impl = getMinecraft().getRenderTypeBuffers().getBufferSource();
            RenderSystem.runAsFancy(() -> {
                entityrenderermanager.renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 0xF000F0);
            });
            irendertypebuffer$impl.finish();
            entityrenderermanager.setRenderShadow(true);
            entity.setCustomNameVisible(isCustomNameVisible);
            entity.rotationYaw = rotationYaw;
            entity.rotationPitch = rotationPitch;
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).renderYawOffset = renderYawOffset;
                ((LivingEntity) entity).rotationYawHead = rotationYawHead;
            }
            RenderSystem.popMatrix();
        }
    }

    @Override
    protected void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.targetInputField = new TextFieldWidget(this.font, this.guiLeft + 10, this.guiTop + 27, 72, 8, ITextComponent.getTextComponentOrEmpty("Target Field"));
        this.targetInputField.setMaxStringLength(256);
        this.targetInputField.setTextColor(0x888888);
        this.targetInputField.setText("Target Field");
        addButton(this.targetInputField);

        int slotIndex = this.container.inventory.getSlotFor(container.sourceItemStack);

        this.targetAddButton = addButton(new Button(this.guiLeft + 8, this.guiTop + 40, 76, 14, new TranslationTextComponent("gui.cubicthings.scannerMenu.add", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getText().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 1));
            ScannerItem.addTarget(this.itemStack, this.getScannerMode(), input);
            updateWidgets();

            button.active = false;
            this.targetRemoveButton.active = true;
        }));
        this.targetAddButton.active = false;

        this.targetRemoveButton = addButton(new Button(this.guiLeft + 8, this.guiTop + 52, 76, 14, new TranslationTextComponent("gui.cubicthings.scannerMenu.remove", getScannerMode().toTitleCase().replace("ies", "y").concat("\t").replace("s\t", "").trim()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getText().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 2));
            ScannerItem.removeTarget(this.itemStack, this.getScannerMode(), input);
            updateWidgets();

            button.active = false;
            this.targetAddButton.active = true;
        }));
        this.targetRemoveButton.active = false;

        this.targetsClearButton = addButton(new Button(this.guiLeft + 8, this.guiTop + 66, 76, 14, new TranslationTextComponent("gui.cubicthings.scannerMenu.clear", getScannerMode().toTitleCase()), (button) -> {
            String input = new ResourceLocation(this.targetInputField.getText().toLowerCase().replace(" ","")).toString();
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerTargetPacket(input, slotIndex, (byte) 3));
            getScannerMode().getTargetList(this.itemStack).clear();
            updateWidgets();

            this.targetInputField.setText("");
        }));
        this.targetsClearButton.active = getStoredTargetSet().size() > 0;

        this.targetModeButton = addButton(new Button(this.guiLeft + 6, this.guiTop + 84, 80, 20, currentTargetText(), (button) -> {
            ScannerItem.ScannerMode scannerMode;
            try { scannerMode = ScannerItem.ScannerMode.values()[getScannerMode().ordinal() + 1]; }
            catch (ArrayIndexOutOfBoundsException e) { scannerMode = ScannerItem.ScannerMode.values()[0]; }

            ScannerItem.setScannerMode(this.itemStack, scannerMode);
            NetworkInit.PACKET_HANDLER.sendToServer(new CScannerModePacket(scannerMode, slotIndex));
            button.setMessage(currentTargetText());
            this.targetInputField.setText("");
            updateWidgets();
        }));
        this.targetModeButton.active = true;

        this.targetList = new TextListWidget(this, 156, 50, this.guiTop + 109, this.guiLeft + 7, 2, -2 , this.font.FONT_HEIGHT + 2, 0xCCCCCC, true, false);

    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.targetInputField.isFocused() && this.targetInputField.getVisible()){
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
                case GLFW.GLFW_KEY_ENTER:
                case GLFW.GLFW_KEY_TAB: {
                    this.targetInputField.setFocused2(false);
                    break;

                }
                case GLFW.GLFW_KEY_BACKSPACE: {
                    this.targetInputField.deleteFromCursor(-1);
                    break;
                }
            }

            return keyCode != GLFW.GLFW_KEY_ESCAPE || super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        this.font.drawText(matrixStack, ((TextComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), (float)this.titleX, (float)this.titleY, 0x404040);
        // Draw object texts
        if (renderTarget instanceof Block)
            this.font.drawText(matrixStack, ((Block)renderTarget).getTranslatedName(), 90, 16,  0xBABABA);
        else if (renderTarget instanceof Biome)
            this.font.drawText(matrixStack, new TranslationTextComponent(("biome." + ((Biome) renderTarget).getRegistryName()).replace(':', '.')), 90, 16, 0xBABABA);
        else if (renderTarget instanceof Entity)
            this.font.drawText(matrixStack, ((Entity)renderTarget).getDisplayName(), 90, 16, 0xBABABA);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft, "Minecraft cannot be null.").getTextureManager().bindTexture(SCANNER_SCREEN_TEXTURE);
        this.blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (renderTarget instanceof Block)
            drawTargetOnScreen(this.guiLeft + 127, this.guiTop + 98, 1);
        else if (renderTarget instanceof Biome)
            drawTargetOnScreen(this.guiLeft + 127, this.guiTop + 98, 1);
        else if (renderTarget instanceof Entity)
            drawTargetOnScreen(this.guiLeft + 127, this.guiTop + 98, (int) MathHelper.clamp(this.ySize / Math.max(((Entity)renderTarget).getSize(Pose.STANDING).width * 3, ((Entity)renderTarget).getSize(Pose.STANDING).height * 3), this.ySize / 10.0F, this.ySize / 4.0F));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.targetList.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        updateWidgets();
        super.tick();
    }

}
