package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DustColorTransitionParticle extends DustParticleBase<DustColorTransitionOptions> {
    private final Vector3f fromColor;
    private final Vector3f toColor;

    protected DustColorTransitionParticle(
        ClientLevel param0,
        double param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        DustColorTransitionOptions param7,
        SpriteSet param8
    ) {
        super(param0, param1, param2, param3, param4, param5, param6, param7, param8);
        float var0 = this.random.nextFloat() * 0.4F + 0.6F;
        this.fromColor = this.randomizeColor(param7.getFromColor(), var0);
        this.toColor = this.randomizeColor(param7.getToColor(), var0);
    }

    private Vector3f randomizeColor(Vector3f param0, float param1) {
        return new Vector3f(this.randomizeColor(param0.x(), param1), this.randomizeColor(param0.y(), param1), this.randomizeColor(param0.z(), param1));
    }

    private void lerpColors(float param0) {
        float var0 = ((float)this.age + param0) / ((float)this.lifetime + 1.0F);
        Vector3f var1 = this.fromColor.copy();
        var1.lerp(this.toColor, var0);
        this.rCol = var1.x();
        this.gCol = var1.y();
        this.bCol = var1.z();
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        this.lerpColors(param2);
        super.render(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<DustColorTransitionOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            DustColorTransitionOptions param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new DustColorTransitionParticle(param1, param2, param3, param4, param5, param6, param7, param0, this.sprites);
        }
    }
}
