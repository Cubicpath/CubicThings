package com.cubicpath.cubicthings.client.gui.widget;

import com.cubicpath.cubicthings.client.gui.screen.ITextListHolder;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

public class TextEntry extends AbstractTextList.AbstractTextEntry<TextEntry> {
    public TextEntry(Consumer<AbstractTextList.AbstractTextEntry<TextEntry>> onClicked, ITextComponent displayText, ITextListHolder parent, int xPadding, int yPadding, int color) {
        super(onClicked, displayText, parent, xPadding, yPadding, color);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
