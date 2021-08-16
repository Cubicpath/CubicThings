////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;

import net.minecraft.client.gui.FontRenderer;

import java.util.function.Consumer;

public class ConfigListWidget extends AbstractTextList<ConfigEntry> {
    private final ConfigScreen parent;

    public ConfigListWidget(ConfigScreen parent, int listWidth, int listHeight, int x, int y, int borderPaddingX, int borderPaddingY, int textSpacing, int textColor, boolean renderDirtBackground, boolean renderDarkOutline, FontRenderer font, Consumer<AbstractTextEntry<ConfigEntry>> onEntryClicked) {
        super(listWidth, listHeight, x, y, borderPaddingX, borderPaddingY, textSpacing, textColor, renderDirtBackground, renderDarkOutline, font, onEntryClicked);
        this.parent = parent;
    }

    @Override
    public void refreshList() {
        this.clearEntries();
        this.parent.buildTextList(super::addEntry, (componentIn, pathIn) -> new ConfigEntry(pathIn != null ? this.onEntryClicked : __ -> {}, componentIn, pathIn, this.parent, this.borderPaddingX, this.borderPaddingY, this.textColor));
    }

}
