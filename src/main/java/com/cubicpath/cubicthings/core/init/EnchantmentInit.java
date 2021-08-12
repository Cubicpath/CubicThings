////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.enchantment.*;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public final class EnchantmentInit {
    private EnchantmentInit() {
        throw new IllegalStateException();
    }

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS , CubicThings.MODID);

    public static final RegistryObject<Enchantment> MAGMA_VISION = ENCHANTMENTS.register("magma_vision",
            () -> new MagmaVisionEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.HEAD}));

    public static final RegistryObject<Enchantment> MULTISTEP = ENCHANTMENTS.register("multistep",
            () -> new MultistepEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.FEET}));

    public static final RegistryObject<Enchantment> MULTIMINE = ENCHANTMENTS.register("multimine",
            () -> new MultimineEnchantment(Enchantment.Rarity.VERY_RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));

}
