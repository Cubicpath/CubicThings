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
 * @since 1.0
 * @author Cubicpath
 */
public class RayTraceHelper {

    public static class LookingAtContext {
        public final float pitch;
        public final float yaw;
        public final float yawCos;
        public final float yawSin;
        public final float pitchCos;
        public final float pitchSin;
        public final float xAngle;
        public final float yAngle;
        public final float zAngle;
        public final Vector3d eyePosition;
        public final Vector3d eyeLookingTo;

        // uses net.minecraft.item.Item::rayTrace math
        public LookingAtContext(Entity entityIn, double maxDistance) {
            this.pitch = entityIn.rotationPitch;
            this.yaw = entityIn.rotationYaw;
            this.yawCos = MathHelper.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
            this.yawSin = MathHelper.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
            this.pitchCos = MathHelper.cos(-pitch * ((float)Math.PI / 180F));
            this.pitchSin = MathHelper.sin(-pitch * ((float)Math.PI / 180F));
            this.xAngle = yawSin * -pitchCos;
            this.yAngle = pitchSin;
            this.zAngle = yawCos * -pitchCos;
            this.eyePosition = entityIn.getEyePosition(1.0F);
            this.eyeLookingTo = eyePosition.add((double)xAngle * maxDistance, (double)yAngle * maxDistance, (double)zAngle * maxDistance);
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
        return entityIn.getEntityWorld().rayTraceBlocks(newEntityLookingAtContext(entityIn, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, maxDistance));
    }

    /** Do a ray trace, hit fluids and visual outlines. **/
    public static BlockRayTraceResult doFluidRayTrace(Entity entityIn, double maxDistance) {
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
