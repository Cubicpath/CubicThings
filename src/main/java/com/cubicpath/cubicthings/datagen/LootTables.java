////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.datagen;

import com.cubicpath.cubicthings.CubicThings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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
    public void run(HashCache cache) {
        Map<ResourceLocation, LootTable> tables = new HashMap<>();

        writeTables(cache, tables);
    }

    //https://github.com/McJty/YouTubeTutorial17/blob/b7b3cf09630f9143cd0475f96dd328176f2dacf1/src/main/java/com/mcjty/datagen/LootTables.java#L61
    private void writeTables(HashCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                DataProvider.save(GSON, cache, net.minecraft.world.level.storage.loot.LootTables.serialize(lootTable), path);
            } catch (IOException e) {
                CubicThings.LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }


    protected static <T> T applyExplosionCondition(Block block, ConditionUserBuilder<T> p_124136_) {
        return (T) (!(block.getExplosionResistance() < 0) ? p_124136_.when(ExplosionCondition.survivesExplosion()) : p_124136_.unwrap());
    }

    public LootTable.Builder dropSelf(Block block) {
        return dropSelf(block, false);
    }

    public LootTable.Builder dropSelf(Block block, boolean explosionResistant) {
        return LootTable.lootTable().withPool(explosionResistant ? LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block)) : applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))).add(LootItem.lootTableItem(block)));
    }

    @Override
    public String getName() {
        return CubicThings.MODID + " LootTables";
    }


}
