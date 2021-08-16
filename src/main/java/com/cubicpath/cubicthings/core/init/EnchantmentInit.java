////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.enchantment.*;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public final class EnchantmentInit {
    private EnchantmentInit() {
        throw new IllegalStateException();
    }

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS , CubicThings.MODID);

    public static final RegistryObject<Enchantment> MAGMA_VISION = ENCHANTMENTS.register("magma_vision",
            () -> new MagmaVisionEnchantment(Enchantment.Rarity.RARE, new EquipmentSlotType[]{EquipmentSlotType.HEAD}));

    public static final RegistryObject<Enchantment> MULTISTEP = ENCHANTMENTS.register("multistep",
            () -> new MultistepEnchantment(Enchantment.Rarity.RARE, new EquipmentSlotType[]{EquipmentSlotType.FEET}));

    public static final RegistryObject<Enchantment> MULTIMINE = ENCHANTMENTS.register("multimine",
            () -> new MultimineEnchantment(Enchantment.Rarity.VERY_RARE, new EquipmentSlotType[]{EquipmentSlotType.MAINHAND}));

}
