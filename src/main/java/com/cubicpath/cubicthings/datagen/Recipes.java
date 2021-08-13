////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import com.cubicpath.cubicthings.core.init.ItemInit;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

class Recipes extends RecipeProvider {
    Recipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(ItemInit.SCANNER.get())
                .pattern("IDD")
                .pattern(" IR")
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .group("tools")
                .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                .save(consumer);
    }
}
