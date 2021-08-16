////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.widget.ConfigListWidget;
import com.cubicpath.cubicthings.core.config.BaseConfig;
import com.cubicpath.cubicthings.core.config.ConfigMenuConfig;
import com.cubicpath.util.ComponentUtils;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.*;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
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
import java.util.stream.Collectors;

public class ConfigScreen extends Screen implements ITextListHolder {
    protected static boolean hideComments, hideSeparators = false;

    protected boolean clickedKeyField = false, clickedValueField = false, invalidKey = false, invalidValue = false, foundKey = false;
    protected Button prevConfigButton, nextConfigButton, cancelButton, toggleCommentsButton, toggleSeparatorsButton, submitChangeButton;
    protected TextFieldWidget keyInputField, valueInputField;
    protected ConfigListWidget configList;
    protected BaseConfig current;
    protected final ConfigMenuConfig menuConfig;
    protected final List<BaseConfig> configs;
    protected final Screen prevScreen;

    public ConfigScreen(Screen prevScreen, String configName, ConfigMenuConfig menuConfig, BaseConfig... modConfig) {
        super(new TranslationTextComponent("modConfig.title", configName));
        this.menuConfig = menuConfig;
        this.current = modConfig[0];
        this.configs = Arrays.asList(modConfig);
        this.prevScreen = prevScreen;
    }

    protected void updateWidgets(){
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

        if (!this.keyInputField.getValue().trim().endsWith(".") && this.current.isValueSpecInPath(this.keyInputField.getValue().trim().split("\\.")))
            this.keyInputField.setTextColor(this.menuConfig.colors[0]);
        else if (!this.invalidKey)
            this.keyInputField.setTextColor(0xA0A0A0);

        this.toggleCommentsButton.setMessage(new TranslationTextComponent(hideComments ? "modConfig.comments.hidden" : "modConfig.comments.shown"));
        this.toggleSeparatorsButton.setMessage(new TranslationTextComponent(hideSeparators ? "modConfig.separators.hidden" : "modConfig.separators.shown"));

        this.configList.refreshList();
    }

    public BaseConfig getCurrent(){
        return this.current;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return this.font;
    }

    public <T extends AbstractList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<ITextComponent, String, T> newEntry) {
        addTextEntries(this.current.getSpec().valueMap(), textListViewConsumer, newEntry);
    }

    public <T extends AbstractList.AbstractListEntry<T>> void addTextEntries(Map<String, Object> valueMap, Consumer<T> textListViewConsumer, BiFunction<ITextComponent, String, T> newEntry){
        addTextEntries(valueMap, textListViewConsumer, newEntry, null, 0);
        textListViewConsumer.accept(newEntry.apply(ITextComponent.nullToEmpty(null), null));
    }

    protected <T extends AbstractList.AbstractListEntry<T>> void addTextEntries(Map<String, Object> valueMap, Consumer<T> textListViewConsumer, BiFunction<ITextComponent, String, T> newEntry, @Nullable String prevConfigName, final int depth){
        valueMap.keySet().forEach((key) -> {
            String pathName = prevConfigName != null ? prevConfigName + "." + key : key;
            String comment = this.current.getComment(pathName);
            if (valueMap.get(key) instanceof AbstractConfig) {
                if (!hideComments && comment != null)
                    for (String string : comment.split("\\n\\r | [\\n\\r]"))
                        textListViewConsumer.accept(newEntry.apply(ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4 - 1) + "#" + string.replace("\t", "    "), this.menuConfig.colors[4]), null));

                textListViewConsumer.accept(newEntry.apply(
                        ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4)).append
                                (ComponentUtils.stringToText("[", 0xAAAAAA)).append
                                (ComponentUtils.stringToText(pathName, this.menuConfig.colors[3])).append
                                (ComponentUtils.stringToText("]", 0xAAAAAA)),
                        null));

                addTextEntries(((AbstractConfig)valueMap.get(key)).valueMap(), textListViewConsumer, newEntry, pathName, depth + 1);
            } else {
                if (!hideComments && comment != null)
                    for (String string : comment.split("\\n\\r | [\\n\\r]"))
                        textListViewConsumer.accept(newEntry.apply(ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4 - 1) + "#" + string.replace("\t", "    "), this.menuConfig.colors[4]), null));

                //Format text based on type
                IFormattableTextComponent outputBase = ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4) + (this.menuConfig.showFullKeyNames ? pathName : key), this.menuConfig.colors[2]).append(ComponentUtils.stringToText(" = ", 0xA0A0A0));
                textListViewConsumer.accept(newEntry.apply(outputBase.append(getValueComponent(pathName.split("\\."))), pathName));

                if (!hideSeparators)
                    for (int i = 0; i < this.menuConfig.separatorHeight; i++)
                        textListViewConsumer.accept(newEntry.apply(ComponentUtils.stringToText(StringUtils.repeat(' ', depth * 4) + StringUtils.repeat(this.menuConfig.separatorChars, this.menuConfig.separatorLength), this.menuConfig.colors[5]), null));
            }
        });
    }

    public void doConfigChange(){
        doConfigChange(this.current.getSpec().valueMap(), null, 0);
    }

    /** Magic that saves values to config file. */
    protected void doConfigChange(Map<String, Object> valueMap, @Nullable AbstractConfig nestedConfig, final int depth){
        String[] splitInput = this.keyInputField.getValue().trim().split("\\.");
        String lastSplit = splitInput[splitInput.length - 1];
        if (!valueMap.containsKey(lastSplit) && valueMap.values().stream().noneMatch((o) -> o instanceof AbstractConfig)) {
            this.keyInputField.setTextColor(this.menuConfig.colors[1]);
            this.invalidKey = true;
        } else {
            try {
                String valueInput = this.valueInputField.getValue().trim();
                Object valueSpec = nestedConfig == null ? valueMap.get(lastSplit) : nestedConfig.get(lastSplit);
                ForgeConfigSpec.ConfigValue<?> configValue = this.current.getConfigValue(StringUtils.join(splitInput, '.'));
                AbstractConfig abstractConfig = valueMap.get(splitInput[depth]) instanceof AbstractConfig ? ((AbstractConfig)valueMap.get(splitInput[depth])) : null;
                boolean isTruthy = StringUtils.equalsAnyIgnoreCase(valueInput, "true", "yes", "y", "t") || (StringUtils.isNumeric(valueInput) && Integer.parseInt(valueInput) == 1);
                boolean isFalsy = StringUtils.equalsAnyIgnoreCase(valueInput, "false", "no", "n", "f") || (StringUtils.isNumeric(valueInput) && Integer.parseInt(valueInput) == 0);
                int radix = StringUtils.startsWithAny(valueInput.toLowerCase(), "0x", "#") ? 16
                        : StringUtils.startsWithAny(valueInput.toLowerCase(), "0o", "o") ? 8
                        : StringUtils.startsWithAny(valueInput.toLowerCase(), "0b", "b") ? 2
                        : 10;
                if (valueSpec instanceof ForgeConfigSpec.ValueSpec && configValue != null){
                    Class<?> valueClazz = ((ForgeConfigSpec.ValueSpec) valueSpec).getClazz();

                    if (valueClazz == List.class)           this.current.setValue(StringUtils.join(splitInput, '.'), Arrays.stream(StringUtils.strip(valueInput, "[]").split(", ")).collect(Collectors.toList()));
                    else if (valueClazz == String.class)    this.current.setValue(StringUtils.join(splitInput, '.'), valueInput);
                    else if (valueClazz == Short.class)     this.current.setValue(StringUtils.join(splitInput, '.'), Short.valueOf(StringUtils.stripStart(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b|b)", "0"), "0"), radix));
                    else if (valueClazz == Integer.class)   this.current.setValue(StringUtils.join(splitInput, '.'), Integer.valueOf(StringUtils.stripStart(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b|b)", "0"), "0"), radix));
                    else if (valueClazz == Long.class)      this.current.setValue(StringUtils.join(splitInput, '.'), Long.valueOf(StringUtils.stripStart(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b|b)", "0"), "0"), radix));
                    else if (valueClazz == Float.class)     this.current.setValue(StringUtils.join(splitInput, '.'), Float.valueOf(valueInput));
                    else if (valueClazz == Double.class)    this.current.setValue(StringUtils.join(splitInput, '.'), Double.valueOf(valueInput));
                    else if (valueClazz == Byte.class) {
                        int intValue = Integer.parseInt(valueInput.replaceFirst("(?i)(0x|#|0o|o|0b)", "0"), radix);
                        if (intValue <= 255)                this.current.setValue(StringUtils.join(splitInput, '.'), (byte) intValue);
                        else throw new NumberFormatException("Bad byte value: \"" + valueInput + "\"");
                    } else if (valueClazz == Boolean.class) {
                        if (isTruthy || isFalsy)            this.current.setValue(StringUtils.join(splitInput, '.'), isTruthy && !isFalsy);
                        else throw new Exception("Bad boolean value: \"" + valueInput + "\"");
                    }

                    // Save changes.
                    configValue.save();
                    this.foundKey = true;
                    this.invalidKey = false;
                    this.valueInputField.setTextColor(this.menuConfig.colors[0]);
                } else if (abstractConfig != null) {
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
                    this.keyInputField.setTextColor(this.menuConfig.colors[1]);
                } else {
                    this.invalidValue = true;
                    this.valueInputField.setTextColor(this.menuConfig.colors[1]);
                }
                CubicThings.LOGGER.info("ConfigScreen Value Error: " + e);
            }
        }
    }

    @Nonnull
    private IFormattableTextComponent getValueComponent(@Nullable String[] path){
        return getValueComponent(this.current.getValue(StringUtils.join(path, '.'), Object.class));
    }

    @Nonnull
    private IFormattableTextComponent getValueComponent(@Nullable Object o){
        boolean isIntegralNumber = o instanceof Short || o instanceof Integer || o instanceof Long;
        if (o instanceof String)                                                        return ComponentUtils.stringToText('"' + ((String)o).replace("\\", "\\\\").replace("\"", "\\\"") + '"', this.menuConfig.colors[6]);
        if (o instanceof Boolean)                                                       return ComponentUtils.stringToText(o.toString(), this.menuConfig.colors[7]);
        if (o instanceof Byte && this.menuConfig.showBytesAsBinary)                     return ComponentUtils.stringToText("0b" + StringUtils.repeat('0', 8 - Integer.toBinaryString((byte) o & 0xFF).length()) + Integer.toBinaryString((byte) o & 0xFF), this.menuConfig.colors[8]);
        if (o instanceof Byte && this.menuConfig.showIntsAsHex)                         return ComponentUtils.stringToText("0x" + Integer.toHexString((Byte)o & 0xFF).toUpperCase(), this.menuConfig.colors[8]);
        if (o instanceof Byte)                                                          return ComponentUtils.stringToText(((Integer)(((Byte)o) & 0xFF)).toString(), this.menuConfig.colors[8]);
        if (isIntegralNumber && this.menuConfig.showIntsAsHex)                          return ComponentUtils.stringToText("0x" + Long.toHexString(((Number) o).longValue()).toUpperCase(), this.menuConfig.colors[9]);
        if (isIntegralNumber)                                                           return ComponentUtils.stringToText(o.toString(), this.menuConfig.colors[9]);
        if (o instanceof Float || o instanceof Double)                                  return ComponentUtils.stringToText(o.toString(), this.menuConfig.colors[10]);
        if (o instanceof List<?>) {
            List<?> l = (List<?>) o; 
            IFormattableTextComponent a = ComponentUtils.stringToText("[", 0xAAAAAA);
            for (int i = 0; i < l.size() ; i++){
                a.append(getValueComponent(l.get(i)));
                if (i != l.size() - 1)
                    a.append(ComponentUtils.stringToText(", ", 0xAAAAAA));
            }                                                                           return a.append(ComponentUtils.stringToText("]", 0xAAAAAA));
        }
        if (o != null)                                                                  return ComponentUtils.stringToText(o.toString(), this.menuConfig.colors[2]);
        return ComponentUtils.getEmptyMutable();
    }

    @Override
    protected void init() {
        super.init();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        this.keyInputField = new TextFieldWidget(this.font, this.width / 2 - 150, 25, 130, 10, ITextComponent.nullToEmpty("Key"));
        this.keyInputField.setMaxLength(256);
        this.keyInputField.setTextColor(0x888888);
        this.keyInputField.setValue("Key");
        addButton(this.keyInputField);

        this.valueInputField = new TextFieldWidget(this.font, this.width / 2 - 5, 25, 80, 10, ITextComponent.nullToEmpty("Value"));
        this.valueInputField.setMaxLength(256);
        this.valueInputField.setTextColor(0x888888);
        this.valueInputField.setValue("Value");
        addButton(this.valueInputField);

        this.prevConfigButton = addButton(new Button(this.width / 2 + 135, 20, 10, 20, ITextComponent.nullToEmpty("<"), button -> {
            try {
                this.current = configs.get(configs.indexOf(current) - 1);
            } catch (IndexOutOfBoundsException e){
                this.current = configs.get(configs.size() - 1);
            }
        }));
        this.prevConfigButton.active = this.configs.size() > 1;

        this.nextConfigButton = addButton(new Button(this.width / 2 + 145, 20, 10, 20, ITextComponent.nullToEmpty(">"), button -> {
            try {
                this.current = configs.get(configs.indexOf(current) + 1);
            } catch (IndexOutOfBoundsException e){
                this.current = configs.get(0);
            }
        }));
        this.nextConfigButton.active = this.configs.size() > 1;

        this.toggleCommentsButton = addButton(new Button(this.width / 2 - 150, this.height - 30, 110, 20, new TranslationTextComponent(hideComments ? "modConfig.comments.hidden" : "modConfig.comments.shown"), (button) -> {
            // Toggles comment hiding
            hideComments = !hideComments;
        }));

        this.toggleSeparatorsButton = addButton(new Button(this.width / 2 - 40, this.height - 30, 110, 20, new TranslationTextComponent(hideComments ? "modConfig.separators.hidden" : "modConfig.separators.shown"), (button) -> {
            // Toggles separator hiding
            hideSeparators = !hideSeparators;
        }));

        this.cancelButton = addButton(new Button(this.width / 2 + 86, this.height - 30, 70, 20, new TranslationTextComponent("gui.cancel"), (button) -> {
            // Send player back to last screen.
            getMinecraft().setScreen(this.prevScreen);
        }));

        this.submitChangeButton = addButton(new Button(this.width / 2 + 80, 20, 50, 20,  new TranslationTextComponent("gui.submit"), (button) -> {
            // Change a config value with a given input.
            doConfigChange();
        }));

        this.configList = new ConfigListWidget(this, 300,this.height - 75,this.width / 2 - 150,45,4, 4, this.font.lineHeight + 2, 0xC0C0C0, true, true, this.font, (configEntry) -> {
            String oldValue = this.keyInputField.getValue();
            String valueString = getValueComponent(configEntry.get().configValue.getPath().toArray(new String[0])).getString().replace("\\\"","\"").replace("\\\\", "\\");
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
                getMinecraft().getSoundManager().play(new SimpleSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.25F, 1.0F, 0, 0, 0));
        });

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
                case GLFW.GLFW_KEY_ENTER: {
                    this.submitChangeButton.onPress();
                    break;
                }
                case GLFW.GLFW_KEY_ESCAPE: {
                    this.valueInputField.setFocus(false);
                    break;
                }
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
    public void renderBackground(MatrixStack MatrixStack) {
        super.renderBackground(MatrixStack);
    }

    public void renderForeground(MatrixStack MatrixStack){
        this.font.draw(MatrixStack, ITextComponent.nullToEmpty("="), this.width / 2.0F - 15, 26, 0xAAAAAA);
        this.font.draw(MatrixStack, ((IFormattableTextComponent)this.title).setStyle(this.title.getStyle().setUnderlined(true)), 5, 5, 0xA0A0A0);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.updateWidgets();
        this.configList.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderForeground(matrixStack);
    }

    @Override
    public void tick() {
        super.tick();
    }

}
