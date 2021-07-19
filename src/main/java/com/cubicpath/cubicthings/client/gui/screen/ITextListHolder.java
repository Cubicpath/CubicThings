////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface ITextListHolder {
    FontRenderer getFontRenderer();

    <T extends ExtendedList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, BiFunction<String, Integer, T> newEntry);

    void renderBackground(MatrixStack matrixStack);
}
