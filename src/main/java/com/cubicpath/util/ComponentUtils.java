////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import net.minecraft.network.chat.*;

/**
 * Utils relating to text manipulation.
 *
 * @since 2.0
 * @author Cubicpath
 */
public final class ComponentUtils {

    private ComponentUtils(){
        throw new IllegalStateException();
    }

    public static MutableComponent getEmptyMutable(){
        return TextComponent.EMPTY.plainCopy();
    }

    public static TextComponent stringToText(String string){
        return (TextComponent) Component.nullToEmpty(string);
    }

    public static TextComponent stringToText(String string, int color){
        return colorText(stringToText(string), color);
    }

    public static TextComponent colorText(MutableComponent component, int color){
        return (TextComponent) component.setStyle(component.getStyle().withColor(color));
    }

}
