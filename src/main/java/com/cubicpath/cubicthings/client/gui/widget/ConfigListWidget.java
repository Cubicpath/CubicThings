////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ConfigListWidget extends AbstractTextList<ConfigListWidget.ConfigEntry> {
    private final ConfigScreen parent;

    public ConfigListWidget(ConfigScreen parent, int listWidth, int listHeight, int x, int y, int borderPaddingX, int borderPaddingY, int textSpacing, int textColor, boolean renderDirtBackground, boolean renderDarkOutline, Font font, Consumer<AbstractTextEntry<ConfigEntry>> onEntryClicked) {
        super(listWidth, listHeight, x, y, borderPaddingX, borderPaddingY, textSpacing, textColor, renderDirtBackground, renderDarkOutline, font, onEntryClicked);
        this.parent = parent;
    }

    @Override
    public void refreshList() {
        this.clearEntries();
        this.parent.buildTextList(super::addEntry, (componentIn, pathIn) -> new ConfigEntry(pathIn != null ? this.onEntryClicked : __ -> {}, componentIn, pathIn, this.parent, this.borderPaddingX, this.borderPaddingY, this.textColor));
    }

    public static class ConfigEntry extends AbstractTextEntry<ConfigEntry> {
        public final ForgeConfigSpec.ConfigValue<?> configValue;

        public ConfigEntry(Consumer<AbstractTextEntry<ConfigEntry>> onClicked, Component displayText, @Nullable String path, ConfigScreen parent, int xPadding, int yPadding, int color) {
            super(onClicked, displayText, parent, xPadding, yPadding, color);
            this.configValue = parent.getCurrent().getConfigValue(path);
        }

        @Override
        public boolean isActive() {
            return this.configValue != null;
        }
    }
}
