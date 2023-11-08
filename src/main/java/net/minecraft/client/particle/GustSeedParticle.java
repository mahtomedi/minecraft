package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GustSeedParticle extends NoRenderParticle {
    GustSeedParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.lifetime = 7;
    }

    @Override
    public void tick() {
        for(int var0 = 0; var0 < 3; ++var0) {
            double var1 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            double var2 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            double var3 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
            this.level.addParticle(ParticleTypes.GUST, var1, var2, var3, (double)((float)this.age / (float)this.lifetime), 0.0, 0.0);
        }

        if (this.age++ == this.lifetime) {
            this.remove();
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new GustSeedParticle(param1, param2, param3, param4);
        }
    }
}
