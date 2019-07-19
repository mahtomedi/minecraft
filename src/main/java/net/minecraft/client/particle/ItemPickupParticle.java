package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemPickupParticle extends Particle {
    private final Entity itemEntity;
    private final Entity target;
    private int life;
    private final int lifeTime;
    private final float yOffs;
    private final EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

    public ItemPickupParticle(Level param0, Entity param1, Entity param2, float param3) {
        this(param0, param1, param2, param3, param1.getDeltaMovement());
    }

    private ItemPickupParticle(Level param0, Entity param1, Entity param2, float param3, Vec3 param4) {
        super(param0, param1.x, param1.y, param1.z, param4.x, param4.y, param4.z);
        this.itemEntity = param1;
        this.target = param2;
        this.lifeTime = 3;
        this.yOffs = param3;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void render(BufferBuilder param0, Camera param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        float var0 = ((float)this.life + param2) / (float)this.lifeTime;
        var0 *= var0;
        double var1 = this.itemEntity.x;
        double var2 = this.itemEntity.y;
        double var3 = this.itemEntity.z;
        double var4 = Mth.lerp((double)param2, this.target.xOld, this.target.x);
        double var5 = Mth.lerp((double)param2, this.target.yOld, this.target.y) + (double)this.yOffs;
        double var6 = Mth.lerp((double)param2, this.target.zOld, this.target.z);
        double var7 = Mth.lerp((double)var0, var1, var4);
        double var8 = Mth.lerp((double)var0, var2, var5);
        double var9 = Mth.lerp((double)var0, var3, var6);
        int var10 = this.getLightColor(param2);
        int var11 = var10 % 65536;
        int var12 = var10 / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var11, (float)var12);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        var7 -= xOff;
        var8 -= yOff;
        var9 -= zOff;
        GlStateManager.enableLighting();
        this.entityRenderDispatcher.render(this.itemEntity, var7, var8, var9, this.itemEntity.yRot, param2, false);
    }

    @Override
    public void tick() {
        ++this.life;
        if (this.life == this.lifeTime) {
            this.remove();
        }

    }
}
