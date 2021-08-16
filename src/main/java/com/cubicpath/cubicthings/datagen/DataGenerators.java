////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    private DataGenerators() {
        throw new IllegalStateException();
    }

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        if (event.includeClient()){
            ItemModels itemModels       =       new ItemModels(generator, fileHelper);
            BlockStates blockStates     =       new BlockStates(generator, fileHelper);
            generator.addProvider(itemModels);
            generator.addProvider(blockStates);
        }

        if (event.includeServer()){
            BlockTags blockTags         =       new BlockTags(generator, fileHelper);
            ItemTags itemTags           =       new ItemTags(generator, blockTags, fileHelper);
            LootTables lootTables       =       new LootTables(generator);
            Recipes recipes             =       new Recipes(generator);
            generator.addProvider(blockTags);
            generator.addProvider(itemTags);
            generator.addProvider(lootTables);
            generator.addProvider(recipes);
        }

    }

}
