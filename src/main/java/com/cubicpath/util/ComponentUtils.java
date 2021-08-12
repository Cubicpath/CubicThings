////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import com.cubicpath.cubicthings.CubicThings;
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

    /**
     * {@code startIndex} & {@code endIndex} support negative numbers. <br>
     * &emsp&emsp&emsp&emsp&nbsp Ex: "-1" is last character of component string.
     *
     * @param component Component to create sub-component with.
     * @param startIndex Index of component string to start.
     * @param endIndex Index of component string to end.
     * @return {@linkplain MutableComponent} retaining styles of {@code component}, shortened by given indices.
     * @see String#substring(int, int)
     */
    /*
    public static MutableComponent subComponent(Component component, int startIndex, int endIndex){
        startIndex = startIndex < 0 ? component.getString().length() + startIndex : startIndex;
        endIndex = endIndex < 0 ? component.getString().length() + endIndex : endIndex;
        CubicThings.LOGGER.info("startIndex = " + startIndex);
        CubicThings.LOGGER.info("endIndex = " + endIndex);
        return subComponentRecursiveSiblingStyler(component, startIndex, endIndex, 0);
    }

    private static MutableComponent subComponentRecursiveSiblingStyler(Component component, int startIndex, final int endIndex, int counter){
        MutableComponent newComponent = getEmptyMutable();
        String string;
        int i1;

        if (component.getContents().isEmpty() && component.getSiblings().isEmpty()){
            newComponent.append(stringToText(component.getString().substring(startIndex, endIndex)).setStyle(component.getStyle()));
        } else {
            string = component.getContents();
            if (!(endIndex == 0 || string.length() < startIndex)){
                CubicThings.LOGGER.info("component = |" + string + "| counter = " + string.length());
                if (string.length() < endIndex) newComponent.append(stringToText(string).setStyle(component.getStyle()));
                else newComponent.append(stringToText(string.substring(startIndex, endIndex)).setStyle(component.getStyle()));
            }
            counter = string.length();
            CubicThings.LOGGER.info(component.getString());

            for (Component sibling : component.getSiblings()){
                CubicThings.LOGGER.info("sibling = |" + sibling.getString() + "| counter = " + counter);
                string = sibling.getString();
                i1 = counter + string.length();
                if (counter >= endIndex) break; // Already reached end index - stop loop
                if (i1 < startIndex) continue; // Won't reach start index - skip sibling
                if (i1 < endIndex) newComponent.append(sibling); // Won't go past end index - append regularly
                else { // Going past start index OR not going past end index
                    if (!sibling.getSiblings().isEmpty()) {
                        // If sibling has siblings, repeat
                        newComponent.append(subComponentRecursiveSiblingStyler(sibling, startIndex, endIndex, counter));
                        counter = i1;
                    }
                    else {
                        // If end of sibling tree
                        newComponent.append(stringToText(string.substring(startIndex - counter, endIndex - counter)).setStyle(sibling.getStyle()));
                    }
                }
                startIndex = counter;
            }
        }

        return newComponent;
    }

     */

}
