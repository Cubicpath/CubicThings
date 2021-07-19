////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.widget.TextListWidget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigScreen extends Screen implements ITextListHolder {
    protected boolean clickedKeyField, clickedValueField, invalidKey, invalidValue = false;
    protected final Screen prevScreen;
    protected Button cancelButton, submitChangeButton;
    protected TextFieldWidget keyInputField, valueInputField;
    protected TextListWidget configList;

    public ConfigScreen(Screen prevScreen) {
        super(new TranslationTextComponent("gui.modConfig", CubicThings.MODNAME));
        this.prevScreen = prevScreen;
    }

    void updateWidgets(){
        if (!this.clickedKeyField && this.keyInputField.isFocused()) {
            this.keyInputField.setText("");
            this.clickedKeyField = true;
        }
        if (!this.clickedValueField && this.valueInputField.isFocused()) {
            this.valueInputField.setText("");
            this.clickedValueField = true;
        }

        if (this.clickedKeyField && this.keyInputField.isFocused()){
            this.keyInputField.setTextColor(0xA0A0A0);
            this.invalidKey = false;
        }

        if (this.clickedValueField && this.valueInputField.isFocused()){
            this.valueInputField.setTextColor(0xA0A0A0);
            this.invalidValue = false;
        }

        if (CubicThings.Config.SPEC.valueMap().containsKey(this.keyInputField.getText()))
            this.keyInputField.setTextColor(0x44BA44);
        else if (!this.invalidKey)
            this.keyInputField.setTextColor(0xA0A0A0);

        this.configList.refreshList();
    }

    @Override
    public FontRenderer getFontRenderer() {
        return this.font;
    }

    @Override
    public <T extends ExtendedList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, Function<String, T> newEntry) {
        CubicThings.Config.SPEC.valueMap().keySet().forEach((key) -> {
            textListViewConsumer.accept(newEntry.apply("#" + ((ForgeConfigSpec.ValueSpec)CubicThings.Config.SPEC.valueMap().get(key)).getComment()));
            try { textListViewConsumer.accept(newEntry.apply(key + " = " + ((ForgeConfigSpec.ConfigValue<?>)CubicThings.Config.class.getField(key).get(null)).get().toString()));
            } catch (IllegalAccessException | NoSuchFieldException ignored) { }
            textListViewConsumer.accept(newEntry.apply(""));
            textListViewConsumer.accept(newEntry.apply("--------------------"));
            textListViewConsumer.accept(newEntry.apply(""));
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.keyInputField = new TextFieldWidget(this.font, this.width / 2 - 85, 30, 80, 10, ITextComponent.getTextComponentOrEmpty("Key"));
        this.keyInputField.setMaxStringLength(256);
        this.keyInputField.setTextColor(0x888888);
        this.keyInputField.setText("Key");
        addButton(this.keyInputField);

        this.valueInputField = new TextFieldWidget(this.font, this.width / 2 + 10, 30, 80, 10, ITextComponent.getTextComponentOrEmpty("Value"));
        this.valueInputField.setMaxStringLength(256);
        this.valueInputField.setTextColor(0x888888);
        this.valueInputField.setText("Value");
        addButton(this.valueInputField);

        this.cancelButton = addButton(new Button(this.width - 95, this.height - 35,80, 20, new TranslationTextComponent("gui.cancel"), (button) -> {
            // Send player back to last screen.
            getMinecraft().displayGuiScreen(this.prevScreen);
        }));

        this.submitChangeButton = addButton(new Button(this.width / 2 + 100, 25, 50, 20,  new TranslationTextComponent("gui.submit"), (button) -> {
            if (!CubicThings.Config.SPEC.valueMap().containsKey(this.keyInputField.getText())) {
                this.keyInputField.setTextColor(0xbb4444);
                this.invalidKey = true;
            } else {
                ForgeConfigSpec.ValueSpec value = (ForgeConfigSpec.ValueSpec) CubicThings.Config.SPEC.valueMap().get(this.keyInputField.getText());
                try {
                    Object o = CubicThings.Config.class.getField(this.keyInputField.getText()).get(null);
                    if (value != null && o instanceof ForgeConfigSpec.ConfigValue<?>){
                        if (value.getClazz() == String.class)
                            ((ForgeConfigSpec.ConfigValue<String>) o).set(this.valueInputField.getText());
                        else if (value.getClazz() == Integer.class)
                            ((ForgeConfigSpec.ConfigValue<Integer>) o).set(Integer.valueOf(this.valueInputField.getText()));
                        else if (value.getClazz() == Double.class)
                            ((ForgeConfigSpec.ConfigValue<Double>) o).set(Double.valueOf(this.valueInputField.getText()));
                        else if (value.getClazz() == Boolean.class)
                            ((ForgeConfigSpec.ConfigValue<Boolean>) o).set(Boolean.valueOf(this.valueInputField.getText()));

                        ((ForgeConfigSpec.ConfigValue<?>) o).save();
                        CubicThings.Config.SPEC.save();
                        this.valueInputField.setTextColor(0x44BA44);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e){
                    this.valueInputField.setTextColor(0xbb4444);
                    this.invalidValue = true;
                }
            }

        }));

        this.configList = new TextListWidget(this, 300,150,this.width / 2 - 150,50,4, 4, this.font.FONT_HEIGHT + 2, 0xC0C0C0, true, true);
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.configList.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.configList.mouseScrolled(mouseX, mouseY, delta * 4);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.configList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.configList.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack) {
        super.renderBackground(matrixStack);
    }

    public void renderForeground(MatrixStack matrixStack){
        this.font.drawText(matrixStack, ITextComponent.getTextComponentOrEmpty("="), this.width / 2.0F, 31, 0xAAAAAA);
        this.font.drawText(matrixStack, ((TextComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), 5, 5, 0xA0A0A0);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        this.configList.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderForeground(matrixStack);
    }

    @Override
    public void tick() {
        updateWidgets();
        super.tick();
    }

}
