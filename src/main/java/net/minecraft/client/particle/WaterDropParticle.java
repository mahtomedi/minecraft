package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WaterDropParticle extends TextureSheetParticle {
    protected WaterDropParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.xd *= 0.3F;
        this.yd = Math.random() * 0.2F + 0.1F;
        this.zd *= 0.3F;
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
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
            this.yd -= (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98F;
            this.yd *= 0.98F;
            this.zd *= 0.98F;
            if (this.onGround) {
                if (Math.random() < 0.5) {
                    this.remove();
                }

                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

            BlockPos var0 = new BlockPos(this.x, this.y, this.z);
            double var1 = Math.max(
                this.level
                    .getBlockState(var0)
                    .getCollisionShape(this.level, var0)
                    .max(Direction.Axis.Y, this.x - (double)var0.getX(), this.z - (double)var0.getZ()),
                (double)this.level.getFluidState(var0).getHeight(this.level, var0)
            );
            if (var1 > 0.0 && this.y < (double)var0.getY() + var1) {
                this.remove();
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
            WaterDropParticle var0 = new WaterDropParticle(param1, param2, param3, param4);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
