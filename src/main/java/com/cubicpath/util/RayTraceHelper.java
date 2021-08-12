////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.Objects;

/**
 * Provides easy-to-access methods for simple ray tracing.
 *
 * @see ClipContext
 * @see BlockHitResult
 * @see EntityHitResult
 *
 * @since 1.0
 * @author Cubicpath
 */
public final class RayTraceHelper {
    private RayTraceHelper(){
        throw new IllegalStateException();
    }

    public static class LookingAtContext {
        public final double xAngle;
        public final double yAngle;
        public final double zAngle;
        public final Vec3 view;
        public final Vec3 eyePosition;
        public final Vec3 eyeLookingTo;

        public LookingAtContext(Entity entityIn, double maxDistance) {
            this.view = entityIn.getViewVector(1.0F);
            this.xAngle = view.x;
            this.yAngle = view.y;
            this.zAngle = view.z;
            this.eyePosition = entityIn.getEyePosition(1.0F);
            this.eyeLookingTo = eyePosition.add(view.x * maxDistance, view.y * maxDistance, view.z * maxDistance);
        }
    }

    /** Get the reach distance of a LivingEntity. **/
    public static double getReachDistance(LivingEntity entityIn){
        return Objects.requireNonNull(entityIn.getAttribute(ForgeMod.REACH_DISTANCE.get()), "Reach distance cannot be null.").getValue();
    }


    /**
     * Create a new {@link ClipContext} using an {@link Entity} and where that entity is looking.
     * @param entityIn Entity that you want to create a new ray from.
     * @param block What {@link ClipContext.Block} the ray should react to.
     * @param fluid What {@link ClipContext.Fluid} the ray should react to.
     * @param maxDistance Ma-distance for the ray to travel.
     * @return {@link ClipContext} from entityIn's eye position.
     */
    public static ClipContext newEntityLookingAtContext(Entity entityIn, ClipContext.Block block, ClipContext.Fluid fluid, double maxDistance){
        Vec3 endVec = new LookingAtContext(entityIn, maxDistance).eyeLookingTo;
        return new ClipContext(entityIn.getEyePosition(1.0F), endVec, block, fluid, entityIn);
    }

    /**
     * Create a new {@link ClipContext} using an {@link Entity} and where that entity is looking.<br>
     * <br>
     * block – {@link ClipContext.Block#OUTLINE}<br>
     * fluid – {@link ClipContext.Fluid#NONE}<br>
     * maxDistance – 1024<br>
     * @param entityIn Entity that you want to create a new ray from.
     * @return {@link ClipContext} from entityIn's eye position.
     */
    public static ClipContext newEntityLookingAtContext(Entity entityIn){
        Vec3 endVec = new LookingAtContext(entityIn, 1024).eyeLookingTo;
        return new ClipContext(entityIn.getEyePosition(1.0F), endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entityIn);
    }

    /** Do a ray trace, ignore fluids and hit block outlines. **/
    public static BlockHitResult doBlockRayTrace(Entity entityIn, double maxDistance) {
        return entityIn.getCommandSenderWorld().clip(newEntityLookingAtContext(entityIn, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, maxDistance));
    }

    /** Do a ray trace, hit fluids and visual outlines. **/
    public static BlockHitResult doFluidRayTrace(Entity entityIn, double maxDistance) {
        return entityIn.getCommandSenderWorld().clip(newEntityLookingAtContext(entityIn, ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, maxDistance));
    }

    /** Get the block face a LivingEntity is looking at. **/
    public static Direction targetedBlockFace(LivingEntity entityIn, boolean ignoreReachDistance){
        return doBlockRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getDirection();
    }

    /** Get the block face an Entity is looking at. **/
    public static Direction targetedBlockFace(Entity entityIn){
        return doBlockRayTrace(entityIn, 1024).getDirection();
    }

    /** Get the BlockState of the block a LivingEntity is looking at. **/
    public static BlockState targetedBlockState(LivingEntity entityIn, BlockGetter blockGetter, boolean ignoreReachDistance){
        return blockGetter.getBlockState(doBlockRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getBlockPos());
    }

    /** Get the BlockState of the block an Entity is looking at. **/
    public static BlockState targetedBlockState(Entity entityIn, BlockGetter blockGetter){
        return blockGetter.getBlockState(doBlockRayTrace(entityIn, 1024).getBlockPos());
    }

    /** Get the FluidState of the block or fluid a LivingEntity is looking at. **/
    public static FluidState targetedFluidState(LivingEntity entityIn, BlockGetter blockGetter, boolean ignoreReachDistance){
        return blockGetter.getFluidState(doFluidRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getBlockPos());
    }

    /** Get the FluidState of the block or fluid an Entity is looking at. **/
    public static FluidState targetedFluidState(Entity entityIn, BlockGetter blockGetter){
        return blockGetter.getFluidState(doFluidRayTrace(entityIn, 1024).getBlockPos());
    }
}
