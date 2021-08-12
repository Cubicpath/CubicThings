////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.config;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains changeable values stored in memory. Automagically syncs a config file to its values when registered using {@linkplain ModLoadingContext#registerConfig}.
 *
 * @see ForgeConfigSpec
 * @see AbstractConfig
 */
public class BaseConfig {
    public final String modid;
    protected ForgeConfigSpec spec;
    private final HashMap<String, String> comments = new HashMap<>();
    private final HashMap<String, ForgeConfigSpec.ConfigValue<?>> values = new HashMap<>();
    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    private final ArrayList<String> builderPath = new ArrayList<>();

    public BaseConfig(String modid) {
        this.modid = modid;
    }

    public static boolean isValueSpecInMapPath(Map<String, Object> valueMap, String[] path, final int depth) {
        try {
            Object o = valueMap.get(path[depth]);
            AbstractConfig config = o instanceof AbstractConfig ? (AbstractConfig) o : null;
            return (o instanceof ForgeConfigSpec.ValueSpec && depth == path.length - 1) || (config != null && isValueSpecInMapPath(config.valueMap(), path, depth + 1));
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * @param path Usually a string split on periods.
     * @return Whether a {@linkplain ForgeConfigSpec.ValueSpec} is in the given path of the config.
     */
    public boolean isValueSpecInPath(String[] path) {
        return isValueSpecInMapPath(spec.valueMap(), path, 0);
    }

    public ForgeConfigSpec getSpec() {
        return this.spec;
    }

    @Nullable
    public ForgeConfigSpec.ConfigValue<?> getConfigValue(String path) {
        return values.get(path);
    }

    @Nullable
    public String getComment(String path) {
        return comments.get(path);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getDefault(String path, Class<T> clazz) {
        if (!(spec.get(path) instanceof ForgeConfigSpec.ValueSpec valueSpec))
            return null;
        return (T) valueSpec.getDefault();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getValue(String path, Class<T> clazz) {
        if (values.get(path) == null || !clazz.isAssignableFrom(values.get(path).get().getClass()))
            return null;
        return (T) values.get(path).get();
    }

    public <T> void buildValue(String valueName, T defaultValue, @Nullable String comment, Class<T> clazz) {
        var pathString = StringUtils.join(builderPath, ".") + (builderPath.isEmpty() ? "" : ".") + valueName;
        List<String> path = Lists.newArrayList(valueName);
        Supplier<T> defaultSupplier = () -> defaultValue;
        Predicate<Object> validator = (o) -> o != null && clazz.isAssignableFrom(o.getClass());
        if (comment != null) {
            comments.put(pathString, comment);
            builder.comment(comment);
        }
        values.put(pathString, builder.translation("modConfig." + modid + "." + pathString).define(path, defaultSupplier, validator, clazz));
    }

    public <T> void buildListValue(String valueName, List<? extends T> defaultValue, @Nullable String comment, Predicate<Object> elementValidator) {
        var pathString = StringUtils.join(builderPath, ".") + (builderPath.isEmpty() ? "" : ".") + valueName;
        List<String> path = Lists.newArrayList(valueName);
        Supplier<List<? extends T>> defaultSupplier = () -> defaultValue;
        if (comment != null) {
            comments.put(pathString, comment);
            builder.comment(comment);
        }
        values.put(pathString, builder.translation("modConfig." + modid + "." + pathString).defineList(path, defaultSupplier, elementValidator));
    }

    public void push(String name, @Nullable String comment) {
        var pathString = StringUtils.join(builderPath, ".") + (builderPath.isEmpty() ? "" : ".") + name;
        if (comment != null) {
            comments.put(pathString, comment);
            builder.comment(comment);
        }
        builder.push(name);
        builderPath.add(name);
    }

    public void pop(int amount) {
        for (int i = 0; i < amount; i++) {
            builder.pop();
            builderPath.remove(builderPath.size() - 1);
        }
    }

    public void build() {
        this.spec = this.builder.build();
    }
}
