package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@OnlyIn(Dist.CLIENT)
public class GustDustParticle extends TextureSheetParticle {
    private final Vector3f fromColor = new Vector3f(0.5F, 0.5F, 0.5F);
    private final Vector3f toColor = new Vector3f(1.0F, 1.0F, 1.0F);

    GustDustParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3);
        this.hasPhysics = false;
        this.xd = param4 + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
        this.zd = param6 + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
        double var0 = Math.random() * 2.0;
        double var1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        this.xd = this.xd / var1 * var0 * 0.4F;
        this.zd = this.zd / var1 * var0 * 0.4F;
        this.quadSize *= 2.5F;
        this.xd *= 0.08F;
        this.zd *= 0.08F;
        this.lifetime = 18 + this.random.nextInt(4);
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        this.lerpColors(param2);
        super.render(param0, param1, param2);
    }

    private void lerpColors(float param0) {
        float var0 = ((float)this.age + param0) / (float)(this.lifetime + 1);
        Vector3f var1 = new Vector3f((Vector3fc)this.fromColor).lerp(this.toColor, var0);
        this.rCol = var1.x();
        this.gCol = var1.y();
        this.bCol = var1.z();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.xo = this.x;
            this.zo = this.z;
            this.move(this.xd, 0.0, this.zd);
            this.xd *= 0.99;
            this.zd *= 0.99;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class GustDustParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public GustDustParticleProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            GustDustParticle var0 = new GustDustParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
