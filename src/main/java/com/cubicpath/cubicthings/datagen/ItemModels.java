////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.core.init.ItemInit;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

class ItemModels extends ItemModelProvider {
    ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, CubicThings.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(ItemInit.SCANNER.getId().getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(CubicThings.MODID, "items/scanner"));
    }
}
