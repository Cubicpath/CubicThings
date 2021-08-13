////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

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
            var itemModels      =       new ItemModels(generator, fileHelper);
            var blockStates     =       new BlockStates(generator, fileHelper);
            generator.addProvider(itemModels);
            generator.addProvider(blockStates);
        }

        if (event.includeServer()){
            var blockTags       =       new BlockTags(generator, fileHelper);
            var itemTags        =       new ItemTags(generator, blockTags, fileHelper);
            var lootTables      =       new LootTables(generator);
            var recipes         =       new Recipes(generator);
            generator.addProvider(blockTags);
            generator.addProvider(itemTags);
            generator.addProvider(lootTables);
            generator.addProvider(recipes);
        }

    }

}
