////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////


/**
 * <p>Init classes hold fields or methods that aid the mod in initializing. They themselves cannot be instantiated nor subclassed. In order from load-order:</p>
 * <br>
 * {@link com.cubicpath.cubicthings.core.init.BlockInit} - Contains the deferred register and fields for custom {@linkplain net.minecraft.world.level.block.Block Block}s<br>
 * {@link com.cubicpath.cubicthings.core.init.ItemInit} - Contains the deferred register and fields for custom {@linkplain net.minecraft.world.item.Item Item}s<br>
 * {@link com.cubicpath.cubicthings.core.init.EnchantmentInit} - Contains the deferred register and fields for custom {@linkplain net.minecraft.world.item.enchantment.Enchantment Enchantment}s<br>
 * {@link com.cubicpath.cubicthings.core.init.ContainerInit} - Contains the deferred register and fields for custom {@linkplain net.minecraft.world.inventory.MenuType Container}s<br>
 * {@link com.cubicpath.cubicthings.core.init.CommandInit} - Holds a list of command registration methods, which will be called during startup.
 * {@link com.cubicpath.cubicthings.core.init.NetworkInit} - Holds the mod's packet handlers and packet-registration methods.
 *
 * @since 0.1.0
 * @author Cubicpath
 */
package com.cubicpath.cubicthings.core.init;