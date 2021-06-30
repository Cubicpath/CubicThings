////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class MultimineEnchantment extends Enchantment {

    public MultimineEnchantment(Rarity rarityIn, EquipmentSlotType[] slots) {
        super(rarityIn, EnchantmentType.DIGGER, slots);
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

}
