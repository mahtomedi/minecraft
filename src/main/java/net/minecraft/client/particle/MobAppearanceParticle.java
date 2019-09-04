package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobAppearanceParticle extends Particle {
    private LivingEntity displayEntity;

    private MobAppearanceParticle(Level param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3);
        this.gravity = 0.0F;
        this.lifetime = 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.displayEntity == null) {
            ElderGuardian var0 = EntityType.ELDER_GUARDIAN.create(this.level);
            var0.setGhost();
            this.displayEntity = var0;
        }

    }

    @Override
    public void render(BufferBuilder param0, Camera param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (this.displayEntity != null) {
            EntityRenderDispatcher var0 = Minecraft.getInstance().getEntityRenderDispatcher();
            var0.setPosition(Particle.xOff, Particle.yOff, Particle.zOff);
            float var1 = 1.0F / ElderGuardian.ELDER_SIZE_SCALE;
            float var2 = ((float)this.age + param2) / (float)this.lifetime;
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            float var3 = 240.0F;
            RenderSystem.glMultiTexCoord2f(33985, 240.0F, 240.0F);
            RenderSystem.pushMatrix();
            float var4 = 0.05F + 0.5F * Mth.sin(var2 * (float) Math.PI);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, var4);
            RenderSystem.translatef(0.0F, 1.8F, 0.0F);
            RenderSystem.rotatef(180.0F - param1.getYRot(), 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(60.0F - 150.0F * var2 - param1.getXRot(), 1.0F, 0.0F, 0.0F);
            RenderSystem.translatef(0.0F, -0.4F, -1.5F);
            RenderSystem.scalef(var1, var1, var1);
            this.displayEntity.yRot = 0.0F;
            this.displayEntity.yHeadRot = 0.0F;
            this.displayEntity.yRotO = 0.0F;
            this.displayEntity.yHeadRotO = 0.0F;
            var0.render(this.displayEntity, 0.0, 0.0, 0.0, 0.0F, param2, false);
            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new MobAppearanceParticle(param1, param2, param3, param4);
        }
    }
}
