package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DustColorTransitionParticle extends DustParticleBase<DustColorTransitionOptions> {
    private final Vec3 fromColor;
    private final Vec3 toColor;

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

    private Vec3 randomizeColor(Vec3 param0, float param1) {
        return new Vec3(
            (double)this.randomizeColor((float)param0.x, param1),
            (double)this.randomizeColor((float)param0.y, param1),
            (double)this.randomizeColor((float)param0.z, param1)
        );
    }

    private void lerpColors(float param0) {
        float var0 = ((float)this.age + param0) / ((float)this.lifetime + 1.0F);
        Vec3 var1 = this.fromColor.lerp(this.toColor, (double)var0);
        this.rCol = (float)var1.x;
        this.gCol = (float)var1.y;
        this.bCol = (float)var1.z;
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
