package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuspendedParticle extends TextureSheetParticle {
    private SuspendedParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2 - 0.125, param3);
        this.rCol = 0.4F;
        this.gCol = 0.4F;
        this.bCol = 0.7F;
        this.setSize(0.01F, 0.01F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
    }

    private SuspendedParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2 - 0.125, param3, param4, param5, param6);
        this.setSize(0.01F, 0.01F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.6F;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CrimsonSporeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public CrimsonSporeProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedParticle var0 = new SuspendedParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            var0.setColor(0.9F, 0.4F, 0.5F);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class UnderwaterProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public UnderwaterProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedParticle var0 = new SuspendedParticle(param1, param2, param3, param4);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WarpedSporeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WarpedSporeProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedParticle var0 = new SuspendedParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            var0.setColor(0.1F, 0.1F, 0.3F);
            var0.setSize(0.001F, 0.001F);
            return var0;
        }
    }
}
