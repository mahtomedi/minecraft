package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ShriekParticle extends TextureSheetParticle {
    private static final Vector3f ROTATION_VECTOR = new Vector3f(0.5F, 0.5F, 0.5F).normalize();
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float MAGICAL_X_ROT = 1.0472F;
    private int delay;

    ShriekParticle(ClientLevel param0, double param1, double param2, double param3, int param4) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.quadSize = 0.85F;
        this.delay = param4;
        this.lifetime = 30;
        this.gravity = 0.0F;
        this.xd = 0.0;
        this.yd = 0.1;
        this.zd = 0.0;
    }

    @Override
    public float getQuadSize(float param0) {
        return this.quadSize * Mth.clamp(((float)this.age + param0) / (float)this.lifetime * 0.75F, 0.0F, 1.0F);
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        if (this.delay <= 0) {
            this.alpha = 1.0F - Mth.clamp(((float)this.age + param2) / (float)this.lifetime, 0.0F, 1.0F);
            this.renderRotatedParticle(param0, param1, param2, param0x -> param0x.mul(new Quaternionf().rotationX(-1.0472F)));
            this.renderRotatedParticle(param0, param1, param2, param0x -> param0x.mul(new Quaternionf().rotationYXZ((float) -Math.PI, 1.0472F, 0.0F)));
        }
    }

    private void renderRotatedParticle(VertexConsumer param0, Camera param1, float param2, Consumer<Quaternionf> param3) {
        Vec3 var0 = param1.getPosition();
        float var1 = (float)(Mth.lerp((double)param2, this.xo, this.x) - var0.x());
        float var2 = (float)(Mth.lerp((double)param2, this.yo, this.y) - var0.y());
        float var3 = (float)(Mth.lerp((double)param2, this.zo, this.z) - var0.z());
        Quaternionf var4 = new Quaternionf().setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
        param3.accept(var4);
        var4.transform(TRANSFORM_VECTOR);
        Vector3f[] var5 = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float var6 = this.getQuadSize(param2);

        for(int var7 = 0; var7 < 4; ++var7) {
            Vector3f var8 = var5[var7];
            var8.rotate(var4);
            var8.mul(var6);
            var8.add(var1, var2, var3);
        }

        int var9 = this.getLightColor(param2);
        this.makeCornerVertex(param0, var5[0], this.getU1(), this.getV1(), var9);
        this.makeCornerVertex(param0, var5[1], this.getU1(), this.getV0(), var9);
        this.makeCornerVertex(param0, var5[2], this.getU0(), this.getV0(), var9);
        this.makeCornerVertex(param0, var5[3], this.getU0(), this.getV1(), var9);
    }

    private void makeCornerVertex(VertexConsumer param0, Vector3f param1, float param2, float param3, int param4) {
        param0.vertex((double)param1.x(), (double)param1.y(), (double)param1.z())
            .uv(param2, param3)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(param4)
            .endVertex();
    }

    @Override
    public int getLightColor(float param0) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.delay > 0) {
            --this.delay;
        } else {
            super.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ShriekParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            ShriekParticleOption param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            ShriekParticle var0 = new ShriekParticle(param1, param2, param3, param4, param0.getDelay());
            var0.pickSprite(this.sprite);
            var0.setAlpha(1.0F);
            return var0;
        }
    }
}
