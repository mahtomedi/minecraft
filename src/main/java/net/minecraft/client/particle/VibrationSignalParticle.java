package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VibrationSignalParticle extends TextureSheetParticle {
    private final VibrationPath vibrationPath;
    private float yRot;
    private float yRotO;

    private VibrationSignalParticle(ClientLevel param0, VibrationPath param1, int param2) {
        super(
            param0,
            (double)((float)param1.getOrigin().getX() + 0.5F),
            (double)((float)param1.getOrigin().getY() + 0.5F),
            (double)((float)param1.getOrigin().getZ() + 0.5F),
            0.0,
            0.0,
            0.0
        );
        this.quadSize = 0.3F;
        this.vibrationPath = param1;
        this.lifetime = param2;
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        float var0 = Mth.sin(((float)this.age + param2 - (float) (Math.PI * 2)) * 0.05F) * 2.0F;
        float var1 = Mth.lerp(param2, this.yRotO, this.yRot);
        float var2 = 1.0472F;
        this.renderSignal(param0, param1, param2, param2x -> {
            param2x.mul(Vector3f.YP.rotation(var1));
            param2x.mul(Vector3f.XP.rotation(-1.0472F));
            param2x.mul(Vector3f.YP.rotation(var0));
        });
        this.renderSignal(param0, param1, param2, param2x -> {
            param2x.mul(Vector3f.YP.rotation((float) -Math.PI + var1));
            param2x.mul(Vector3f.XP.rotation(1.0472F));
            param2x.mul(Vector3f.YP.rotation(var0));
        });
    }

    private void renderSignal(VertexConsumer param0, Camera param1, float param2, Consumer<Quaternion> param3) {
        Vec3 var0 = param1.getPosition();
        float var1 = (float)(Mth.lerp((double)param2, this.xo, this.x) - var0.x());
        float var2 = (float)(Mth.lerp((double)param2, this.yo, this.y) - var0.y());
        float var3 = (float)(Mth.lerp((double)param2, this.zo, this.z) - var0.z());
        Vector3f var4 = new Vector3f(0.5F, 0.5F, 0.5F);
        var4.normalize();
        Quaternion var5 = new Quaternion(var4, 0.0F, true);
        param3.accept(var5);
        Vector3f var6 = new Vector3f(-1.0F, -1.0F, 0.0F);
        var6.transform(var5);
        Vector3f[] var7 = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float var8 = this.getQuadSize(param2);

        for(int var9 = 0; var9 < 4; ++var9) {
            Vector3f var10 = var7[var9];
            var10.transform(var5);
            var10.mul(var8);
            var10.add(var1, var2, var3);
        }

        float var11 = this.getU0();
        float var12 = this.getU1();
        float var13 = this.getV0();
        float var14 = this.getV1();
        int var15 = this.getLightColor(param2);
        param0.vertex((double)var7[0].x(), (double)var7[0].y(), (double)var7[0].z())
            .uv(var12, var14)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var15)
            .endVertex();
        param0.vertex((double)var7[1].x(), (double)var7[1].y(), (double)var7[1].z())
            .uv(var12, var13)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var15)
            .endVertex();
        param0.vertex((double)var7[2].x(), (double)var7[2].y(), (double)var7[2].z())
            .uv(var11, var13)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var15)
            .endVertex();
        param0.vertex((double)var7[3].x(), (double)var7[3].y(), (double)var7[3].z())
            .uv(var11, var14)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var15)
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
        super.tick();
        Optional<BlockPos> var0 = this.vibrationPath.getDestination().getPosition(this.level);
        if (!var0.isPresent()) {
            this.remove();
        } else {
            double var1 = (double)this.age / (double)this.lifetime;
            BlockPos var2 = this.vibrationPath.getOrigin();
            BlockPos var3 = var0.get();
            this.x = Mth.lerp(var1, (double)var2.getX() + 0.5, (double)var3.getX() + 0.5);
            this.y = Mth.lerp(var1, (double)var2.getY() + 0.5, (double)var3.getY() + 0.5);
            this.z = Mth.lerp(var1, (double)var2.getZ() + 0.5, (double)var3.getZ() + 0.5);
            this.yRotO = this.yRot;
            this.yRot = (float)Mth.atan2(this.x - (double)var3.getX(), this.z - (double)var3.getZ());
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
            VibrationSignalParticle var0 = new VibrationSignalParticle(param1, param0.getVibrationPath(), param0.getVibrationPath().getArrivalInTicks());
            var0.pickSprite(this.sprite);
            var0.setAlpha(1.0F);
            return var0;
        }
    }
}
