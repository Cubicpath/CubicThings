////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.config;

public class ConfigMenuConfig extends BaseConfig {
    public Boolean showFullKeyNames, showBytesAsBinary, showIntsAsHex;
    public String separatorChars;
    public Integer separatorLength, separatorHeight;
    public final Integer[] colors = new Integer[11];
    
    public ConfigMenuConfig(String configName) {
        super(configName);
        this.push("configMenu", "Options for the config menu.");
        this.buildValue("fullKeyNames", false, "Includes the entire path in the key name.", Boolean.class);
        this.buildValue("bytesAsBinary", true, "Represent byte values as individual bits.", Boolean.class);
        this.buildValue("integersAsHex", false, "Show integer values as hex strings.", Boolean.class);
        this.push("separator", "BaseConfig for value separators.");
        this.buildValue("chars", "-", "Characters that seperate lines, \naccepts whitespace.", String.class);
        this.buildValue("length", 28, "Amount of separator chars per line.", Integer.class);
        this.buildValue("height", 1, "Amount of lines containing \nseparators characters.", Integer.class);
        this.pop(1);
        this.push("color", "Color values for the config menu.");
        this.buildValue("fail", 0xBB4444, null, Integer.class);
        this.buildValue("good", 0x44BA44, null, Integer.class);
        this.buildValue("keys", 0xDB7F41, null, Integer.class);
        this.buildValue("subConfigs", 0xDB7F41, null, Integer.class);
        this.buildValue("comments", 0x8190A0, null, Integer.class);
        this.buildValue("separators", 0x4400AA, null, Integer.class);
        this.buildValue("strings", 0x00AA00, null, Integer.class);
        this.buildValue("booleans", 0xFF55FF, null, Integer.class);
        this.buildValue("bytes", 0xFF4400, null, Integer.class);
        this.buildValue("integers", 0xFFAA00, null, Integer.class);
        this.buildValue("floats", 0xFFAA00, null, Integer.class);
        this.pop(2);
        this.build();
        this.reloadFields();
    }
    
    public void reloadFields(){
        String menuPath = "configMenu.";
        String separatorPath = menuPath + "separator.";
        String colorPath = menuPath + "color.";
        this.showFullKeyNames   =    getValue(menuPath  + "fullKeyNames",       Boolean.class);
        this.showBytesAsBinary  =    getValue(menuPath  + "bytesAsBinary",      Boolean.class);
        this.showIntsAsHex      =    getValue(menuPath  + "integersAsHex",      Boolean.class);
        this.separatorChars     =    getValue(separatorPath  + "chars",         String.class);
        this.separatorLength    =    getValue(separatorPath  + "length",        Integer.class);
        this.separatorHeight    =    getValue(separatorPath  + "height",        Integer.class);
        this.colors[0]          =    getValue(colorPath + "good",               Integer.class);
        this.colors[1]          =    getValue(colorPath + "fail",               Integer.class);
        this.colors[2]          =    getValue(colorPath + "keys",               Integer.class);
        this.colors[3]          =    getValue(colorPath + "subConfigs",         Integer.class);
        this.colors[4]          =    getValue(colorPath + "comments",           Integer.class);
        this.colors[5]          =    getValue(colorPath + "separators",         Integer.class);
        this.colors[6]          =    getValue(colorPath + "strings",            Integer.class);
        this.colors[7]          =    getValue(colorPath + "booleans",           Integer.class);
        this.colors[8]          =    getValue(colorPath + "bytes",              Integer.class);
        this.colors[9]          =    getValue(colorPath + "integers",           Integer.class);
        this.colors[10]         =    getValue(colorPath + "floats",             Integer.class);
    }

    @Override
    public <T> void setValue(String path, T value) {
        super.setValue(path, value);
        reloadFields();
    }
}
