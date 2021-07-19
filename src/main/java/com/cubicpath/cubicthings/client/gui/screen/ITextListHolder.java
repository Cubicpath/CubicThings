////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ITextListHolder {
    FontRenderer getFontRenderer();

    <T extends ExtendedList.AbstractListEntry<T>> void buildTextList(Consumer<T> textListViewConsumer, Function<String, T> newEntry);

    void renderBackground(MatrixStack matrixStack);
}
