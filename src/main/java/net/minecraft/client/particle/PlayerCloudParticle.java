package net.minecraft.client.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerCloudParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private PlayerCloudParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.sprites = param7;
        float var0 = 2.5F;
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.xd += param4;
        this.yd += param5;
        this.zd += param6;
        float var1 = 1.0F - (float)(Math.random() * 0.3F);
        this.rCol = var1;
        this.gCol = var1;
        this.bCol = var1;
        this.quadSize *= 1.875F;
        int var2 = (int)(8.0 / (Math.random() * 0.8 + 0.3));
        this.lifetime = (int)Math.max((float)var2 * 2.5F, 1.0F);
        this.hasPhysics = false;
        this.setSpriteFromAge(param7);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float param0) {
        return this.quadSize * Mth.clamp(((float)this.age + param0) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.96F;
            this.yd *= 0.96F;
            this.zd *= 0.96F;
            Player var0 = this.level.getNearestPlayer(this.x, this.y, this.z, 2.0, false);
            if (var0 != null) {
                AABB var1 = var0.getBoundingBox();
                if (this.y > var1.minY) {
                    this.y += (var1.minY - this.y) * 0.2;
                    this.yd += (var0.getDeltaMovement().y - this.yd) * 0.2;
                    this.setPos(this.x, this.y, this.z);
                }
            }

            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new PlayerCloudParticle(param1, param2, param3, param4, param5, param6, param7, this.sprites);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SneezeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SneezeProvider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            Particle var0 = new PlayerCloudParticle(param1, param2, param3, param4, param5, param6, param7, this.sprites);
            var0.setColor(200.0F, 50.0F, 120.0F);
            var0.setAlpha(0.4F);
            return var0;
        }
    }
}
