////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.item;


import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.util.NBTBuilder;
import com.cubicpath.util.RayTraceHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class ScannerItem extends Item {

    public enum ScannerMode{
        BLOCKS(KEY_TARGETS_BLOCK,  8),
        BIOMES(KEY_TARGETS_BIOME,  8),
        ENTITIES(KEY_TARGETS_ENTITY,  8);

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
    public static final String KEY_TARGETS_BLOCK = "BlockTargets"; // List NBT
    public static final String KEY_TARGETS_BIOME = "BiomeTargets"; // List NBT
    public static final String KEY_TARGETS_ENTITY = "EntityTargets"; // List NBT
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

    public static void addTarget(ItemStack scanner, ScannerMode mode, String string){
        ListNBT targetList = mode.getTargetList(scanner);
        StringNBT stringNBT = StringNBT.valueOf(string);
        CubicThings.LOGGER.info(targetList);

        if (!targetList.contains(stringNBT)) {
            targetList.add(stringNBT);
        }
    }

    public static void setupNBT(ItemStack scanner){
        new NBTBuilder(scanner.getOrCreateTag())
                .putString(KEY_MODE, ScannerMode.BLOCKS.name())
                .put(ScannerMode.BLOCKS.listName, new ListNBT())
                .put(ScannerMode.BIOMES.listName, new ListNBT())
                .put(ScannerMode.ENTITIES.listName, new ListNBT());
    }

    public ScannerItem(Properties properties, int maxScanDistance, int scanWidth) {
        super(properties);
        this.maxScanDistance = maxScanDistance;
        this.scanWidth = scanWidth % 2 == 0 ? scanWidth / 2 : (scanWidth - 1) / 2; // Odd numbers are treated literally. Even are halved.
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity.isCrouching()) {
            ScannerMode mode;
            try {
                mode = ScannerMode.values()[(getScannerMode(stack).ordinal() + 1)];
            } catch (ArrayIndexOutOfBoundsException e) {
                mode = ScannerMode.values()[0];
            }
            setScannerMode(stack, mode);
            if (entity instanceof ClientPlayerEntity) {
                entity.getEntityWorld().playSound((ClientPlayerEntity)entity, entity.getPosition(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 1.0F, 0.2F);
                Minecraft.getInstance().ingameGUI.setOverlayMessage(ITextComponent.getTextComponentOrEmpty("\u00A77Changed scanner mode to: \u00A7b" + getScannerMode(stack)), false);
            }
            return true;
        }

        return super.onEntitySwing(stack, entity);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        final HashSet<ScanData> blockPosToScan = new HashSet<>();
        final ItemStack itemStack = playerIn.getHeldItem(handIn);
        final ScannerMode mode = getScannerMode(itemStack);
        final ListNBT targetNBT = mode.getTargetList(itemStack);

        switch (mode){
            default:
            case BLOCKS: {
                CubicThings.LOGGER.info(mode);
                addTarget(itemStack, ScannerMode.BLOCKS, Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(Blocks.DIAMOND_ORE)).toString());
                final Block[] blockTargets = new Block[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    blockTargets[i] = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(targetNBT.getString(i)));
                }

                // Scans client-side only
                if (playerIn instanceof ClientPlayerEntity){
                    markPosToScan(blockPosToScan, playerIn, worldIn);
                }

                for (ScanData scanData : blockPosToScan) {
                    scanForBlocks(itemStack, scanData.world, playerIn, scanData.blockPos, blockTargets, scanData.scanVolume, scanData.scanPitch);
                }

            }

            case BIOMES: break;

            case ENTITIES: {
                addTarget(itemStack, ScannerMode.ENTITIES, Objects.requireNonNull(ForgeRegistries.ENTITIES.getKey(EntityType.ZOMBIE)).toString());
                final EntityType<?>[] entityTargets = new EntityType<?>[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    entityTargets[i] = ForgeRegistries.ENTITIES.getValue(ResourceLocation.tryCreate(targetNBT.getString(i)));
                }

                // Scans client-side only
                if (playerIn instanceof ClientPlayerEntity){
                    markPosToScan(blockPosToScan,playerIn, worldIn);
                }

                for (ScanData scanData : blockPosToScan) {
                    scanForEntities(itemStack, scanData.world, playerIn, scanData.blockPos, entityTargets, scanData.scanVolume, scanData.scanPitch);
                }

                break;
            }
        }

        return ActionResult.resultSuccess(itemStack);
    }

    public static void scanForBlocks(ItemStack scanner, World world, PlayerEntity player, BlockPos pos, Block[] blocks, @Nullable Float volume, float pitch) {
        BlockState state = world.getBlockState(pos);

        if (Arrays.stream(blocks).anyMatch(((block) -> block == state.getBlock()))) {
            if (volume != null) world.playSound(player, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, volume, pitch);
        }
    }

    public static void scanForEntities(ItemStack scanner, World world, PlayerEntity player, BlockPos pos, EntityType<?>[] entityTypes, @Nullable Float volume, float pitch) {
        List<Object> entitiesWithinAABB = new LinkedList<>();

        if (Arrays.stream(entityTypes).anyMatch((entityType) -> {
            entitiesWithinAABB.addAll(world.getEntitiesWithinAABB(entityType, new AxisAlignedBB(pos), (a) -> true));
            return entitiesWithinAABB.size() > 0;
        })) {
            if (volume != null) world.playSound(player, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, volume * 2 * entitiesWithinAABB.size(), pitch);
        }
    }

    public void markPosToScan(Set<ScanData> set, PlayerEntity playerIn, World worldIn){
        BlockPos posToScan;
        BlockPos posToScan1;
        final int soundLimit = 100;
        final int worldHeight = worldIn.getHeight();
        final int worldDepth = 0;
        final RayTraceHelper.LookingAtContext traceContext = new RayTraceHelper.LookingAtContext(playerIn, maxScanDistance);

        for (int i = 0; i < traceContext.eyePosition.distanceTo(traceContext.eyeLookingTo); i++) {
            Vector3d vector3d = traceContext.eyePosition.add(new Vector3d(traceContext.xAngle * i, traceContext.yAngle * i, traceContext.zAngle * i));
            posToScan = new BlockPos(vector3d);
            if (posToScan.getY() >= worldDepth && posToScan.getY() <= worldHeight) {
                set.add(new ScanData(worldIn, posToScan,0.8F / ((float)traceContext.eyePosition.squareDistanceTo(vector3d) / 2), 5.0F));

                int soundCounter = 0;
                for (int x = -this.scanWidth; x <= this.scanWidth; x++) {
                    for (int y = -this.scanWidth; y <= this.scanWidth ; y++) {
                        for (int z = -this.scanWidth; z <= this.scanWidth ; z++) {
                            soundCounter++;
                            posToScan1 = new BlockPos(vector3d.add(x, y, z));
                            if (posToScan1 != posToScan && posToScan1.withinDistance(vector3d, (float)this.maxScanDistance / 2)) {
                                //CubicThings.LOGGER.info(posToScan1);
                                float soundDivider = ((float)traceContext.eyePosition.squareDistanceTo(posToScan1.getX(), posToScan1.getY(), posToScan1.getZ()) / 2) / ((float)posToScan1.distanceSq(vector3d, true) / this.scanWidth);
                                set.add(new ScanData(worldIn, posToScan1, soundCounter <= soundLimit ? (0.4F / soundDivider) : null, 5.0F));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    @Nonnull
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        setupNBT(stack);
        return stack;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(this.getDefaultInstance());
        }
    }

}
