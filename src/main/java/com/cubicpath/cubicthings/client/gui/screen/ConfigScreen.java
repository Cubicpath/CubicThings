////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.widget.ConfigListWidget;
import com.cubicpath.cubicthings.core.config.BaseConfig;
import com.cubicpath.util.ComponentUtils;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ConfigScreen extends Screen implements ITextListHolder {
    protected static boolean hideComments, hideSeparators = false;

    protected boolean clickedKeyField = false, clickedValueField = false, invalidKey = false, invalidValue = false, foundKey = false;
    protected final Integer[] colors = new Integer[11];
    protected String seperatorChars;
    protected Integer separatorLength, separatorHeight;
    protected Boolean showIntsAsHex, showBytesAsBinary, showFullKeyNames;
    protected Button cancelButton, toggleCommentsButton, toggleSeparatorsButton, submitChangeButton;
    protected EditBox keyInputField, valueInputField;
    protected ConfigListWidget configList;
    protected final Screen prevScreen;
    protected final BaseConfig config;

    public ConfigScreen(Screen prevScreen, String configName, BaseConfig modConfig) {
        super(new TranslatableComponent("modConfig.title", configName));
        this.config = modConfig;
        this.prevScreen = prevScreen;
    }

    protected void updateWidgets(){
        reloadMenuConfigs();

        if ((!this.clickedKeyField || this.invalidKey) && this.keyInputField.isFocused()) {
            this.keyInputField.setValue("");
            this.keyInputField.setTextColor(0xA0A0A0);
            this.invalidKey = false;
            this.clickedKeyField = true;
        }

        if ((!this.clickedValueField || this.invalidValue) && this.valueInputField.isFocused()) {
            this.valueInputField.setValue("");
            this.valueInputField.setTextColor(0xA0A0A0);
            this.invalidValue = false;
            this.clickedValueField = true;
        }

        if (!this.keyInputField.getValue().trim().endsWith(".") && this.config.isValueSpecInPath(this.keyInputField.getValue().trim().split("\\.")))
            this.keyInputField.setTextColor(this.colors[0]);
        else if (!this.invalidKey)
            this.keyInputField.setTextColor(0xA0A0A0);

        this.toggleCommentsButton.setMessage(new TranslatableComponent(hideComments ? "modConfig.comments.hidden" : "modConfig.comments.shown"));
        this.toggleSeparatorsButton.setMessage(new TranslatableComponent(hideSeparators ? "modConfig.separators.hidden" : "modConfig.separators.shown"));

        this.configList.refreshList();
    }

    public BaseConfig getConfig(){
        return this.config;
    }

    @Override
    public Font getFontRenderer() {
        return this.font;
    }

    public <T extends AbstractSelectionList.Entry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<Component, String, T> newEntry) {
        addTextEntries(this.config.getSpec().valueMap(), textListViewConsumer, newEntry);
    }

    public <T extends AbstractSelectionList.Entry<T>> void addTextEntries(Map<String, Object> valueMap, Consumer<T> textListViewConsumer, BiFunction<Component, String, T> newEntry){
        addTextEntries(valueMap, textListViewConsumer, newEntry, null, 0);
        textListViewConsumer.accept(newEntry.apply(TextComponent.EMPTY, null)); // Empty entry to help with clipping issues
    }

    protected <T extends AbstractSelectionList.Entry<T>> void addTextEntries(Map<String, Object> valueMap, Consumer<T> textListViewConsumer, BiFunction<Component, String, T> newEntry, @Nullable String prevConfigName, final int depth){
        valueMap.keySet().forEach((key) -> {
            String pathName = prevConfigName != null ? prevConfigName + "." + key : key;
            String comment = this.config.getComment(pathName);
            if (valueMap.get(key) instanceof AbstractConfig abstractConfig) {
                if (!hideComments && comment != null)
                    for (String string : comment.split("\\n\\r | [\\n\\r]"))
                        textListViewConsumer.accept(newEntry.apply(ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4 - 1) + "#" + string.replace("\t", "    "), this.colors[4]), null));

                textListViewConsumer.accept(newEntry.apply(
                        ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4)).append
                                (ComponentUtils.stringToText("[", 0xAAAAAA)).append
                                (ComponentUtils.stringToText(pathName, this.colors[3])).append
                                (ComponentUtils.stringToText("]", 0xAAAAAA)),
                        null));

                addTextEntries(abstractConfig.valueMap(), textListViewConsumer, newEntry, pathName, depth + 1);
            } else {
                if (!hideComments && comment != null)
                    for (String string : comment.split("\\n\\r | [\\n\\r]"))
                        textListViewConsumer.accept(newEntry.apply(ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4 - 1) + "#" + string.replace("\t", "    "), this.colors[4]), null));

                //Format text based on type
                MutableComponent outputBase = ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4) + (this.showFullKeyNames ? pathName : key), this.colors[2]).append(ComponentUtils.stringToText(" = ", 0xA0A0A0));
                textListViewConsumer.accept(newEntry.apply(outputBase.append(getValueComponent(pathName.split("\\."))), pathName));

                if (!hideSeparators)
                    for (int i = 0; i < separatorHeight; i++)
                        textListViewConsumer.accept(newEntry.apply(ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4) + StringUtils.repeat(this.seperatorChars, this.separatorLength), this.colors[5]), null));
            }
        });
    }

    public void doConfigChange(){
        doConfigChange(this.config.getSpec().valueMap(), null, 0);
    }

    /** Magic that saves values to config file. */
    @SuppressWarnings("unchecked")
    protected void doConfigChange(Map<String, Object> valueMap, @Nullable AbstractConfig nestedConfig, final int depth){
        String[] splitInput = this.keyInputField.getValue().trim().split("\\.");
        String lastSplit = splitInput[splitInput.length - 1];
        if (!valueMap.containsKey(lastSplit) && valueMap.values().stream().noneMatch((o) -> o instanceof AbstractConfig)) {
            this.keyInputField.setTextColor(this.colors[1]);
            this.invalidKey = true;
        } else {
            try {
                String valueInput = this.valueInputField.getValue().trim();
                Object valueSpec = nestedConfig == null ? valueMap.get(lastSplit) : nestedConfig.get(lastSplit);
                var configValue = this.config.getConfigValue(StringUtils.join(splitInput, '.'));
                boolean isTruthy = StringUtils.equalsAnyIgnoreCase(valueInput, "true", "yes", "y", "t") || (StringUtils.isNumeric(valueInput) && Integer.parseInt(valueInput) == 1);
                boolean isFalsy = StringUtils.equalsAnyIgnoreCase(valueInput, "false", "no", "n", "f") || (StringUtils.isNumeric(valueInput) && Integer.parseInt(valueInput) == 0);
                int radix = StringUtils.startsWithAny(valueInput.toLowerCase(), "0x", "#") ? 16
                        : StringUtils.startsWithAny(valueInput.toLowerCase(), "0o", "o") ? 8
                        : StringUtils.startsWithAny(valueInput.toLowerCase(), "0b", "b") ? 2
                        : 10;
                if (valueSpec instanceof ForgeConfigSpec.ValueSpec && configValue != null){
                    Class<?> valueClazz = ((ForgeConfigSpec.ValueSpec) valueSpec).getClazz();

                    if (valueClazz == List.class)           ((ForgeConfigSpec.ConfigValue<List<?>>) configValue).set(Arrays.stream(StringUtils.strip(valueInput, "[]").split(", ")).toList());
                    else if (valueClazz == String.class)    ((ForgeConfigSpec.ConfigValue<String>)  configValue).set(valueInput);
                    else if (valueClazz == Short.class)     ((ForgeConfigSpec.ConfigValue<Short>)   configValue).set(Short.valueOf(StringUtils.stripStart(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b|b)", "0"), "0"), radix));
                    else if (valueClazz == Integer.class)   ((ForgeConfigSpec.ConfigValue<Integer>) configValue).set(Integer.valueOf(StringUtils.stripStart(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b|b)", "0"), "0"), radix));
                    else if (valueClazz == Long.class)      ((ForgeConfigSpec.ConfigValue<Long>)    configValue).set(Long.valueOf(StringUtils.stripStart(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b|b)", "0"), "0"), radix));
                    else if (valueClazz == Float.class)     ((ForgeConfigSpec.ConfigValue<Float>)   configValue).set(Float.valueOf(valueInput));
                    else if (valueClazz == Double.class)    ((ForgeConfigSpec.ConfigValue<Double>)  configValue).set(Double.valueOf(valueInput));
                    else if (valueClazz == Byte.class) {
                        int intValue = Integer.parseInt(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b)", "0"), radix);
                        if (intValue <= 255)                ((ForgeConfigSpec.ConfigValue<Byte>)    configValue).set((byte) intValue);
                        else throw new NumberFormatException("Bad byte value: \"" + valueInput + "\"");
                    } else if (valueClazz == Boolean.class) {
                        if (isTruthy || isFalsy)            ((ForgeConfigSpec.ConfigValue<Boolean>) configValue).set(isTruthy && !isFalsy);
                        else throw new Exception("Bad boolean value: \"" + valueInput + "\"");
                    }

                    // Save changes.
                    configValue.save();
                    this.foundKey = true;
                    this.invalidKey = false;
                    this.valueInputField.setTextColor(this.colors[0]);
                } else if (valueMap.get(splitInput[depth]) instanceof AbstractConfig abstractConfig) {
                    // Call same function if next value in path translates to an AbstractConfig
                    doConfigChange(abstractConfig.valueMap(), abstractConfig, depth + 1);
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
                    this.keyInputField.setTextColor(this.colors[1]);
                } else {
                    this.invalidValue = true;
                    this.valueInputField.setTextColor(this.colors[1]);
                }
                CubicThings.LOGGER.info("ConfigScreen Value Error: " + e);
            }
        }
    }

    protected void reloadMenuConfigs(){
        var menuPath = "configMenu.";
        var seperatorPath = menuPath + "seperator.";
        var colorPath = menuPath + "color.";
        this.showFullKeyNames   =    this.config.getValue(menuPath  + "fullKeyNames",       Boolean.class);
        this.showBytesAsBinary  =    this.config.getValue(menuPath  + "bytesAsBinary",      Boolean.class);
        this.showIntsAsHex      =    this.config.getValue(menuPath  + "integersAsHex",      Boolean.class);
        this.seperatorChars     =    this.config.getValue(seperatorPath  + "chars",         String.class);
        this.separatorLength    =    this.config.getValue(seperatorPath  + "length",        Integer.class);
        this.separatorHeight    =    this.config.getValue(seperatorPath  + "height",        Integer.class);
        this.colors[0]          =    this.config.getValue(colorPath + "good",               Integer.class);
        this.colors[1]          =    this.config.getValue(colorPath + "fail",               Integer.class);
        this.colors[2]          =    this.config.getValue(colorPath + "keys",               Integer.class);
        this.colors[3]          =    this.config.getValue(colorPath + "subConfigs",         Integer.class);
        this.colors[4]          =    this.config.getValue(colorPath + "comments",           Integer.class);
        this.colors[5]          =    this.config.getValue(colorPath + "separators",         Integer.class);
        this.colors[6]          =    this.config.getValue(colorPath + "strings",            Integer.class);
        this.colors[7]          =    this.config.getValue(colorPath + "booleans",           Integer.class);
        this.colors[8]          =    this.config.getValue(colorPath + "bytes",              Integer.class);
        this.colors[9]          =    this.config.getValue(colorPath + "integers",           Integer.class);
        this.colors[10]         =    this.config.getValue(colorPath + "floats",             Integer.class);
    }

    @Nonnull
    private MutableComponent getValueComponent(@Nullable String[] path){
        return getValueComponent(this.config.getValue(StringUtils.join(path, '.'), Object.class));
    }

    @Nonnull
    private MutableComponent getValueComponent(@Nullable Object o){
        if (o instanceof String s)                                                                      return ComponentUtils.stringToText('"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"', this.colors[6]);
        if (o instanceof Boolean)                                                                       return ComponentUtils.stringToText(o.toString(), this.colors[7]);
        if (o instanceof Byte && this.showBytesAsBinary)                                                return ComponentUtils.stringToText("0b" + StringUtils.repeat('0', 8 - Integer.toBinaryString((byte) o & 0xFF).length()) + Integer.toBinaryString((byte) o & 0xFF), this.colors[8]);
        if (o instanceof Byte && this.showIntsAsHex)                                                    return ComponentUtils.stringToText("0x" + Integer.toHexString((Byte)o & 0xFF).toUpperCase(), this.colors[8]);
        if (o instanceof Byte b)                                                                        return ComponentUtils.stringToText(((Integer)(b & 0xFF)).toString(), this.colors[8]);
        if ((o instanceof Short || o instanceof Integer || o instanceof Long) && this.showIntsAsHex)    return ComponentUtils.stringToText("0x" + Long.toHexString(((Number) o).longValue()).toUpperCase(), this.colors[9]);
        if (o instanceof Short || o instanceof Integer || o instanceof Long)                            return ComponentUtils.stringToText(o.toString(), this.colors[9]);
        if (o instanceof Float || o instanceof Double)                                                  return ComponentUtils.stringToText(o.toString(), this.colors[10]);
        if (o instanceof List<?> l) {
            var mutableComponent = ComponentUtils.stringToText("[", 0xAAAAAA);
            for (int i = 0; i < l.size() ; i++){
                mutableComponent.append(getValueComponent(l.get(i)));
                if (i != l.size() - 1)
                    mutableComponent.append(ComponentUtils.stringToText(", ", 0xAAAAAA));
            }                                                                                           return mutableComponent.append(ComponentUtils.stringToText("]", 0xAAAAAA));
        }
        if (o != null)                                                                                  return ComponentUtils.stringToText(o.toString(), this.colors[2]);
        return TextComponent.EMPTY.plainCopy();
    }

    @Override
    protected void init() {
        super.init();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        this.keyInputField = new EditBox(this.font, this.width / 2 - 135, 25, 130, 10, Component.nullToEmpty("Key"));
        this.keyInputField.setMaxLength(256);
        this.keyInputField.setTextColor(0x888888);
        this.keyInputField.setValue("Key");
        addRenderableWidget(this.keyInputField);

        this.valueInputField = new EditBox(this.font, this.width / 2 + 10, 25, 80, 10, Component.nullToEmpty("Value"));
        this.valueInputField.setMaxLength(256);
        this.valueInputField.setTextColor(0x888888);
        this.valueInputField.setValue("Value");
        addRenderableWidget(this.valueInputField);

        this.toggleCommentsButton = addRenderableWidget(new Button(this.width / 2 - 150, this.height - 30, 110, 20, new TranslatableComponent(hideComments ? "modConfig.comments.hidden" : "modConfig.comments.shown"), (button) -> {
            // Toggles comment hiding
            hideComments = !hideComments;
        }));

        this.toggleSeparatorsButton = addRenderableWidget(new Button(this.width / 2 - 40, this.height - 30, 110, 20, new TranslatableComponent(hideComments ? "modConfig.separators.hidden" : "modConfig.separators.shown"), (button) -> {
            // Toggles separator hiding
            hideSeparators = !hideSeparators;
        }));

        this.cancelButton = addRenderableWidget(new Button(this.width / 2 + 86, this.height - 30, 70, 20, new TranslatableComponent("gui.cancel"), (button) -> {
            // Send player back to last screen.
            getMinecraft().setScreen(this.prevScreen);
        }));

        this.submitChangeButton = addRenderableWidget(new Button(this.width / 2 + 100, 20, 50, 20,  new TranslatableComponent("gui.submit"), (button) -> {
            // Change a config value with a given input.
            doConfigChange();
        }));

        this.configList = new ConfigListWidget(this, 300,this.height - 75,this.width / 2 - 150,45,4, 4, this.font.lineHeight + 2, 0xC0C0C0, true, true, this.font, (configEntry) -> {
            var oldValue = this.keyInputField.getValue();
            var valueString = getValueComponent(configEntry.get().configValue.getPath().toArray(new String[0])).getString().replace("\\\"","\"").replace("\\\\", "\\");
            if (configEntry.get().configValue.get() instanceof String) valueString = valueString.substring(1, valueString.length() - 1);
            if (configEntry.get().configValue.get() instanceof List<?>) {
                StringBuilder s = new StringBuilder("[");
                for (String string : StringUtils.strip(valueString, "[]").split(", ")) {
                    s.append(StringUtils.strip(string, "\"")).append(", ");
                }
                valueString = s.append("]").toString();
            }

            this.clickedKeyField = false;
            this.clickedValueField = false;

            this.invalidValue = false;
            this.valueInputField.setValue(valueString);
            this.valueInputField.moveCursorToStart();
            this.valueInputField.setTextColor(0xA0A0A0);

            this.invalidKey = false;
            this.keyInputField.setValue(StringUtils.join(configEntry.get().configValue.getPath().toArray(), '.'));
            this.keyInputField.moveCursorToStart();

            if (!this.keyInputField.getValue().equals(oldValue))
                getMinecraft().getSoundManager().play(new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 0.25F, 1.0F, 0, 0, 0));
        });

        reloadMenuConfigs();
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.valueInputField.isFocused() && this.valueInputField.isVisible()){
            switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER -> this.submitChangeButton.onPress();
                case GLFW.GLFW_KEY_ESCAPE -> this.valueInputField.setFocus(false);
            }

            // If key printable, default value color.
            if (keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_WORLD_2) this.valueInputField.setTextColor(0xA0A0A0);
            return keyCode != GLFW.GLFW_KEY_ESCAPE && super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
    public void renderBackground(PoseStack PoseStack) {
        super.renderBackground(PoseStack);
    }

    public void renderForeground(PoseStack PoseStack){
        this.font.draw(PoseStack, Component.nullToEmpty("="), this.width / 2.0F, 26, 0xAAAAAA);
        this.font.draw(PoseStack, ((MutableComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), 5, 5, 0xA0A0A0);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.updateWidgets();
        this.configList.render(poseStack, mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderForeground(poseStack);
    }

    @Override
    public void tick() {
        super.tick();
    }

}
