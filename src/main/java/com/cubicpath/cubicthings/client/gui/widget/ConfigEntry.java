package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ConfigEntry extends AbstractTextList.AbstractTextEntry<ConfigEntry> {
    public final ForgeConfigSpec.ConfigValue<?> configValue;

    public ConfigEntry(Consumer<AbstractTextList.AbstractTextEntry<ConfigEntry>> onClicked, ITextComponent displayText, @Nullable String path, ConfigScreen parent, int xPadding, int yPadding, int color) {
        super(onClicked, displayText, parent, xPadding, yPadding, color);
        this.configValue = parent.getCurrent().getConfigValue(path);
    }

    @Override
    public boolean isActive() {
        return this.configValue != null;
    }
}