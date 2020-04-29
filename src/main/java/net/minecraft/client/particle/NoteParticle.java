package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoteParticle extends TextureSheetParticle {
    private NoteParticle(ClientLevel param0, double param1, double param2, double param3, double param4) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.xd *= 0.01F;
        this.yd *= 0.01F;
        this.zd *= 0.01F;
        this.yd += 0.2;
        this.rCol = Math.max(0.0F, Mth.sin(((float)param4 + 0.0F) * (float) (Math.PI * 2)) * 0.65F + 0.35F);
        this.gCol = Math.max(0.0F, Mth.sin(((float)param4 + 0.33333334F) * (float) (Math.PI * 2)) * 0.65F + 0.35F);
        this.bCol = Math.max(0.0F, Mth.sin(((float)param4 + 0.6666667F) * (float) (Math.PI * 2)) * 0.65F + 0.35F);
        this.quadSize *= 1.5F;
        this.lifetime = 6;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
            this.move(this.xd, this.yd, this.zd);
            if (this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd *= 0.66F;
            this.yd *= 0.66F;
            this.zd *= 0.66F;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            NoteParticle var0 = new NoteParticle(param1, param2, param3, param4, param5);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
