package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class VibrationSignalParticle extends TextureSheetParticle {
    private final PositionSource target;
    private float rot;
    private float rotO;
    private float pitch;
    private float pitchO;

    VibrationSignalParticle(ClientLevel param0, double param1, double param2, double param3, PositionSource param4, int param5) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.quadSize = 0.3F;
        this.target = param4;
        this.lifetime = param5;
        Optional<Vec3> var0 = param4.getPosition(param0);
        if (var0.isPresent()) {
            Vec3 var1 = var0.get();
            double var2 = param1 - var1.x();
            double var3 = param2 - var1.y();
            double var4 = param3 - var1.z();
            this.rotO = this.rot = (float)Mth.atan2(var2, var4);
            this.pitchO = this.pitch = (float)Mth.atan2(var3, Math.sqrt(var2 * var2 + var4 * var4));
        }

    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        float var0 = Mth.sin(((float)this.age + param2 - (float) (Math.PI * 2)) * 0.05F) * 2.0F;
        float var1 = Mth.lerp(param2, this.rotO, this.rot);
        float var2 = Mth.lerp(param2, this.pitchO, this.pitch) + (float) (Math.PI / 2);
        this.renderSignal(param0, param1, param2, param3 -> param3.rotateY(var1).rotateX(-var2).rotateY(var0));
        this.renderSignal(param0, param1, param2, param3 -> param3.rotateY((float) -Math.PI + var1).rotateX(var2).rotateY(var0));
    }

    private void renderSignal(VertexConsumer param0, Camera param1, float param2, Consumer<Quaternionf> param3) {
        Vec3 var0 = param1.getPosition();
        float var1 = (float)(Mth.lerp((double)param2, this.xo, this.x) - var0.x());
        float var2 = (float)(Mth.lerp((double)param2, this.yo, this.y) - var0.y());
        float var3 = (float)(Mth.lerp((double)param2, this.zo, this.z) - var0.z());
        Vector3f var4 = new Vector3f(0.5F, 0.5F, 0.5F).normalize();
        Quaternionf var5 = new Quaternionf().setAngleAxis(0.0F, var4.x(), var4.y(), var4.z());
        param3.accept(var5);
        Vector3f[] var6 = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float var7 = this.getQuadSize(param2);

        for(int var8 = 0; var8 < 4; ++var8) {
            Vector3f var9 = var6[var8];
            var9.rotate(var5);
            var9.mul(var7);
            var9.add(var1, var2, var3);
        }

        float var10 = this.getU0();
        float var11 = this.getU1();
        float var12 = this.getV0();
        float var13 = this.getV1();
        int var14 = this.getLightColor(param2);
        param0.vertex((double)var6[0].x(), (double)var6[0].y(), (double)var6[0].z())
            .uv(var11, var13)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var14)
            .endVertex();
        param0.vertex((double)var6[1].x(), (double)var6[1].y(), (double)var6[1].z())
            .uv(var11, var12)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var14)
            .endVertex();
        param0.vertex((double)var6[2].x(), (double)var6[2].y(), (double)var6[2].z())
            .uv(var10, var12)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var14)
            .endVertex();
        param0.vertex((double)var6[3].x(), (double)var6[3].y(), (double)var6[3].z())
            .uv(var10, var13)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var14)
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
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            Optional<Vec3> var0 = this.target.getPosition(this.level);
            if (var0.isEmpty()) {
                this.remove();
            } else {
                int var1 = this.lifetime - this.age;
                double var2 = 1.0 / (double)var1;
                Vec3 var3 = var0.get();
                this.x = Mth.lerp(var2, this.x, var3.x());
                this.y = Mth.lerp(var2, this.y, var3.y());
                this.z = Mth.lerp(var2, this.z, var3.z());
                double var4 = this.x - var3.x();
                double var5 = this.y - var3.y();
                double var6 = this.z - var3.z();
                this.rotO = this.rot;
                this.rot = (float)Mth.atan2(var4, var6);
                this.pitchO = this.pitch;
                this.pitch = (float)Mth.atan2(var5, Math.sqrt(var4 * var4 + var6 * var6));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<VibrationParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            VibrationParticleOption param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            VibrationSignalParticle var0 = new VibrationSignalParticle(param1, param2, param3, param4, param0.getDestination(), param0.getArrivalInTicks());
            var0.pickSprite(this.sprite);
            var0.setAlpha(1.0F);
            return var0;
        }
    }
}
