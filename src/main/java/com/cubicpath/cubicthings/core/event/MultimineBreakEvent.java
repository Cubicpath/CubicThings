////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.event;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.core.init.EnchantmentInit;
import com.cubicpath.util.RayTraceHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = CubicThings.MODID)
public final class MultimineBreakEvent {
    private MultimineBreakEvent() {
        throw new IllegalStateException();
    }

    /**
     * Ignores hardness {float} higher than what you are mining.
     *     (Allows for breaking grass while mining dirt, or breaking cobble while mining stone.)
     **/
    private static final float HARDNESS_BOUNDARY = 0.1f;

    /**
     * Would ignore the harvest levels {int} higher than what you are mining.
     *     (Allows breaking iron while mining stone, or obsidian while mining gold)
     **/
    @Deprecated
    private static final int HARVEST_BOUNDARY = 0;

    /** Must be an enchanted {@linkplain DiggerItem} that could be enchanted with multimine */
    private static boolean isValidItem(ItemStack item) {
        return EnchantmentInit.MULTIMINE.get().canApplyAtEnchantingTable(item) && item.isEnchanted() && item.getItem() instanceof DiggerItem;
    }

    /** In survival, crouching returns true. In creative, crouching returns false */
    private static boolean isMultimineEnabled(Player player){
        return ((!player.isCreative() && player.isCrouching()) || player.isCreative() && !player.isCrouching());
    }

    @SubscribeEvent
    public static void multimineBreakEvent(final BreakEvent event){
        final var player = event.getPlayer();
        final var blockPos = event.getPos();
        final var blockState = event.getState();
        final var world = player.getCommandSenderWorld();
        final var itemMainhand = player.getMainHandItem();
        final var hardness = blockState.getDestroySpeed(world, blockPos);

        if (isValidItem(itemMainhand) && isMultimineEnabled(player)){
            final var blockFace = RayTraceHelper.targetedBlockFace(player, false);
            final var enchantLvl = EnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.MULTIMINE.get(), itemMainhand);

            // Original block must be harvestable
            if (itemMainhand.isCorrectToolForDrops(blockState) && enchantLvl > 0) {
                BlockPos posToBreak;

                switch (blockFace.getAxis()) {
                    default -> throw new IllegalArgumentException("Unknown Axis.");
                    case X -> {
                        // Vertical & Inward Block Slice (X Axis Faces(East & West))
                        for (int y = -enchantLvl; y <= enchantLvl; y++) {
                            for (int z = -enchantLvl; z <= enchantLvl; z++) {
                                posToBreak = new BlockPos(blockPos.getX(), blockPos.getY() + y, blockPos.getZ() + z);
                                // Break (ignore parent block).
                                if (!posToBreak.equals(blockPos)) {
                                    breakBlock(world, player, hardness, posToBreak, itemMainhand);
                                }
                            }
                        }
                    }
                    case Y -> {
                        // Horizontal & Inward Block Slice (Y Axis Faces(Up & Down))
                        for (int x = -enchantLvl; x <= enchantLvl; x++) {
                            for (int z = -enchantLvl; z <= enchantLvl; z++) {
                                posToBreak = new BlockPos(blockPos.getX() + x, blockPos.getY(), blockPos.getZ() + z);
                                // Break (ignore parent block).
                                if (!posToBreak.equals(blockPos)) {
                                    breakBlock(world, player, hardness, posToBreak, itemMainhand);
                                }
                            }
                        }
                    }
                    case Z -> {
                        // Horizontal & Vertical Block Slice (Z Axis Faces(North & South))
                        for (int x = -enchantLvl; x <= enchantLvl; x++) {
                            for (int y = -enchantLvl; y <= enchantLvl; y++) {
                                posToBreak = new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ());
                                // Break (ignore parent block).
                                if (!posToBreak.equals(blockPos)) {
                                    breakBlock(world, player, hardness, posToBreak, itemMainhand);
                                }
                            }
                        }
                    }
                }

            }

        }
    }

    /**
     * Breaks a block using a given tool.
     */
    private static void breakBlock(Level world, Player player, float originalHardness, BlockPos posToBreak, ItemStack harvestTool){
        final var blockState = world.getBlockState(posToBreak);
        final var hardness = blockState.getDestroySpeed(world, posToBreak);
        if (harvestTool.isCorrectToolForDrops(blockState) && !(hardness < 0) && (hardness <= originalHardness + HARDNESS_BOUNDARY)){
            world.levelEvent(2001, posToBreak, Block.getId(blockState));                    // Play block-break event (Particle and Sound events)
            world.setBlock(posToBreak, world.getFluidState(posToBreak).createLegacyBlock(), 3);// Delete block & update change across clients

            // Survival-specific stuff
            if(!player.isCreative()) {
                // Damage tool, average damage can be reduced by the Unbreaking enchantment
                if (harvestTool.isDamageableItem()) {
                    harvestTool.hurt(1, player.getRandom(), player instanceof ServerPlayer ? (ServerPlayer)player : null);
                }

                player.awardStat(Stats.BLOCK_MINED.get(blockState.getBlock()));
                player.causeFoodExhaustion(0.003F);
                Block.dropResources(blockState, world, posToBreak);
            }
        }

    }
}
