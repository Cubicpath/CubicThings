////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.CubicThings;

import com.cubicpath.cubicthings.common.item.ScannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public final class ItemInit {
    private ItemInit(){
        throw new IllegalStateException();
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS , CubicThings.MODID);

    public static final RegistryObject<Item> SCANNER = ITEMS.register("scanner", () -> new ScannerItem(new Item.Properties().group(ItemGroup.TOOLS).maxDamage(5000), 40, 3));

}
