////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.item;


import com.cubicpath.util.NBTBuilder;
import com.cubicpath.util.RayTraceHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ScannerItem extends Item {

    public enum ScannerMode{
        BLOCKS("BlockTargets", 8),
        BIOMES("BiomeTargets", 8),
        ENTITIES("EntityTargets", 8);

        public final String listName;
        public final int listType;

        ScannerMode(String listName, int listType){
            this.listName = listName;
            this.listType = listType;
        }

        public ListNBT getTargetList(ItemStack scanner){
            CompoundNBT compoundnbt = scanner.getOrCreateTag();
            return compoundnbt.getList(this.listName, this.listType);
        }

    }

    public static class ScanData {
        public final World world;
        public final BlockPos blockPos;
        public final Float scanVolume;
        public final float scanPitch;

        public ScanData(World world, BlockPos blockPos, @Nullable Float scanVolume, float scanPitch) {
            this.world = world;
            this.blockPos = blockPos;
            this.scanVolume = scanVolume;
            this.scanPitch = scanPitch;
        }

    }


    public static final String KEY_MODE = "ScannerMode"; // String NBT
    public final int maxScanDistance;
    public final int scanWidth;


    /** Return the {@linkplain #KEY_MODE} NBT value. */
    public static ScannerMode getScannerMode(ItemStack scanner) {
        CompoundNBT compoundnbt = scanner.getOrCreateTag();
        if (!compoundnbt.getString(KEY_MODE).isEmpty())
            return ScannerMode.valueOf(compoundnbt.getString(KEY_MODE));
        return ScannerMode.BLOCKS;
    }

    /** Set the {@linkplain #KEY_MODE} NBT value to a given ScannerMode value. */
    public static void setScannerMode(ItemStack scanner, ScannerMode mode) {
        new NBTBuilder(scanner.getOrCreateTag())
                .putString(KEY_MODE, mode.name());
    }

    public static boolean addTarget(ItemStack scanner, ScannerMode mode, String string){
        ListNBT targetList = mode.getTargetList(scanner);
        StringNBT stringNBT = StringNBT.valueOf(string);

        if (targetList.contains(stringNBT)) return false;
        else return targetList.add(stringNBT);
    }

    public ScannerItem(Properties properties, int maxScanDistance, int scanWidth) {
        super(properties);
        this.maxScanDistance = maxScanDistance;
        this.scanWidth = scanWidth % 2 == 0 ? scanWidth / 2 : (scanWidth - 1) / 2; // Odd numbers are treated literally. Even are halved.
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        HashSet<ScanData> blocksToScan = new HashSet<>();
        ScannerMode mode = getScannerMode(context.getItem());

        switch (mode){
            default:
            case BLOCKS: {
                final int soundLimit = 50;
                final int worldHeight = context.getWorld().getHeight();
                final int worldDepth = 0;
                BlockPos posToScan;
                BlockPos posToScan1;
                addTarget(context.getItem(), getScannerMode(context.getItem()), "minecraft:diamond_ore");
                ListNBT targetNBT = mode.getTargetList(context.getItem());
                Block[] blockTargets = new Block[targetNBT.size()];
                for (int j = 0; j < targetNBT.size(); j++) {
                    String target = targetNBT.getString(j);
                    blockTargets[j] = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(target));
                }

                // Scans client-side only
                if (context.getPlayer() instanceof ClientPlayerEntity){
                    final RayTraceHelper.LookingAtContext traceContext = new RayTraceHelper.LookingAtContext(context.getPlayer(), maxScanDistance);

                    for (int i = 0; i < traceContext.eyePosition.distanceTo(traceContext.eyeLookingTo); i++) {
                        Vector3d vector3d = traceContext.eyePosition.add(new Vector3d(traceContext.xAngle * i, traceContext.yAngle * i, traceContext.zAngle * i));
                        posToScan = new BlockPos(vector3d);
                        if (posToScan.getY() >= worldDepth && posToScan.getY() <= worldHeight) {
                            blocksToScan.add(new ScanData(context.getWorld(), posToScan,0.8F / ((float)traceContext.eyePosition.squareDistanceTo(vector3d) / 2), 5.0F));

                            int soundCounter = 0;
                            for (int x = -this.scanWidth; x <= this.scanWidth; x++) {
                                for (int y = -this.scanWidth; y <= this.scanWidth ; y++) {
                                    for (int z = -this.scanWidth; z <= this.scanWidth ; z++) {
                                        soundCounter++;
                                        posToScan1 = new BlockPos(vector3d.add(x, y, z));
                                        if (posToScan1 != posToScan && posToScan1.withinDistance(vector3d, (float)this.maxScanDistance / 2)) {
                                            //CubicThings.LOGGER.info(posToScan1);
                                            float soundDivider = ((float)traceContext.eyePosition.squareDistanceTo(posToScan1.getX(), posToScan1.getY(), posToScan1.getZ()) / 2) / ((float)posToScan1.distanceSq(vector3d, true) / this.scanWidth);
                                            blocksToScan.add(new ScanData(context.getWorld(), posToScan1, soundCounter <= soundLimit ? (0.4F / soundDivider) : null, 5.0F));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (ScanData scanData : blocksToScan) {
                    scanBlock(context.getItem(), context.getWorld(), context.getPlayer(), scanData.blockPos, blockTargets, scanData.scanVolume, scanData.scanPitch);
                }

            }

            case BIOMES: break;

            case ENTITIES: break;
        }

        return ActionResultType.SUCCESS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn) {
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public static void scanBlock(ItemStack stack, World world, PlayerEntity player, BlockPos pos, Block[] blocks, @Nullable Float volume, float pitch) {
        BlockState state = world.getBlockState(pos);
        if (Arrays.stream(blocks).anyMatch(((block) -> block == state.getBlock()))) {
            if (volume != null) world.playSound(player, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, volume, pitch);
        }
    }

    @Nonnull
    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        new NBTBuilder(stack.getOrCreateTag())
                .putString(KEY_MODE, ScannerMode.BLOCKS.name())
                .put(ScannerMode.BLOCKS.listName, new ListNBT())
                .put(ScannerMode.BIOMES.listName, new ListNBT())
                .put(ScannerMode.ENTITIES.listName, new ListNBT());
        return stack;
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(this.getDefaultInstance());
        }
    }

}
