////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.config;

import com.cubicpath.cubicthings.CubicThings;

import java.util.List;

public final class Configs {
    public static final BaseConfig CUBICTHINGS_COMMON = new BaseConfig(CubicThings.MODID);

    static {
        CUBICTHINGS_COMMON.buildValue("booleanValue", true, "This is a Boolean value. \n\tIt has 2 possible values: true & false.", Boolean.class);
        CUBICTHINGS_COMMON.buildValue("byteValue", (byte) 0, "This is a Byte value. \n\tIt consists of 8 bits, binary representation \n\tis denoted by a prefix of '0b'.", Byte.class);
        CUBICTHINGS_COMMON.buildValue("integerValue", 0, "This is an Integer value. \n\tIt is a whole number.", Integer.class);
        CUBICTHINGS_COMMON.buildValue("doubleValue", 1.000D, "This is a Double value. \n\tIt has decimals.", Double.class);
        CUBICTHINGS_COMMON.buildValue("stringValue", "Value", "This is a String value. \n\tIt has quotation marks. Characters ([\\] & [\"]) are \n\tautomatically escaped if needed.", String.class);
        CUBICTHINGS_COMMON.buildListValue("stringList", List.of("1", "wow", "third3"), "This is a String List. \n\tIt contains an assortment of string values.", o -> o != null && String.class.isAssignableFrom(o.getClass()));
        CUBICTHINGS_COMMON.buildListValue("intList", List.of(1, 2, 3), "This is an Integer List. \n\tIt contains an assortment of int values.", o -> o != null && Integer.class.isAssignableFrom(o.getClass()));
        CUBICTHINGS_COMMON.buildListValue("objectList", List.of("1", (byte)0xFF, 2, 3.33, List.of(22, 44, 66)), "This is an Object list. \n\tIt can contain anything.", o -> true);
        CUBICTHINGS_COMMON.buildListValue("listList", List.of(List.of("wow", "eee", "aaa"), List.of(22, 44, 66)), "This is a List of Lists. \n\tIt contains an assortment of lists.", o -> o instanceof List<?>);
        CUBICTHINGS_COMMON.buildListValue("byteListList", List.of(List.of((byte) 5, (byte) 4, (byte) 3), List.of((byte) 5, (byte) 4, (byte) 3)), "This is a List of Byte Lists. \n\tIt contains a list containing byte lists.", o -> o instanceof List<?> && (((List<?>) o).isEmpty() || Byte.class.isAssignableFrom(((List<?>) o).get(0).getClass())));
        CUBICTHINGS_COMMON.push("configMenu", "Options for the config menu.");
        CUBICTHINGS_COMMON.buildValue("fullKeyNames", false, "Includes the entire path in the key name.", Boolean.class);
        CUBICTHINGS_COMMON.buildValue("bytesAsBinary", true, "Represent byte values as individual bits.", Boolean.class);
        CUBICTHINGS_COMMON.buildValue("integersAsHex", false, "Show integer values as hex strings.", Boolean.class);
        CUBICTHINGS_COMMON.push("seperator", "BaseConfig for value seperators.");
        CUBICTHINGS_COMMON.buildValue("chars", "-", "Characters that seperate lines, \naccepts whitespace.", String.class);
        CUBICTHINGS_COMMON.buildValue("length", 28, "Amount of seperator chars per line.", Integer.class);
        CUBICTHINGS_COMMON.buildValue("height", 1, "Amount of lines containing \nseperators characters.", Integer.class);
        CUBICTHINGS_COMMON.pop(1);
        CUBICTHINGS_COMMON.push("color", "Color values for the config menu.");
        CUBICTHINGS_COMMON.buildValue("fail", 0xBB4444, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("good", 0x44BA44, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("keys", 0xDB7F41, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("subConfigs", 0xDB7F41, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("comments", 0x8190A0, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("separators", 0x4400AA, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("strings", 0x00AA00, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("booleans", 0xFF55FF, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("bytes", 0xFF4400, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("integers", 0xFFAA00, null, Integer.class);
        CUBICTHINGS_COMMON.buildValue("floats", 0xFFAA00, null, Integer.class);
        CUBICTHINGS_COMMON.pop(2);
        CUBICTHINGS_COMMON.build();
    }
}
