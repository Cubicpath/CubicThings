////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface ITextListHolder {
    Font getFontRenderer();

    <T extends AbstractSelectionList.Entry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<Component, String, T> newEntry);
}
