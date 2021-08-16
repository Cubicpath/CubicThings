////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import com.cubicpath.cubicthings.CubicThings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class LootTables extends LootTableProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    LootTables(DataGenerator generator) {
        super(generator);
        this.generator = generator;
    }

    @Override
    public void run(DirectoryCache cache) {
        Map<ResourceLocation, LootTable> tables = new HashMap<>();

        writeTables(cache, tables);
    }

    //https://github.com/McJty/YouTubeTutorial17/blob/b7b3cf09630f9143cd0475f96dd328176f2dacf1/src/main/java/com/mcjty/datagen/LootTables.java#L61
    private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable), path);
            } catch (IOException e) {
                CubicThings.LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }

    @SuppressWarnings("deprecation")
    protected static <T> T applyExplosionCondition(Block block, ILootConditionConsumer<T> p_124136_) {
        return !(block.getExplosionResistance() < 0) ? p_124136_.when(SurvivesExplosion.survivesExplosion()) : p_124136_.unwrap();
    }

    public LootTable.Builder dropSelf(Block block) {
        return dropSelf(block, false);
    }

    public LootTable.Builder dropSelf(Block block, boolean explosionResistant) {
        return LootTable.lootTable().withPool(explosionResistant ? LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(ItemLootEntry.lootTableItem(block)) : applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantRange.exactly(1))).add(ItemLootEntry.lootTableItem(block)));
    }

    @Override
    public String getName() {
        return CubicThings.MODID + " LootTables";
    }


}
