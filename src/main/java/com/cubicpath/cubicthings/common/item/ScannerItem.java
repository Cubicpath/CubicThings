////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.item;


import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.container.ScannerContainer;
import com.cubicpath.util.NBTBuilder;
import com.cubicpath.util.RayTraceHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class ScannerItem extends Item implements INamedContainerProvider {

    public enum ScannerMode{
        BLOCKS(KEY_TARGETS_BLOCK,  8),
        BIOMES(KEY_TARGETS_BIOME,  8),
        ENTITIES(KEY_TARGETS_ENTITY,  8);

        public final String nbtName;
        public final int nbtType;

        ScannerMode(String nbtName, int nbtType){
            this.nbtName = nbtName;
            this.nbtType = nbtType;
        }

        public ListNBT getTargetList(ItemStack stack){
            CompoundNBT compoundnbt = stack.getOrCreateTag();
            if (!compoundnbt.contains(this.nbtName))
                compoundnbt.put(this.nbtName, new ListNBT());
            return compoundnbt.getList(this.nbtName, this.nbtType);
        }

        public String toTitleCase() {
            char[] charArray = this.name().toLowerCase().toCharArray();
            charArray[0] = this.name().toUpperCase().charAt(0);
            return String.valueOf(charArray);
        }
    }

    protected static class ScanContext {
        public final World world;
        public final BlockPos blockPos;
        public final Float scanVolume;
        public final float scanPitch;

        public ScanContext(World world, BlockPos blockPos, @Nullable Float scanVolume, float scanPitch) {
            this.world = world;
            this.blockPos = blockPos;
            this.scanVolume = scanVolume;
            this.scanPitch = scanPitch;
        }

    }

    //TODO: Make biome scanner mode

    public static final String KEY_MODE = "ScannerMode"; // String NBT
    public static final String KEY_TARGETS_BLOCK = "BlockTargets"; // List NBT
    public static final String KEY_TARGETS_BIOME = "BiomeTargets"; // List NBT
    public static final String KEY_TARGETS_ENTITY = "EntityTargets"; // List NBT
    private static final int SOUND_LIMIT = 100;
    public final int maxScanDistance;
    public final int scanWidth;


    /** Return the {@linkplain #KEY_MODE} NBT value. */
    public static ScannerMode getScannerMode(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getOrCreateTag();
        if (!compoundnbt.contains(KEY_MODE)) compoundnbt.putString(KEY_MODE, "Blocks");
        if (!compoundnbt.getString(KEY_MODE).isEmpty())
            return ScannerMode.valueOf(compoundnbt.getString(KEY_MODE).toUpperCase());
        return ScannerMode.BLOCKS;
    }

    /** Set the {@linkplain #KEY_MODE} NBT value to a given ScannerMode value. */
    public static void setScannerMode(ItemStack stack, ScannerMode mode) {
        new NBTBuilder(stack.getOrCreateTag())
                .putString(KEY_MODE, mode.toTitleCase());
    }

    public static void addTarget(ItemStack stack, ScannerMode mode, String string){
        ListNBT targetList = mode.getTargetList(stack);
        StringNBT stringNBT = StringNBT.valueOf(string);

        if (!targetList.contains(stringNBT)) {
            targetList.add(stringNBT);
        }
    }

    public static void removeTarget(ItemStack stack, ScannerMode mode, String string){
        ListNBT targetList = mode.getTargetList(stack);
        StringNBT stringNBT = StringNBT.valueOf(string);

        targetList.remove(stringNBT);
    }

    public static void setupNBT(ItemStack stack){
        new NBTBuilder(stack.getOrCreateTag())
                .putString(KEY_MODE, ScannerMode.BLOCKS.toTitleCase())
                .put(ScannerMode.BLOCKS.nbtName, new ListNBT())
                .put(ScannerMode.BIOMES.nbtName, new ListNBT())
                .put(ScannerMode.ENTITIES.nbtName, new ListNBT());
    }

    public ScannerItem(Properties properties, int maxScanDistance, int scanWidth) {
        super(properties);
        this.maxScanDistance = maxScanDistance;
        this.scanWidth = scanWidth % 2 == 0 ? scanWidth / 2 : (scanWidth - 1) / 2; // Odd numbers are treated literally. Even are halved.
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity.isCrouching()) {
            if (entity instanceof ServerPlayerEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) entity, this, (packetBuffer) -> packetBuffer.writeItemStack(stack));
            }
            return true;
        }
        return super.onEntitySwing(stack, entity);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        final HashSet<ScanContext> blockPosToScan = new HashSet<>();
        final ItemStack itemStack = playerIn.getHeldItem(handIn);
        final ScannerMode mode = getScannerMode(itemStack);
        final ListNBT targetNBT = mode.getTargetList(itemStack);
        Boolean[] scanResults;
        ActionResult<ItemStack> actionResult;

        switch (mode){
            default:
            case BLOCKS: {
                final Block[] blockTargets = new Block[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    blockTargets[i] = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(targetNBT.getString(i)));
                }

                // Scans client-side only
                if (worldIn.isRemote()){
                    markPosToScan(blockPosToScan, playerIn, worldIn);
                }

                int i = 0;
                scanResults = new Boolean[blockPosToScan.size()];
                for (ScanContext scanContext : blockPosToScan) {
                    scanResults[i++] = scanForBlocks(scanContext.world, playerIn, scanContext.blockPos, blockTargets, scanContext.scanVolume, scanContext.scanPitch);
                }

            }

            case BIOMES: {
                final Biome[] biomeTargets = new Biome[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    biomeTargets[i] = ForgeRegistries.BIOMES.getValue(ResourceLocation.tryCreate(targetNBT.getString(i)));
                }

                //scans server-side only
                if (!worldIn.isRemote()){
                    CubicThings.LOGGER.info("Test Biome Scan");
                }

                int i = 0;
                scanResults = new Boolean[blockPosToScan.size()];
                for (ScanContext scanContext : blockPosToScan) {
                    scanResults[i++] = scanForBiomes(scanContext.world, playerIn, scanContext.blockPos, biomeTargets, scanContext.scanVolume, scanContext.scanPitch);
                }


            }

            case ENTITIES: {
                final EntityType<?>[] entityTargets = new EntityType<?>[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    entityTargets[i] = ForgeRegistries.ENTITIES.getValue(ResourceLocation.tryCreate(targetNBT.getString(i)));
                }

                // Scans client-side only
                if (worldIn.isRemote()){
                    markPosToScan(blockPosToScan,playerIn, worldIn);
                }

                int i = 0;
                scanResults = new Boolean[blockPosToScan.size()];
                for (ScanContext scanContext : blockPosToScan) {
                    scanResults[i++] = scanForEntities(scanContext.world, playerIn, scanContext.blockPos, entityTargets, scanContext.scanVolume, scanContext.scanPitch);
                }

                break;
            }
        }

        actionResult = Arrays.stream(scanResults).anyMatch((scanResult) -> scanResult) ? ActionResult.resultConsume(itemStack) : ActionResult.resultPass(itemStack);
        // If any of the results are true, consume; else pass.
        if (playerIn instanceof ServerPlayerEntity && actionResult.getType() == ActionResultType.CONSUME && !playerIn.isCreative()) {
            itemStack.attemptDamageItem(1, playerIn.getRNG(), (ServerPlayerEntity)playerIn);
        }
        return actionResult;
    }

    /**
     * Scan a {@linkplain BlockState} at {@linkplain BlockPos} {@code pos}. If the {@linkplain Block} value of the {@code pos} is in the {@code blocks} Block array, play a sound
     * and return true.
     *
     * @param world World to scan in
     * @param scanner Entity that started the scan
     * @param pos Position to scan
     * @param blocks Which blocks to scan for
     * @param volume Volume of audio feedback
     * @param pitch Pitch of audio feedback
     * @return Whether the scanned block is in the {@code blocks} {@linkplain Block} array
     */
    public static boolean scanForBlocks(World world, LivingEntity scanner, BlockPos pos, Block[] blocks, @Nullable Float volume, float pitch) {
        BlockState state = world.getBlockState(pos);

        if (Arrays.stream(blocks).anyMatch(((block) -> block == state.getBlock()))) {
            if (volume != null) world.playSound(scanner instanceof PlayerEntity ? (PlayerEntity)scanner : null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, volume, pitch);
            return true;
        }
        return false;
    }

    public static boolean scanForBiomes(World world, LivingEntity scanner, BlockPos pos, Biome[] blocks, @Nullable Float volume, float pitch) {
        return false;
    }


    /**
     * Scan a 1x1 area for {@linkplain net.minecraft.entity.Entity Entities} at {@linkplain BlockPos} {@code pos}. If the {@linkplain EntityType} value of the entity scanned is in the
     * {@code entityTypes} EntityType<?> array, play a sound and return true. Ignores the entity that started scanning.
     *
     * @param world World to scan in
     * @param scanner Entity that started the scan
     * @param pos Position to scan
     * @param entityTypes Which blocks to scan for
     * @param volume Volume of audio feedback
     * @param pitch Pitch of audio feedback
     * @return Whether the scanned entity is in the {@code entityTypes} {@linkplain EntityType} array
     */
    public static boolean scanForEntities(World world, LivingEntity scanner, BlockPos pos, EntityType<?>[] entityTypes, @Nullable Float volume, float pitch) {
        List<Object> entitiesWithinAABB = new LinkedList<>();

        if (Arrays.stream(entityTypes).anyMatch((entityType) -> {
            entitiesWithinAABB.addAll(world.getEntitiesWithinAABB(entityType, new AxisAlignedBB(pos), (a) -> !a.equals(scanner)));
            return entitiesWithinAABB.size() > 0;
        })) {
            if (volume != null) world.playSound(scanner instanceof PlayerEntity ? (PlayerEntity)scanner : null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, volume * 2 * entitiesWithinAABB.size(), pitch);
            return true;
        }
        return false;
    }

    /**
     * Follows a ray from an entity's head using {@link RayTraceHelper.LookingAtContext} and marks blocks around the ray for scanning.
     *
     * @param set Set to modify and return results to
     * @param entityIn Entity to start a ray from
     * @param worldIn World to scan
     */
    public void markPosToScan(Set<ScanContext> set, LivingEntity entityIn, World worldIn){
        final int worldHeight = worldIn.getHeight();
        final int worldDepth = 0;
        final RayTraceHelper.LookingAtContext traceContext = new RayTraceHelper.LookingAtContext(entityIn, maxScanDistance);

        for (int blockDistance = 0; blockDistance < traceContext.eyePosition.distanceTo(traceContext.eyeLookingTo); blockDistance++) {
            Vector3d vector3d = traceContext.eyePosition.add(new Vector3d(traceContext.xAngle * blockDistance, traceContext.yAngle * blockDistance, traceContext.zAngle * blockDistance));
            final BlockPos posToScan = new BlockPos(vector3d);
            if (posToScan.getY() >= worldDepth && posToScan.getY() <= worldHeight) {
                set.add(new ScanContext(worldIn, posToScan,1.0F / ((float)traceContext.eyePosition.squareDistanceTo(vector3d) / 2), 5.0F));

                int soundCounter = 0;
                for (int x = -this.scanWidth; x <= this.scanWidth; x++) {
                    for (int y = -this.scanWidth; y <= this.scanWidth ; y++) {
                        for (int z = -this.scanWidth; z <= this.scanWidth ; z++) {
                            soundCounter++;
                            final BlockPos posToScan1 = new BlockPos(vector3d.add(x, y, z));

                            if (set.stream().noneMatch(scanContext -> scanContext.blockPos == posToScan) && posToScan1.withinDistance(vector3d, (float)this.maxScanDistance / 2)) {
                                float soundDivider = ((float)traceContext.eyePosition.squareDistanceTo(posToScan1.getX(), posToScan1.getY(), posToScan1.getZ()) / 2) / ((float)posToScan1.distanceSq(vector3d, true) / this.scanWidth);
                                set.add(new ScanContext(worldIn, posToScan1, soundCounter >= SOUND_LIMIT ? (0.25F / soundDivider) : null, 5.0F));
                            }
                        }
                    }
                }
            }
        }
    }

    public void markChunkToScan(Set<ScanContext> set, LivingEntity entityIn, World worldIn){
        final RayTraceHelper.LookingAtContext traceContext = new RayTraceHelper.LookingAtContext(entityIn, maxScanDistance);


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

    /** Container name for Scanner menu. */
    @Override
    @Nonnull
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.cubicthings.scannerMenu.title");
    }

    @Override
    @Nullable
    @ParametersAreNonnullByDefault
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ScannerContainer(windowId, inv, null);
    }

}
