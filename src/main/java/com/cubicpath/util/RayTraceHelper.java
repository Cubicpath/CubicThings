////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ForgeMod;

import java.util.Objects;

/**
 * Provides easy-to-access methods for simple ray tracing.
 *
 * @see RayTraceContext
 * @see BlockRayTraceResult
 * @see EntityRayTraceResult
 *
 * @since 1.0
 * @author Cubicpath
 */
public class RayTraceHelper {

    public static class LookingAtContext {
        public final double xAngle;
        public final double yAngle;
        public final double zAngle;
        public final Vector3d view;
        public final Vector3d eyePosition;
        public final Vector3d eyeLookingTo;

        public LookingAtContext(Entity entityIn, double maxDistance) {
            this.view = entityIn.getViewVector(1.0F);
            this.xAngle = view.x;
            this.yAngle = view.y;
            this.zAngle = view.z;
            this.eyePosition = entityIn.getEyePosition(1.0F);
            this.eyeLookingTo = eyePosition.add(xAngle * maxDistance, yAngle * maxDistance, zAngle * maxDistance);
        }
    }

    /** Get the reach distance of a LivingEntity. **/
    public static double getReachDistance(LivingEntity entityIn){
        return Objects.requireNonNull(entityIn.getAttribute(ForgeMod.REACH_DISTANCE.get()), "Reach distance cannot be null.").getValue();
    }


    /**
     * Create a new {@link RayTraceContext} using an {@link Entity} and where that entity is looking.
     * @param entityIn Entity that you want to create a new ray from.
     * @param blockMode What {@link RayTraceContext.BlockMode} the ray should react to.
     * @param fluidMode What {@link RayTraceContext.FluidMode} the ray should react to.
     * @param maxDistance Max distance for the ray to travel.
     * @return {@link RayTraceContext} from entityIn's eye position.
     */
    public static RayTraceContext newEntityLookingAtContext(Entity entityIn, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode, double maxDistance){
        Vector3d endVec = new LookingAtContext(entityIn, maxDistance).eyeLookingTo;
        return new RayTraceContext(entityIn.getEyePosition(1.0F), endVec, blockMode, fluidMode, entityIn);
    }

    /**
     * Create a new {@link RayTraceContext} using an {@link Entity} and where that entity is looking.<br>
     * <br>
     * blockMode – {@link RayTraceContext.BlockMode#OUTLINE}<br>
     * fluidMode – {@link RayTraceContext.FluidMode#NONE}<br>
     * maxDistance – 1024<br>
     * @param entityIn Entity that you want to create a new ray from.
     * @return {@link RayTraceContext} from entityIn's eye position.
     */
    public static RayTraceContext newEntityLookingAtContext(Entity entityIn){
        Vector3d endVec = new LookingAtContext(entityIn, 1024).eyeLookingTo;
        return new RayTraceContext(entityIn.getEyePosition(1.0F), endVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, entityIn);
    }

    /** Do a ray trace, ignore fluids and hit block outlines. **/
    public static BlockRayTraceResult doBlockRayTrace(Entity entityIn, double maxDistance) {
        return entityIn.level.clip(newEntityLookingAtContext(entityIn, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, maxDistance));
    }

    /** Do a ray trace, hit fluids and visual outlines. **/
    public static BlockRayTraceResult doFluidRayTrace(Entity entityIn, double maxDistance) {
        return entityIn.getCommandSenderWorld().clip(newEntityLookingAtContext(entityIn, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.ANY, maxDistance));
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
    public static BlockState targetedBlockState(LivingEntity entityIn, IWorld iWorld, boolean ignoreReachDistance){
        return iWorld.getBlockState(doBlockRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getBlockPos());
    }

    /** Get the BlockState of the block an Entity is looking at. **/
    public static BlockState targetedBlockState(Entity entityIn, IWorld iWorld){
        return iWorld.getBlockState(doBlockRayTrace(entityIn, 1024).getBlockPos());
    }

    /** Get the FluidState of the block or fluid a LivingEntity is looking at. **/
    public static FluidState targetedFluidState(LivingEntity entityIn, IWorld iWorld, boolean ignoreReachDistance){
        return iWorld.getFluidState(doFluidRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getBlockPos());
    }

    /** Get the FluidState of the block or fluid an Entity is looking at. **/
    public static FluidState targetedFluidState(Entity entityIn, IWorld iWorld){
        return iWorld.getFluidState(doFluidRayTrace(entityIn, 1024).getBlockPos());
    }
}