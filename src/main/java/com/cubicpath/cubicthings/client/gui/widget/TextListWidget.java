////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ITextListHolder;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class TextListWidget extends AbstractTextList<TextListWidget.TextEntry> {
    private final ITextListHolder parent;

    public TextListWidget(ITextListHolder parent, int listWidth, int listHeight, int x, int y, int borderPaddingX, int borderPaddingY, int textSpacing, int textColor, boolean renderDirtBackground, boolean renderDarkOutline, Font font, Consumer<AbstractTextEntry<TextEntry>> onEntryClicked) {
        super(listWidth, listHeight, x, y, borderPaddingX, borderPaddingY, textSpacing, textColor, renderDirtBackground, renderDarkOutline, font, onEntryClicked);
        this.parent = parent;
    }

    @Override
    public void refreshList() {
        this.clearEntries();
        this.parent.buildTextList(super::addEntry, (componentIn, clickableIn) -> new TextEntry(Boolean.getBoolean(clickableIn) ? this.onEntryClicked : __ -> {}, componentIn, this.parent, this.borderPaddingX, this.borderPaddingY, this.textColor));
    }

    public static class TextEntry extends AbstractTextEntry<TextEntry> {
        public TextEntry(Consumer<AbstractTextEntry<TextEntry>> onClicked, Component displayText, ITextListHolder parent, int xPadding, int yPadding, int color) {
            super(onClicked, displayText, parent, xPadding, yPadding, color);
        }

        @Override
        public boolean isActive() {
            return true;
        }
    }
}
