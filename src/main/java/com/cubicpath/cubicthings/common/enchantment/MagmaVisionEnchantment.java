////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MagmaVisionEnchantment extends Enchantment {

    public MagmaVisionEnchantment(Rarity rarityIn, EquipmentSlot[] slots) {
        super(rarityIn, EnchantmentCategory.ARMOR_HEAD, slots);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

}
