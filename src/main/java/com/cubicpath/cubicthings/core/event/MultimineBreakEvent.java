////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.event;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.core.init.EnchantmentInit;
import com.cubicpath.util.RayTraceHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = CubicThings.MODID)
public class MultimineBreakEvent {
    /**
     * Ignores hardness {float} higher than what you are mining.
     *     (Allows for breaking grass while mining dirt, or breaking cobble while mining stone.)
     **/
    private static final float HARDNESS_BOUNDARY = 0.1f;

    /**
     * Would ignore the harvest levels {int} higher than what you are mining.
     *     (Allows breaking iron while mining stone, or obsidian while mining gold)
     **/
    private static final int HARVEST_BOUNDARY = 0;

    /** Must be an enchanted {@linkplain ToolItem} that could be enchanted with multimine */
    private static boolean isValidItem(ItemStack item) {
        return EnchantmentInit.MULTIMINE.get().canApplyAtEnchantingTable(item) && item.isEnchanted() && (item.getItem() instanceof ToolItem);
    }

    /** In survival, crouching returns true. In creative, crouching returns false */
    private static boolean isMultimineEnabled(PlayerEntity player){
        return ((!player.isCreative() && player.isCrouching()) || player.isCreative() && !player.isCrouching());
    }

    private static boolean canMine(ItemStack itemStack, BlockState blockState){
        // If itemStack item is a PickaxeItem
        for (ToolType toolType: itemStack.getToolTypes()) {
            if (blockState.isToolEffective(toolType)){
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void multimineBreakEvent(final BreakEvent event){
        final PlayerEntity player = event.getPlayer();
        final BlockPos pos = event.getPos();
        final BlockState blockState = event.getState();
        final World world = player.getEntityWorld();
        final ItemStack itemMainhand = player.getHeldItemMainhand();
        final int harvestLvl = blockState.getHarvestLevel();
        final float hardness = blockState.getBlockHardness(world, pos);

        if (isValidItem(itemMainhand) && isMultimineEnabled(player)){
            final Direction blockFace = RayTraceHelper.targetedBlockFace(player, false);
            final int enchantLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MULTIMINE.get(), itemMainhand);

            // Original block must be harvestable
            if (canMine(itemMainhand, blockState) && enchantLvl > 0) {
                BlockPos posToBreak;

                switch(blockFace.getAxis()){
                    default: throw new IllegalArgumentException("Unknown Axis.");

                    case X:{
                        // Vertical & Inward Block Slice (X Axis Faces(East & West))
                        for(int y = -enchantLvl; y <= enchantLvl; y++){
                            for(int z = -enchantLvl; z <= enchantLvl; z++) {
                                posToBreak = new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() + z);
                                // Break (ignore parent block).
                                if (!posToBreak.equals(pos)){
                                    breakBlock(world, player, hardness, harvestLvl, posToBreak, itemMainhand);
                                }
                            }
                        }
                        break;
                    }

                    case Y:{
                        // Horizontal & Inward Block Slice (Y Axis Faces(Up & Down))
                        for(int x = -enchantLvl; x <= enchantLvl; x++){
                            for(int z = -enchantLvl; z <= enchantLvl; z++) {
                                posToBreak = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                                // Break (ignore parent block).
                                if (!posToBreak.equals(pos)){
                                    breakBlock(world, player, hardness, harvestLvl, posToBreak, itemMainhand);
                                }
                            }
                        }
                        break;
                    }

                    case Z:{
                        // Horizontal & Vertical Block Slice (Z Axis Faces(North & South))
                        for(int x = -enchantLvl; x <= enchantLvl; x++){
                            for(int y = -enchantLvl; y <= enchantLvl; y++) {
                                posToBreak = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ());
                                // Break (ignore parent block).
                                if (!posToBreak.equals(pos)){
                                    breakBlock(world, player, hardness, harvestLvl, posToBreak, itemMainhand);
                                }
                            }
                        }
                        break;
                    }
                }

            }

        }
    }

    /**
     * Breaks a block using a given tool.
     */
    private static void breakBlock(World world, PlayerEntity player, float originalHardness, int originalHarvestLvl, BlockPos posToBreak, ItemStack harvestTool){
        final BlockState blockState = world.getBlockState(posToBreak);
        final int harvestLvl = blockState.getHarvestLevel();
        final float hardness = blockState.getBlockHardness(world, posToBreak);
        if (canMine(harvestTool, blockState) && !(harvestLvl < 0 || hardness < 0) && (hardness <= originalHardness + HARDNESS_BOUNDARY) && (harvestLvl <= originalHarvestLvl + HARVEST_BOUNDARY)){
            world.playEvent(2001, posToBreak, Block.getStateId(blockState));                    // Play block-break event (Particle and Sound events)
            world.setBlockState(posToBreak, world.getFluidState(posToBreak).getBlockState(), 3);// Delete block & update change across clients

            // Survival-specific stuff
            if(!player.isCreative()) {
                // Damage tool, average damage can be reduced by the Unbreaking enchantment
                if (harvestTool.isDamageable()) {
                    harvestTool.attemptDamageItem(1, player.getRNG(), player instanceof ServerPlayerEntity ? (ServerPlayerEntity)player : null);
                }

                player.addStat(Stats.BLOCK_MINED.get(blockState.getBlock()));
                player.addExhaustion(0.003F);
                Block.spawnDrops(blockState, world, posToBreak);
            }
        }

    }
}
