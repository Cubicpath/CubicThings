////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Builder wrapper for {@link CompoundNBT} objects.
 *
 * @since 1.0
 * @author Cubicpath
 */
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

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 1 (stored {@link ByteNBT})
     */
    public NBTBuilder putBoolean(String key, boolean value){
        this.data.putBoolean(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 1
     */
    public NBTBuilder putByte(String key, byte value){
        this.data.putByte(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 1
     */
    public NBTBuilder putByte(String key, ByteNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 2
     */
    public NBTBuilder putShort(String key, short value){
        this.data.putShort(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 2
     */
    public NBTBuilder putShort(String key, ShortNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 3
     */
    public NBTBuilder putInt(String key, int value){
        this.data.putInt(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 3
     */
    public NBTBuilder putInt(String key, IntNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 4
     */
    public NBTBuilder putLong(String key, long value){
        this.data.putLong(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 4
     */
    public NBTBuilder putLong(String key, LongNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 5
     */
    public NBTBuilder putFloat(String key, float value){
        this.data.putFloat(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 5
     */
    public NBTBuilder putFloat(String key, FloatNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 6
     */
    public NBTBuilder putDouble(String key, double value){
        this.data.putDouble(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 6
     */
    public NBTBuilder putDouble(String key, DoubleNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 7
     */
    public NBTBuilder putByteArray(String key, byte[] value){
        this.data.putByteArray(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 7
     */
    public NBTBuilder putByteArray(String key, ByteArrayNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 8
     */
    public NBTBuilder putString(String key, String value){
        this.data.putString(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 8
     */
    public NBTBuilder putString(String key, StringNBT value){
        this.data.put(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 9
     */
    public NBTBuilder putList(String key, ListNBT value){
        this.data.put(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 10
     */
    public NBTBuilder putCompound(String key, CompoundNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 11 (internally stored as {@link IntArrayNBT})
     */
    public NBTBuilder putUniqueId(String key, UUID value){
        this.data.putUniqueId(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 11
     */
    public NBTBuilder putIntArray(String key, int[] value){
        this.data.putIntArray(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 11
     */
    public NBTBuilder putIntArray(String key, List<Integer> value){
        this.data.putIntArray(key, value);
        return this;
    }

    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 11
     */
    public NBTBuilder putIntArray(String key, IntArrayNBT value){
        this.data.put(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 12
     */
    public NBTBuilder putLongArray(String key, long[] value){
        this.data.putLongArray(key, value);
        return this;
    }

    /** <p>Exposes corresponding {@link CompoundNBT} method and returns self.</p>
     *
     * tag_id = 12
     */
    public NBTBuilder putLongArray(String key, List<Long> value){
        this.data.putLongArray(key, value);
        return this;
    }
    
    /**
     * <p>Simple method to avoid raw usage of {@linkplain #put}; returns self.</p>
     * tag_id = 12
     */
    public NBTBuilder putLongArray(String key, LongArrayNBT value){
        this.data.put(key, value);
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
