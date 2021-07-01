////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/** Builder wrapper for {@link CompoundNBT} objects. */
public class NBTBuilder {
    private final CompoundNBT data;

    /** Static method for quickly getting an empty {@link CompoundNBT} object. */
    public static CompoundNBT getEmptyNBT(){
        return new CompoundNBT();
    }

    /**
     * Construct {@link NBTBuilder} with a new {@link CompoundNBT}. <br>
     * <br>
     * <i>NOTE: Using the {@linkplain #build()} method is not required if using this builder to modify NBT Objects as opposed to creating.</i>
     */
    public NBTBuilder() {
        this(getEmptyNBT());
    }

    /**
     * Construct {@link NBTBuilder} with given {@link CompoundNBT} data. Replaces null with {@link #getEmptyNBT()} data. <br>
     * <br>
     * <i>NOTE: Using the {@linkplain #build()} method is not required if using this builder to modify NBT Objects as opposed to creating.</i>
     */
    public NBTBuilder(@Nullable CompoundNBT data) {
        this.data = data != null ? data : getEmptyNBT();
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder put(String key, INBT value){
        this.data.put(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putDouble(String key, double value){
        this.data.putDouble(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putFloat(String key, float value){
        this.data.putFloat(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putLongArray(String key, List<Long> value){
        this.data.putLongArray(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putLongArray(String key, long[] value){
        this.data.putLongArray(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putLong(String key, long value){
        this.data.putLong(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putIntArray(String key, List<Integer> value){
        this.data.putIntArray(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putIntArray(String key, int[] value){
        this.data.putIntArray(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putInt(String key, int value){
        this.data.putInt(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putShort(String key, short value){
        this.data.putShort(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putByteArray(String key, byte[] value){
        this.data.putByteArray(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putByte(String key, byte value){
        this.data.putByte(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putBoolean(String key, boolean value){
        this.data.putBoolean(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putUniqueId(String key, UUID value){
        this.data.putUniqueId(key, value);
        return this;
    }

    /** Exposes corresponding {@link CompoundNBT} method and returns self. */
    public NBTBuilder putString(String key, String value){
        this.data.putString(key, value);
        return this;
    }

    /**
     * Finish building and return the {@link CompoundNBT} value. <br>
     * <br>
     * <i>NOTE: This is not required if using the builder to modify NBT data as opposed to creating.</i>
     */
    public CompoundNBT build(){
        return this.data;
    }

}
