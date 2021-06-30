package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.CubicThings;

import net.minecraft.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public final class BlockInit {
    private BlockInit(){
        throw new IllegalStateException();
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS , CubicThings.MODID);

}
