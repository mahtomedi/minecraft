package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Particle {
    private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    protected final ClientLevel level;
    protected double xo;
    protected double yo;
    protected double zo;
    protected double x;
    protected double y;
    protected double z;
    protected double xd;
    protected double yd;
    protected double zd;
    private AABB bb = INITIAL_AABB;
    protected boolean onGround;
    protected boolean hasPhysics = true;
    private boolean stoppedByCollision;
    protected boolean removed;
    protected float bbWidth = 0.6F;
    protected float bbHeight = 1.8F;
    protected final Random random = new Random();
    protected int age;
    protected int lifetime;
    protected float gravity;
    protected float rCol = 1.0F;
    protected float gCol = 1.0F;
    protected float bCol = 1.0F;
    protected float alpha = 1.0F;
    protected float roll;
    protected float oRoll;
    protected float friction = 0.98F;
    protected boolean speedUpWhenYMotionIsBlocked = false;

    protected Particle(ClientLevel param0, double param1, double param2, double param3) {
        this.level = param0;
        this.setSize(0.2F, 0.2F);
        this.setPos(param1, param2, param3);
        this.xo = param1;
        this.yo = param2;
        this.zo = param3;
        this.lifetime = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
    }

    public Particle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        this(param0, param1, param2, param3);
        this.xd = param4 + (Math.random() * 2.0 - 1.0) * 0.4F;
        this.yd = param5 + (Math.random() * 2.0 - 1.0) * 0.4F;
        this.zd = param6 + (Math.random() * 2.0 - 1.0) * 0.4F;
        double var0 = (Math.random() + Math.random() + 1.0) * 0.15F;
        double var1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        this.xd = this.xd / var1 * var0 * 0.4F;
        this.yd = this.yd / var1 * var0 * 0.4F + 0.1F;
        this.zd = this.zd / var1 * var0 * 0.4F;
    }

    public Particle setPower(float param0) {
        this.xd *= (double)param0;
        this.yd = (this.yd - 0.1F) * (double)param0 + 0.1F;
        this.zd *= (double)param0;
        return this;
    }

    public void setParticleSpeed(double param0, double param1, double param2) {
        this.xd = param0;
        this.yd = param1;
        this.zd = param2;
    }

    public Particle scale(float param0) {
        this.setSize(0.2F * param0, 0.2F * param0);
        return this;
    }

    public void setColor(float param0, float param1, float param2) {
        this.rCol = param0;
        this.gCol = param1;
        this.bCol = param2;
    }

    protected void setAlpha(float param0) {
        this.alpha = param0;
    }

    public void setLifetime(int param0) {
        this.lifetime = param0;
    }

    public int getLifetime() {
        return this.lifetime;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd -= 0.04 * (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd *= (double)this.friction;
            this.yd *= (double)this.friction;
            this.zd *= (double)this.friction;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

        }
    }

    public abstract void render(VertexConsumer var1, Camera var2, float var3);

    public abstract ParticleRenderType getRenderType();

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
            + ", Pos ("
            + this.x
            + ","
            + this.y
            + ","
            + this.z
            + "), RGBA ("
            + this.rCol
            + ","
            + this.gCol
            + ","
            + this.bCol
            + ","
            + this.alpha
            + "), Age "
            + this.age;
    }

    public void remove() {
        this.removed = true;
    }

    protected void setSize(float param0, float param1) {
        if (param0 != this.bbWidth || param1 != this.bbHeight) {
            this.bbWidth = param0;
            this.bbHeight = param1;
            AABB var0 = this.getBoundingBox();
            double var1 = (var0.minX + var0.maxX - (double)param0) / 2.0;
            double var2 = (var0.minZ + var0.maxZ - (double)param0) / 2.0;
            this.setBoundingBox(new AABB(var1, var0.minY, var2, var1 + (double)this.bbWidth, var0.minY + (double)this.bbHeight, var2 + (double)this.bbWidth));
        }

    }

    public void setPos(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        float var0 = this.bbWidth / 2.0F;
        float var1 = this.bbHeight;
        this.setBoundingBox(new AABB(param0 - (double)var0, param1, param2 - (double)var0, param0 + (double)var0, param1 + (double)var1, param2 + (double)var0));
    }

    public void move(double param0, double param1, double param2) {
        if (!this.stoppedByCollision) {
            double var0 = param0;
            double var1 = param1;
            double var2 = param2;
            if (this.hasPhysics && (param0 != 0.0 || param1 != 0.0 || param2 != 0.0)) {
                Vec3 var3 = Entity.collideBoundingBoxHeuristically(
                    null, new Vec3(param0, param1, param2), this.getBoundingBox(), this.level, CollisionContext.empty(), new RewindableStream<>(Stream.empty())
                );
                param0 = var3.x;
                param1 = var3.y;
                param2 = var3.z;
            }

            if (param0 != 0.0 || param1 != 0.0 || param2 != 0.0) {
                this.setBoundingBox(this.getBoundingBox().move(param0, param1, param2));
                this.setLocationFromBoundingbox();
            }

            if (Math.abs(var1) >= 1.0E-5F && Math.abs(param1) < 1.0E-5F) {
                this.stoppedByCollision = true;
            }

            this.onGround = var1 != param1 && var1 < 0.0;
            if (var0 != param0) {
                this.xd = 0.0;
            }

            if (var2 != param2) {
                this.zd = 0.0;
            }

        }
    }

    protected void setLocationFromBoundingbox() {
        AABB var0 = this.getBoundingBox();
        this.x = (var0.minX + var0.maxX) / 2.0;
        this.y = var0.minY;
        this.z = (var0.minZ + var0.maxZ) / 2.0;
    }

    protected int getLightColor(float param0) {
        BlockPos var0 = new BlockPos(this.x, this.y, this.z);
        return this.level.hasChunkAt(var0) ? LevelRenderer.getLightColor(this.level, var0) : 0;
    }

    public boolean isAlive() {
        return !this.removed;
    }

    public AABB getBoundingBox() {
        return this.bb;
    }

    public void setBoundingBox(AABB param0) {
        this.bb = param0;
    }

    public Optional<ParticleGroup> getParticleGroup() {
        return Optional.empty();
    }
}
