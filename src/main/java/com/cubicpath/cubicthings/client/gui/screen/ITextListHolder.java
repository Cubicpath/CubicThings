////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.text.ITextComponent;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface ITextListHolder {
    FontRenderer getFontRenderer();

    <T extends AbstractList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<ITextComponent, String, T> newEntry);
}
