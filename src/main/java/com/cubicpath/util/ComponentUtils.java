////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import net.minecraft.util.text.*;

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

    public static IFormattableTextComponent getEmptyMutable(){
        return StringTextComponent.EMPTY.plainCopy();
    }

    public static StringTextComponent stringToText(String string){
        return new StringTextComponent(string);
    }

    public static StringTextComponent stringToText(String string, int color){
        return colorText(stringToText(string), color);
    }

    public static StringTextComponent colorText(IFormattableTextComponent component, int color){
        return (StringTextComponent) component.setStyle(component.getStyle().withColor(Color.fromRgb(color)));
    }

}
