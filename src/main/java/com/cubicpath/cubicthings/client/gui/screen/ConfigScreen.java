////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.widget.TextListWidget;

import com.electronwill.nightconfig.core.AbstractConfig;
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
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ConfigScreen extends Screen implements ITextListHolder {
    protected static boolean hideComments, hideSeparators = false;

    protected boolean clickedKeyField = false, clickedValueField = false, invalidKey = false, invalidValue = false, foundKey = false;
    protected final Screen prevScreen;
    protected Button cancelButton, toggleCommentsButton, toggleSeparatorsButton, submitChangeButton;
    protected TextFieldWidget keyInputField, valueInputField;
    protected TextListWidget configList;

    public ConfigScreen(Screen prevScreen) {
        super(new TranslationTextComponent("modConfig.title", CubicThings.MODNAME));
        this.prevScreen = prevScreen;
    }

    void updateWidgets(){
        if ((!this.clickedKeyField || this.invalidKey) && this.keyInputField.isFocused()) {
            this.keyInputField.setText("");
            this.keyInputField.setTextColor(0xA0A0A0);
            this.invalidKey = false;
            this.clickedKeyField = true;
        }

        if ((!this.clickedValueField || this.invalidValue) && this.valueInputField.isFocused()) {
            this.valueInputField.setText("");
            this.valueInputField.setTextColor(0xA0A0A0);
            this.invalidValue = false;
            this.clickedValueField = true;
        }

        if (!this.keyInputField.getText().trim().endsWith(".") && CubicThings.Config.isValueSpecInConfigSpec(CubicThings.Config.SPEC, this.keyInputField.getText().trim().split("\\.")))
            this.keyInputField.setTextColor(CubicThings.Config.good.get());
        else if (!this.invalidKey)
            this.keyInputField.setTextColor(0xA0A0A0);

        this.toggleCommentsButton.setMessage(new TranslationTextComponent(hideComments ? "modConfig.cubicthings.comments.hidden" : "modConfig.cubicthings.comments.shown"));
        this.toggleSeparatorsButton.setMessage(new TranslationTextComponent(hideSeparators ? "modConfig.cubicthings.separators.hidden" : "modConfig.cubicthings.separators.shown"));

        this.configList.refreshList();
    }

    @Override
    public FontRenderer getFontRenderer() {
        return this.font;
    }

    @Override
    public <T extends ExtendedList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<String, Integer, T> newEntry) {
        addTextEntries(CubicThings.Config.SPEC.valueMap(), textListViewConsumer, newEntry);
    }

    public <T extends ExtendedList.AbstractListEntry<T>> void addTextEntries(Map<String, Object> valueMap, Consumer<T> textListViewConsumer, BiFunction<String, Integer, T> newEntry){
        addTextEntries(valueMap, textListViewConsumer, newEntry, null, 0);
    }

    private <T extends ExtendedList.AbstractListEntry<T>> void addTextEntries(Map<String, Object> valueMap, Consumer<T> textListViewConsumer, BiFunction<String, Integer, T> newEntry, @Nullable String prevConfigName, final int depth){
        valueMap.keySet().forEach((key) -> {
            String pathName = prevConfigName != null ? prevConfigName + "." + key : key;
            if (valueMap.get(key) instanceof AbstractConfig) {
                AbstractConfig abstractConfig = (AbstractConfig) valueMap.get(key);
                //if (!hideComments) textListViewConsumer.accept(newEntry.apply(StringUtils.repeat(' ', depth) + "#" + abstractConfig.contains(key), 0x8190A0));
                textListViewConsumer.accept(newEntry.apply(StringUtils.repeat(' ', depth * 4) + "\u00A77[\u00A7r" + pathName + "\u00A77]", 0xDB7F41));
                addTextEntries(abstractConfig.valueMap(), textListViewConsumer, newEntry, pathName, depth + 1);
            } else {
                if (!hideComments && valueMap.get(key) instanceof ForgeConfigSpec.ValueSpec && ((ForgeConfigSpec.ValueSpec)valueMap.get(key)).getComment() != null && !((ForgeConfigSpec.ValueSpec)valueMap.get(key)).getComment().isEmpty())
                    textListViewConsumer.accept(newEntry.apply(StringUtils.repeat(' ', depth * 4 - 2) + "#" + ((ForgeConfigSpec.ValueSpec)valueMap.get(key)).getComment(), 0x8190A0));
                try {
                    Object o = ((ForgeConfigSpec.ConfigValue<?>)CubicThings.Config.class.getField(key).get(null)).get();
                    String outputBase = StringUtils.repeat(' ', depth * 4) + pathName + "\u00A77 = ";
                    if (o instanceof String)
                        textListViewConsumer.accept(newEntry.apply(outputBase + "\u00A72\"" + ((String) o).replace("\\", "\\\\").replace("\"", "\\\"") + "\"", 0xDB7F41));
                    else if (o instanceof Boolean)
                        textListViewConsumer.accept(newEntry.apply(outputBase + "\u00A7d" + o, 0xDB7F41));
                    else if (o instanceof Byte && CubicThings.Config.bytesAsBinary.get())
                        textListViewConsumer.accept(newEntry.apply(outputBase + "\u00A76" + StringUtils.repeat('0', 8 - Integer.toBinaryString((byte) o & 0xFF).length()) + Integer.toBinaryString((byte) o & 0xFF), 0xDB7F41));
                    else if (o instanceof Number)
                        textListViewConsumer.accept(newEntry.apply(outputBase + "\u00A76" + o, 0xDB7F41));
                    else
                        textListViewConsumer.accept(newEntry.apply(outputBase + "\u00A7r" + o.toString(), 0xDB7F41));
                } catch (IllegalAccessException | NoSuchFieldException ignored) { }
                if (!hideSeparators) textListViewConsumer.accept(newEntry.apply(StringUtils.repeat("----------", depth + 1), 0x4400AA));
            }
        });
    }

    public void doConfigChange(Map<String, Object> valueMap, Class<?> clazz){
        doConfigChange(valueMap, clazz, null, 0);
    }

    @SuppressWarnings("unchecked")
    private void doConfigChange(Map<String, Object> valueMap, Class<?> clazz, @Nullable AbstractConfig nestedConfig, final int depth){
        String[] splitInput = this.keyInputField.getText().trim().split("\\.");
        String lastSplit = splitInput[splitInput.length - 1];
        if (!valueMap.containsKey(lastSplit) && valueMap.values().stream().noneMatch((o) -> o instanceof AbstractConfig)) {
            this.keyInputField.setTextColor(CubicThings.Config.fail.get());
            this.invalidKey = true;
        } else {
            try {
                Object value = nestedConfig == null ? valueMap.get(lastSplit) : nestedConfig.get(lastSplit);
                String valueInput = this.valueInputField.getText().trim();
                if (value instanceof ForgeConfigSpec.ValueSpec){
                    // Convert String input into field values.
                    ForgeConfigSpec.ConfigValue<?> configValue = (ForgeConfigSpec.ConfigValue<?>) clazz.getField(lastSplit).get(null);
                    Class<?> valueClazz = ((ForgeConfigSpec.ValueSpec) value).getClazz();
                    if (valueClazz == String.class) {
                        ((ForgeConfigSpec.ConfigValue<String>) configValue).set(valueInput);
                    } else if (valueClazz == Byte.class) {
                        int intValue = Integer.decode(valueInput);
                        if (intValue < 255) ((ForgeConfigSpec.ConfigValue<Byte>) configValue).set((byte) intValue);
                        else throw new Exception("Bad byte value");
                    } else if (valueClazz == Short.class) {
                        ((ForgeConfigSpec.ConfigValue<Short>) configValue).set(Short.decode(valueInput));
                    } else if (valueClazz == Integer.class) {
                        ((ForgeConfigSpec.ConfigValue<Integer>) configValue).set(Integer.decode(valueInput));
                    } else if (valueClazz == Float.class) {
                        ((ForgeConfigSpec.ConfigValue<Float>) configValue).set(Float.valueOf(valueInput));
                    } else if (valueClazz == Double.class) {
                        ((ForgeConfigSpec.ConfigValue<Double>) configValue).set(Double.valueOf(valueInput));
                    }
                    else if (valueClazz == Boolean.class) {
                        boolean isTruthy = valueInput.equalsIgnoreCase("true") || valueInput.equalsIgnoreCase("yes") || valueInput.equalsIgnoreCase("y") || valueInput.equalsIgnoreCase("t") || (StringUtils.isNumeric(valueInput) && Byte.parseByte(valueInput) == 1);
                        boolean isFalsy = valueInput.equalsIgnoreCase("false") || valueInput.equalsIgnoreCase("no") || valueInput.equalsIgnoreCase("n") || valueInput.equalsIgnoreCase("f") || (StringUtils.isNumeric(valueInput) && Byte.parseByte(valueInput) == 0);
                        if (isTruthy || isFalsy) ((ForgeConfigSpec.ConfigValue<Boolean>) configValue).set(isTruthy && !isFalsy);
                        else throw new Exception("Bad boolean value");
                    }

                    // Save changes.
                    configValue.save();
                    CubicThings.Config.SPEC.save();
                    this.foundKey = true;
                    this.invalidKey = false;
                    this.valueInputField.setTextColor(CubicThings.Config.good.get());
                } else if (valueMap.get(splitInput[depth]) instanceof AbstractConfig) {
                    // Call same function if next value in path translates to an AbstractConfig
                    AbstractConfig abstractConfig = (AbstractConfig) valueMap.get(splitInput[depth]);
                    doConfigChange(abstractConfig.valueMap(), clazz, abstractConfig, depth + 1);
                    if (!this.foundKey) {
                        this.invalidKey = true;
                        throw new Exception();
                    }
                } else {
                    throw new Exception();
                }
            } catch (Exception e){
                // Do bad things
                if (!valueMap.containsKey(lastSplit) || (nestedConfig != null && nestedConfig.get(lastSplit) == null)){
                    this.invalidKey = true;
                    this.foundKey = false;
                    this.keyInputField.setTextColor(CubicThings.Config.fail.get());
                } else {
                    this.invalidValue = true;
                    this.valueInputField.setTextColor(CubicThings.Config.fail.get());
                }
                CubicThings.LOGGER.info("ConfigScreen Value Error: " + e);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.keyInputField = new TextFieldWidget(this.font, this.width / 2 - 135, 30, 130, 10, ITextComponent.getTextComponentOrEmpty("Key"));
        this.keyInputField.setMaxStringLength(256);
        this.keyInputField.setTextColor(0x888888);
        this.keyInputField.setText("Key");
        addButton(this.keyInputField);

        this.valueInputField = new TextFieldWidget(this.font, this.width / 2 + 10, 30, 80, 10, ITextComponent.getTextComponentOrEmpty("Value"));
        this.valueInputField.setMaxStringLength(256);
        this.valueInputField.setTextColor(0x888888);
        this.valueInputField.setText("Value");
        addButton(this.valueInputField);

        this.toggleCommentsButton = addButton(new Button(this.width / 2 - 150, 200, 110, 20, new TranslationTextComponent(hideComments ? "modConfig.cubicthings.comments.hidden" : "modConfig.cubicthings.comments.shown"), (button) -> {
            // Toggles comment hiding
            hideComments = !hideComments;
        }));

        this.toggleSeparatorsButton = addButton(new Button(this.width / 2 - 40, 200, 110, 20, new TranslationTextComponent(hideComments ? "modConfig.cubicthings.separators.hidden" : "modConfig.cubicthings.separators.shown"), (button) -> {
            // Toggles separator hiding
            hideSeparators = !hideSeparators;
        }));

        this.cancelButton = addButton(new Button(this.width / 2 + 80, 200, 70, 20, new TranslationTextComponent("gui.cancel"), (button) -> {
            // Send player back to last screen.
            getMinecraft().displayGuiScreen(this.prevScreen);
        }));

        this.submitChangeButton = addButton(new Button(this.width / 2 + 100, 25, 50, 20,  new TranslationTextComponent("gui.submit"), (button) -> {
            // Change a config value with a given input.
            doConfigChange(CubicThings.Config.SPEC.valueMap(), CubicThings.Config.class);
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
