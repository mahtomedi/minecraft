package net.minecraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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
    private Vec3 forwards;
    private Vec3 up;
    private Vec3 left;
    private float xRot;
    private float yRot;
    private boolean detached;
    private boolean mirror;
    private float eyeHeight;
    private float eyeHeightOld;

    public void setup(BlockGetter param0, Entity param1, boolean param2, boolean param3, float param4) {
        this.initialized = true;
        this.level = param0;
        this.entity = param1;
        this.detached = param2;
        this.mirror = param3;
        this.setRotation(param1.getViewYRot(param4), param1.getViewXRot(param4));
        this.setPosition(
            Mth.lerp((double)param4, param1.xo, param1.x),
            Mth.lerp((double)param4, param1.yo, param1.y) + (double)Mth.lerp(param4, this.eyeHeightOld, this.eyeHeight),
            Mth.lerp((double)param4, param1.zo, param1.z)
        );
        if (param2) {
            if (param3) {
                this.yRot += 180.0F;
                this.xRot += -this.xRot * 2.0F;
                this.recalculateViewVector();
            }

            this.move(-this.getMaxZoom(4.0), 0.0, 0.0);
        } else if (param1 instanceof LivingEntity && ((LivingEntity)param1).isSleeping()) {
            Direction var0 = ((LivingEntity)param1).getBedOrientation();
            this.setRotation(var0 != null ? var0.toYRot() - 180.0F : 0.0F, 0.0F);
            this.move(0.0, 0.3, 0.0);
        }

        RenderSystem.rotatef(this.xRot, 1.0F, 0.0F, 0.0F);
        RenderSystem.rotatef(this.yRot + 180.0F, 0.0F, 1.0F, 0.0F);
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
                this.position.x - this.forwards.x * param0 + (double)var1 + (double)var3,
                this.position.y - this.forwards.y * param0 + (double)var2,
                this.position.z - this.forwards.z * param0 + (double)var3
            );
            HitResult var6 = this.level.clip(new ClipContext(var4, var5, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.entity));
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
        double var0 = this.forwards.x * param0 + this.up.x * param1 + this.left.x * param2;
        double var1 = this.forwards.y * param0 + this.up.y * param1 + this.left.y * param2;
        double var2 = this.forwards.z * param0 + this.up.z * param1 + this.left.z * param2;
        this.setPosition(new Vec3(this.position.x + var0, this.position.y + var1, this.position.z + var2));
    }

    protected void recalculateViewVector() {
        float var0 = Mth.cos((this.yRot + 90.0F) * (float) (Math.PI / 180.0));
        float var1 = Mth.sin((this.yRot + 90.0F) * (float) (Math.PI / 180.0));
        float var2 = Mth.cos(-this.xRot * (float) (Math.PI / 180.0));
        float var3 = Mth.sin(-this.xRot * (float) (Math.PI / 180.0));
        float var4 = Mth.cos((-this.xRot + 90.0F) * (float) (Math.PI / 180.0));
        float var5 = Mth.sin((-this.xRot + 90.0F) * (float) (Math.PI / 180.0));
        this.forwards = new Vec3((double)(var0 * var2), (double)var3, (double)(var1 * var2));
        this.up = new Vec3((double)(var0 * var4), (double)var5, (double)(var1 * var4));
        this.left = this.forwards.cross(this.up).scale(-1.0);
    }

    protected void setRotation(float param0, float param1) {
        this.xRot = param1;
        this.yRot = param0;
        this.recalculateViewVector();
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

    public Entity getEntity() {
        return this.entity;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean isDetached() {
        return this.detached;
    }

    public FluidState getFluidInCamera() {
        if (!this.initialized) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            FluidState var0 = this.level.getFluidState(this.blockPosition);
            return !var0.isEmpty() && this.position.y >= (double)((float)this.blockPosition.getY() + var0.getHeight(this.level, this.blockPosition))
                ? Fluids.EMPTY.defaultFluidState()
                : var0;
        }
    }

    public final Vec3 getLookVector() {
        return this.forwards;
    }

    public final Vec3 getUpVector() {
        return this.up;
    }

    public void reset() {
        this.level = null;
        this.entity = null;
        this.initialized = false;
    }
}
