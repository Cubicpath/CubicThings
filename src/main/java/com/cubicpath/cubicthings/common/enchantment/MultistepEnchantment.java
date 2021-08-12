////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MultistepEnchantment extends Enchantment {
    public static final float STEP_FACTOR = 0.5f;

    public MultistepEnchantment(Rarity rarityIn, EquipmentSlot[] slots) {
        super(rarityIn, EnchantmentCategory.ARMOR, slots);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

}
