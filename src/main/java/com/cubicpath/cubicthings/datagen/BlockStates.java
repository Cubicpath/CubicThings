////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import com.cubicpath.cubicthings.CubicThings;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

class BlockStates extends BlockStateProvider {
    BlockStates(DataGenerator generator, ExistingFileHelper exFileHelper) {
        super(generator, CubicThings.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }

}
