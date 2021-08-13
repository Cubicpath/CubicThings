////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import net.minecraft.data.DataGenerator;
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

        if (event.includeClient()){
            var itemModels  =   new ItemModels(generator, event.getExistingFileHelper());
            var blockStates =   new BlockStates(generator, event.getExistingFileHelper());
            generator.addProvider(itemModels);
            generator.addProvider(blockStates);
        }

        if (event.includeServer()){
            var blockTags   =   new BlockTags(generator, event.getExistingFileHelper());
            var itemTags    =   new ItemTags(generator, blockTags, event.getExistingFileHelper());
            var lootTables  =   new LootTables(generator);
            var recipes     =   new Recipes(generator);
            generator.addProvider(blockTags);
            generator.addProvider(itemTags);
            generator.addProvider(lootTables);
            generator.addProvider(recipes);
        }

    }

}
