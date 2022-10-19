package net.minecraft.client;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Camera {
    private boolean initialized;
    private BlockGetter level;
    private Entity entity;
    private Vec3 position = Vec3.ZERO;
    private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private float xRot;
    private float yRot;
    private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private boolean detached;
    private float eyeHeight;
    private float eyeHeightOld;
    public static final float FOG_DISTANCE_SCALE = 0.083333336F;

    public void setup(BlockGetter param0, Entity param1, boolean param2, boolean param3, float param4) {
        this.initialized = true;
        this.level = param0;
        this.entity = param1;
        this.detached = param2;
        this.setRotation(param1.getViewYRot(param4), param1.getViewXRot(param4));
        this.setPosition(
            Mth.lerp((double)param4, param1.xo, param1.getX()),
            Mth.lerp((double)param4, param1.yo, param1.getY()) + (double)Mth.lerp(param4, this.eyeHeightOld, this.eyeHeight),
            Mth.lerp((double)param4, param1.zo, param1.getZ())
        );
        if (param2) {
            if (param3) {
                this.setRotation(this.yRot + 180.0F, -this.xRot);
            }

            this.move(-this.getMaxZoom(4.0), 0.0, 0.0);
        } else if (param1 instanceof LivingEntity && ((LivingEntity)param1).isSleeping()) {
            Direction var0 = ((LivingEntity)param1).getBedOrientation();
            this.setRotation(var0 != null ? var0.toYRot() - 180.0F : 0.0F, 0.0F);
            this.move(0.0, 0.3, 0.0);
        }

    }

    public void tick() {
        if (this.entity != null) {
            this.eyeHeightOld = this.eyeHeight;
            this.eyeHeight += (this.entity.getEyeHeight() - this.eyeHeight) * 0.5F;
        }

    }

    private double getMaxZoom(double param0) {
        for(int var0 = 0; var0 < 8; ++var0) {
            float var1 = (float)((var0 & 1) * 2 - 1);
            float var2 = (float)((var0 >> 1 & 1) * 2 - 1);
            float var3 = (float)((var0 >> 2 & 1) * 2 - 1);
            var1 *= 0.1F;
            var2 *= 0.1F;
            var3 *= 0.1F;
            Vec3 var4 = this.position.add((double)var1, (double)var2, (double)var3);
            Vec3 var5 = new Vec3(
                this.position.x - (double)this.forwards.x() * param0 + (double)var1,
                this.position.y - (double)this.forwards.y() * param0 + (double)var2,
                this.position.z - (double)this.forwards.z() * param0 + (double)var3
            );
            HitResult var6 = this.level.clip(new ClipContext(var4, var5, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));
            if (var6.getType() != HitResult.Type.MISS) {
                double var7 = var6.getLocation().distanceTo(this.position);
                if (var7 < param0) {
                    param0 = var7;
                }
            }
        }

        return param0;
    }

    protected void move(double param0, double param1, double param2) {
        double var0 = (double)this.forwards.x() * param0 + (double)this.up.x() * param1 + (double)this.left.x() * param2;
        double var1 = (double)this.forwards.y() * param0 + (double)this.up.y() * param1 + (double)this.left.y() * param2;
        double var2 = (double)this.forwards.z() * param0 + (double)this.up.z() * param1 + (double)this.left.z() * param2;
        this.setPosition(new Vec3(this.position.x + var0, this.position.y + var1, this.position.z + var2));
    }

    protected void setRotation(float param0, float param1) {
        this.xRot = param1;
        this.yRot = param0;
        this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation.mul(Vector3f.YP.rotationDegrees(-param0));
        this.rotation.mul(Vector3f.XP.rotationDegrees(param1));
        this.forwards.set(0.0F, 0.0F, 1.0F);
        this.forwards.transform(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F);
        this.up.transform(this.rotation);
        this.left.set(1.0F, 0.0F, 0.0F);
        this.left.transform(this.rotation);
    }

    protected void setPosition(double param0, double param1, double param2) {
        this.setPosition(new Vec3(param0, param1, param2));
    }

    protected void setPosition(Vec3 param0) {
        this.position = param0;
        this.blockPosition.set(param0.x, param0.y, param0.z);
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }

    public float getXRot() {
        return this.xRot;
    }

    public float getYRot() {
        return this.yRot;
    }

    public Quaternion rotation() {
        return this.rotation;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean isDetached() {
        return this.detached;
    }

    public Camera.NearPlane getNearPlane() {
        Minecraft var0 = Minecraft.getInstance();
        double var1 = (double)var0.getWindow().getWidth() / (double)var0.getWindow().getHeight();
        double var2 = Math.tan((double)((float)var0.options.fov().get().intValue() * (float) (Math.PI / 180.0)) / 2.0) * 0.05F;
        double var3 = var2 * var1;
        Vec3 var4 = new Vec3(this.forwards).scale(0.05F);
        Vec3 var5 = new Vec3(this.left).scale(var3);
        Vec3 var6 = new Vec3(this.up).scale(var2);
        return new Camera.NearPlane(var4, var5, var6);
    }

    public FogType getFluidInCamera() {
        if (!this.initialized) {
            return FogType.NONE;
        } else {
            FluidState var0 = this.level.getFluidState(this.blockPosition);
            if (var0.is(FluidTags.WATER) && this.position.y < (double)((float)this.blockPosition.getY() + var0.getHeight(this.level, this.blockPosition))) {
                return FogType.WATER;
            } else {
                Camera.NearPlane var1 = this.getNearPlane();

                for(Vec3 var3 : Arrays.asList(var1.forward, var1.getTopLeft(), var1.getTopRight(), var1.getBottomLeft(), var1.getBottomRight())) {
                    Vec3 var4 = this.position.add(var3);
                    BlockPos var5 = new BlockPos(var4);
                    FluidState var6 = this.level.getFluidState(var5);
                    if (var6.is(FluidTags.LAVA)) {
                        if (var4.y <= (double)(var6.getHeight(this.level, var5) + (float)var5.getY())) {
                            return FogType.LAVA;
                        }
                    } else {
                        BlockState var7 = this.level.getBlockState(var5);
                        if (var7.is(Blocks.POWDER_SNOW)) {
                            return FogType.POWDER_SNOW;
                        }
                    }
                }

                return FogType.NONE;
            }
        }
    }

    public final Vector3f getLookVector() {
        return this.forwards;
    }

    public final Vector3f getUpVector() {
        return this.up;
    }

    public final Vector3f getLeftVector() {
        return this.left;
    }

    public void reset() {
        this.level = null;
        this.entity = null;
        this.initialized = false;
    }

    @OnlyIn(Dist.CLIENT)
    public static class NearPlane {
        final Vec3 forward;
        private final Vec3 left;
        private final Vec3 up;

        NearPlane(Vec3 param0, Vec3 param1, Vec3 param2) {
            this.forward = param0;
            this.left = param1;
            this.up = param2;
        }

        public Vec3 getTopLeft() {
            return this.forward.add(this.up).add(this.left);
        }

        public Vec3 getTopRight() {
            return this.forward.add(this.up).subtract(this.left);
        }

        public Vec3 getBottomLeft() {
            return this.forward.subtract(this.up).add(this.left);
        }

        public Vec3 getBottomRight() {
            return this.forward.subtract(this.up).subtract(this.left);
        }

        public Vec3 getPointOnPlane(float param0, float param1) {
            return this.forward.add(this.up.scale((double)param1)).subtract(this.left.scale((double)param0));
        }
    }
}
