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
import net.minecraft.util.math.MathHelper;
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
 **/
public class RayTraceHelper {

    /** Get the reach distance of a LivingEntity. **/
    private static double getReachDistance(LivingEntity entityIn){
        return Objects.requireNonNull(entityIn.getAttribute(ForgeMod.REACH_DISTANCE.get())).getValue();
    }

    // uses net.minecraft.item.Item::rayTrace math
    /**
     * Create a new {@link RayTraceContext} using an {@link Entity} and where that entity is looking.
     * @param entityIn Entity that you want to create a new ray from.
     * @param blockMode What {@link RayTraceContext.BlockMode} the ray should react to.
     * @param fluidMode What {@link RayTraceContext.FluidMode} the ray should react to.
     * @param maxDistance Max distance for the ray to travel.
     * @return {@link RayTraceContext} from entityIn's eye position.
     */
    public static RayTraceContext newEntityLookingAtContext(Entity entityIn, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode, double maxDistance){
        final float pitch = entityIn.rotationPitch;
        final float yaw = entityIn.rotationYaw;
        final float yawCos = MathHelper.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
        final float yawSin = MathHelper.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
        final float pitchCos = MathHelper.cos(-pitch * ((float)Math.PI / 180F));
        final float pitchSin = MathHelper.sin(-pitch * ((float)Math.PI / 180F));
        final float xAngle = yawSin * -pitchCos;
        final float zAngle = yawCos * -pitchCos;
        final Vector3d eyePosition = entityIn.getEyePosition(1.0F);
        final Vector3d eyeLookingTo = eyePosition.add((double)xAngle * maxDistance, (double)pitchSin * maxDistance, (double)zAngle * maxDistance);
        return new RayTraceContext(eyePosition, eyeLookingTo, blockMode, fluidMode, entityIn);
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
        return newEntityLookingAtContext(entityIn, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, 1024);
    }

    /** Do a ray trace, ignore fluids and hit block outlines. **/
    private static BlockRayTraceResult doBlockRayTrace(Entity entityIn, double maxDistance) {
        return entityIn.getEntityWorld().rayTraceBlocks(newEntityLookingAtContext(entityIn, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, maxDistance));
    }

    /** Do a ray trace, hit fluids and visual outlines. **/
    private static BlockRayTraceResult doFluidRayTrace(Entity entityIn, double maxDistance) {
        return entityIn.getEntityWorld().rayTraceBlocks(newEntityLookingAtContext(entityIn, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.ANY, maxDistance));
    }

    /** Get the block face a LivingEntity is looking at. **/
    public static Direction targetedBlockFace(LivingEntity entityIn, boolean ignoreReachDistance){
        return doBlockRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getFace();
    }

    /** Get the block face an Entity is looking at. **/
    public static Direction targetedBlockFace(Entity entityIn){
        return doBlockRayTrace(entityIn, 1024).getFace();
    }

    /** Get the BlockState of the block a LivingEntity is looking at. **/
    public static BlockState targetedBlockState(LivingEntity entityIn, IWorld iWorld, boolean ignoreReachDistance){
        return iWorld.getBlockState(doBlockRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getPos());
    }

    /** Get the BlockState of the block an Entity is looking at. **/
    public static BlockState targetedBlockState(Entity entityIn, IWorld iWorld){
        return iWorld.getBlockState(doBlockRayTrace(entityIn, 1024).getPos());
    }

    /** Get the FluidState of the block or fluid a LivingEntity is looking at. **/
    public static FluidState targetedFluidState(LivingEntity entityIn, IWorld iWorld, boolean ignoreReachDistance){
        return iWorld.getFluidState(doFluidRayTrace(entityIn, ignoreReachDistance ? 1024 : getReachDistance(entityIn)).getPos());
    }

    /** Get the FluidState of the block or fluid an Entity is looking at. **/
    public static FluidState targetedFluidState(Entity entityIn, IWorld iWorld){
        return iWorld.getFluidState(doFluidRayTrace(entityIn, 1024).getPos());
    }
}
