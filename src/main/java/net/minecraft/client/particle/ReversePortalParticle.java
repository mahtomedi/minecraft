package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReversePortalParticle extends PortalParticle {
    ReversePortalParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.quadSize *= 1.5F;
        this.lifetime = (int)(Math.random() * 2.0) + 60;
    }

    @Override
    public float getQuadSize(float param0) {
        float var0 = 1.0F - ((float)this.age + param0) / ((float)this.lifetime * 1.5F);
        return this.quadSize * var0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float var0 = (float)this.age / (float)this.lifetime;
            this.x += this.xd * (double)var0;
            this.y += this.yd * (double)var0;
            this.z += this.zd * (double)var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ReversePortalProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ReversePortalProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            ReversePortalParticle var0 = new ReversePortalParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
