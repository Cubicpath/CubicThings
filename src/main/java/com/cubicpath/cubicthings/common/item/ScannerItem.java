////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.item;


import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.container.ScannerContainer;
import com.cubicpath.util.NBTBuilder;
import com.cubicpath.util.RayTraceHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class ScannerItem extends Item implements MenuProvider, IDefaultNBTHolder {
    
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

        public ListTag getTargetList(ItemStack stack){
            CompoundTag compoundnbt = stack.getOrCreateTag();
            if (!compoundnbt.contains(this.nbtName))
                compoundnbt.put(this.nbtName, new ListTag());
            return compoundnbt.getList(this.nbtName, this.nbtType);
        }

        public String toTitleCase() {
            char[] charArray = this.name().toLowerCase().toCharArray();
            charArray[0] = this.name().toUpperCase().charAt(0);
            return String.valueOf(charArray);
        }
    }

    protected record ScanContext(Level world, BlockPos blockPos, @Nullable Float scanVolume, float scanPitch){}

    //TODO: Make biome scanner mode

    public static final String KEY_MODE = "ScannerMode"; // String NBT
    public static final String KEY_TARGETS_BLOCK = "BlockTargets"; // List NBT
    public static final String KEY_TARGETS_BIOME = "BiomeTargets"; // List NBT
    public static final String KEY_TARGETS_ENTITY = "EntityTargets"; // List NBT
    private static final int SOUND_LIMIT = 100;
    public final int maxScanDistance, scanWidth;


    /** Return the {@linkplain #KEY_MODE} NBT value. */
    public static ScannerMode getScannerMode(ItemStack stack) {
        CompoundTag compoundnbt = stack.getOrCreateTag();
        if (!compoundnbt.contains(KEY_MODE)) new NBTBuilder(compoundnbt).putString(KEY_MODE, "Blocks");
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
        ListTag targetList = mode.getTargetList(stack);
        StringTag stringNBT = StringTag.valueOf(string);

        if (!targetList.contains(stringNBT)) {
            targetList.add(stringNBT);
        }
    }

    public static void removeTarget(ItemStack stack, ScannerMode mode, String string){
        ListTag targetList = mode.getTargetList(stack);
        StringTag stringNBT = StringTag.valueOf(string);

        targetList.remove(stringNBT);
    }

    public void setupNBT(ItemStack stack){
        new NBTBuilder(stack.getOrCreateTag())
                .putString(KEY_MODE, ScannerMode.BLOCKS.toTitleCase())
                .putList(ScannerMode.BLOCKS.nbtName, new ListTag())
                .putList(ScannerMode.BIOMES.nbtName, new ListTag())
                .putList(ScannerMode.ENTITIES.nbtName, new ListTag());
    }

    public ScannerItem(Properties properties, int maxScanDistance, int scanWidth) {
        super(properties);
        this.maxScanDistance = maxScanDistance;
        this.scanWidth = scanWidth % 2 == 0 ? scanWidth / 2 : (scanWidth - 1) / 2; // Odd numbers are treated literally. Even are halved.
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity.isCrouching()) {
            if (entity instanceof ServerPlayer) {
                NetworkHooks.openGui((ServerPlayer) entity, this, (packetBuffer) -> packetBuffer.writeItem(stack));
            }
            return true;
        }
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        final HashSet<ScanContext> blockPosToScan = new HashSet<>();
        final ItemStack itemStack = playerIn.getItemInHand(handIn);
        final ScannerMode mode = getScannerMode(itemStack);
        final ListTag targetNBT = mode.getTargetList(itemStack);
        Boolean[] scanResults;
        InteractionResultHolder<ItemStack> interactionResultHolder;

        switch (mode){
            default:
            case BLOCKS: {
                final Block[] blockTargets = new Block[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    blockTargets[i] = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(targetNBT.getString(i)));
                }

                // Scans client-side only
                if (worldIn.isClientSide()){
                    markPosToScan(blockPosToScan, playerIn, worldIn);
                }

                int i = 0;
                scanResults = new Boolean[blockPosToScan.size()];
                for (ScanContext scanContext : blockPosToScan) {
                    scanResults[i++] = scanForBlocks(scanContext.world(), playerIn, scanContext.blockPos(), blockTargets, scanContext.scanVolume(), scanContext.scanPitch());
                }

            }

            case BIOMES: {
                final Biome[] biomeTargets = new Biome[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    biomeTargets[i] = ForgeRegistries.BIOMES.getValue(ResourceLocation.tryParse(targetNBT.getString(i)));
                }

                //scans server-side only
                if (!worldIn.isClientSide()){
                    CubicThings.LOGGER.info("Test Biome Scan");
                }

                int i = 0;
                scanResults = new Boolean[blockPosToScan.size()];
                for (ScanContext scanContext : blockPosToScan) {
                    scanResults[i++] = scanForBiomes(scanContext.world(), playerIn, scanContext.blockPos(), biomeTargets, scanContext.scanVolume(), scanContext.scanPitch());
                }


            }

            case ENTITIES: {
                final EntityType<?>[] entityTargets = new EntityType<?>[targetNBT.size()];
                for (int i = 0; i < targetNBT.size(); i++) {
                    entityTargets[i] = ForgeRegistries.ENTITIES.getValue(ResourceLocation.tryParse(targetNBT.getString(i)));
                }

                // Scans client-side only
                if (worldIn.isClientSide()){
                    markPosToScan(blockPosToScan,playerIn, worldIn);
                }

                int i = 0;
                scanResults = new Boolean[blockPosToScan.size()];
                for (ScanContext scanContext : blockPosToScan) {
                    scanResults[i++] = scanForEntities(scanContext.world(), playerIn, scanContext.blockPos(), entityTargets, scanContext.scanVolume(), scanContext.scanPitch());
                }

                break;
            }
        }

        interactionResultHolder = Arrays.stream(scanResults).anyMatch((scanResult) -> scanResult) ? InteractionResultHolder.consume(itemStack) : InteractionResultHolder.pass(itemStack);
        // If any of the results are true, consume; else pass.
        if (playerIn instanceof ServerPlayer && interactionResultHolder.getResult() == InteractionResult.CONSUME && !playerIn.isCreative()) {
            itemStack.hurt(1, playerIn.getRandom(), (ServerPlayer)playerIn);
        }
        return interactionResultHolder;
    }

    /**
     * Scan a {@linkplain BlockState} at {@linkplain BlockPos} {@code pos}. If the {@linkplain Block} value of the {@code pos} is in the {@code blocks} Block array, play a sound
     * and return true.
     *
     * @param world Level to scan in
     * @param scanner Entity that started the scan
     * @param pos Position to scan
     * @param blocks Which blocks to scan for
     * @param volume Volume of audio feedback
     * @param pitch Pitch of audio feedback
     * @return Whether the scanned block is in the {@code blocks} {@linkplain Block} array
     */
    public static boolean scanForBlocks(Level world, LivingEntity scanner, BlockPos pos, Block[] blocks, @Nullable Float volume, float pitch) {
        BlockState state = world.getBlockState(pos);

        if (Arrays.stream(blocks).anyMatch(((block) -> block == state.getBlock()))) {
            if (volume != null) world.playSound(scanner instanceof Player ? (Player)scanner : null, pos, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, volume, pitch);
            return true;
        }
        return false;
    }

    public static boolean scanForBiomes(Level world, LivingEntity scanner, BlockPos pos, Biome[] blocks, @Nullable Float volume, float pitch) {
        return false;
    }


    /**
     * Scan a 1x1 area for {@linkplain net.minecraft.world.entity.Entity Entities} at {@linkplain BlockPos} {@code pos}. If the {@linkplain EntityType} value of the entity scanned is in the
     * {@code entityTypes} EntityType<?> array, play a sound and return true. Ignores the entity that started scanning.
     *
     * @param world Level to scan in
     * @param scanner Entity that started the scan
     * @param pos Position to scan
     * @param entityTypes Which entities to scan for
     * @param volume Volume of audio feedback
     * @param pitch Pitch of audio feedback
     * @return Whether the scanned entity is in the {@code entityTypes} {@linkplain EntityType} array
     */
    public static boolean scanForEntities(Level world, LivingEntity scanner, BlockPos pos, EntityType<?>[] entityTypes, @Nullable Float volume, float pitch) {
        List<Object> entitiesWithinAABB = new LinkedList<>();

        if (Arrays.stream(entityTypes).anyMatch((entityType) -> {
            entitiesWithinAABB.addAll(world.getEntitiesOfClass(entityType.getBaseClass(), new AABB(pos), (entity) -> !entity.equals(scanner)));
            return entitiesWithinAABB.size() > 0;
        })) {
            if (volume != null) world.playSound(scanner instanceof Player ? (Player)scanner : null, pos, SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, volume * 2 * entitiesWithinAABB.size(), pitch);
            return true;
        }
        return false;
    }

    /**
     * Follows a ray from an entity's head using {@link RayTraceHelper.LookingAtContext} and marks blocks around the ray for scanning.
     *
     * @param set Set to modify and return results to
     * @param entityIn Entity to start a ray from
     * @param worldIn Level to scan
     */
    public void markPosToScan(Set<ScanContext> set, LivingEntity entityIn, Level worldIn){
        final int worldHeight = worldIn.getHeight();
        final int worldDepth = 0;
        final RayTraceHelper.LookingAtContext traceContext = new RayTraceHelper.LookingAtContext(entityIn, maxScanDistance);

        for (int blockDistance = 0; blockDistance < traceContext.eyePosition.distanceTo(traceContext.eyeLookingTo); blockDistance++) {
            Vec3 vector3d = traceContext.eyePosition.add(new Vec3(traceContext.xAngle * blockDistance, traceContext.yAngle * blockDistance, traceContext.zAngle * blockDistance));
            final BlockPos posToScan = new BlockPos(vector3d);
            if (posToScan.getY() >= worldDepth && posToScan.getY() <= worldHeight) {
                set.add(new ScanContext(worldIn, posToScan,1.0F / ((float)traceContext.eyePosition.distanceToSqr(vector3d) / 2), 5.0F));

                int soundCounter = 0;
                for (int x = -this.scanWidth; x <= this.scanWidth; x++) {
                    for (int y = -this.scanWidth; y <= this.scanWidth ; y++) {
                        for (int z = -this.scanWidth; z <= this.scanWidth ; z++) {
                            soundCounter++;
                            final BlockPos posToScan1 = new BlockPos(vector3d.add(x, y, z));

                            if (set.stream().noneMatch(scanContext -> scanContext.blockPos == posToScan) && posToScan1.closerThan(vector3d, (float)this.maxScanDistance / 2)) {
                                float soundDivider = ((float)traceContext.eyePosition.distanceToSqr(posToScan1.getX(), posToScan1.getY(), posToScan1.getZ()) / 2) / ((float)posToScan1.distSqr(vector3d, true) / this.scanWidth);
                                set.add(new ScanContext(worldIn, posToScan1, soundCounter >= SOUND_LIMIT ? (0.25F / soundDivider) : null, 5.0F));
                            }
                        }
                    }
                }
            }
        }
    }

    public void markChunkToScan(Set<ScanContext> set, LivingEntity entityIn, Level worldIn){
        final RayTraceHelper.LookingAtContext traceContext = new RayTraceHelper.LookingAtContext(entityIn, maxScanDistance);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        setupNBT(stack);
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(this.getDefaultInstance());
        }
    }

    /** Container name for Scanner menu. */
    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("gui.cubicthings.scannerMenu.title");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ScannerContainer(windowId, inv, null);
    }

}
