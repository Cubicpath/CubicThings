////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.item.*;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public final class ItemInit {
    private ItemInit(){
        throw new IllegalStateException();
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS , CubicThings.MODID);

    public static final RegistryObject<Item> SCANNER = ITEMS.register("scanner", () -> new ScannerItem(new Item.Properties().durability(5000).rarity(Rarity.UNCOMMON).tab(CreativeModeTab.TAB_TOOLS), 40, 3));

}
