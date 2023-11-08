package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class SingleQuadParticle extends Particle {
    protected float quadSize;
    private final Quaternionf rotation = new Quaternionf();

    protected SingleQuadParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3);
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    protected SingleQuadParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ;
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        Vec3 var0 = param1.getPosition();
        float var1 = (float)(Mth.lerp((double)param2, this.xo, this.x) - var0.x());
        float var2 = (float)(Mth.lerp((double)param2, this.yo, this.y) - var0.y());
        float var3 = (float)(Mth.lerp((double)param2, this.zo, this.z) - var0.z());
        this.getFacingCameraMode().setRotation(this.rotation, param1, param2);
        if (this.roll != 0.0F) {
            this.rotation.rotateZ(Mth.lerp(param2, this.oRoll, this.roll));
        }

        Vector3f[] var4 = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float var5 = this.getQuadSize(param2);

        for(int var6 = 0; var6 < 4; ++var6) {
            Vector3f var7 = var4[var6];
            var7.rotate(this.rotation);
            var7.mul(var5);
            var7.add(var1, var2, var3);
        }

        float var8 = this.getU0();
        float var9 = this.getU1();
        float var10 = this.getV0();
        float var11 = this.getV1();
        int var12 = this.getLightColor(param2);
        param0.vertex((double)var4[0].x(), (double)var4[0].y(), (double)var4[0].z())
            .uv(var9, var11)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var12)
            .endVertex();
        param0.vertex((double)var4[1].x(), (double)var4[1].y(), (double)var4[1].z())
            .uv(var9, var10)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var12)
            .endVertex();
        param0.vertex((double)var4[2].x(), (double)var4[2].y(), (double)var4[2].z())
            .uv(var8, var10)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var12)
            .endVertex();
        param0.vertex((double)var4[3].x(), (double)var4[3].y(), (double)var4[3].z())
            .uv(var8, var11)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var12)
            .endVertex();
    }

    public float getQuadSize(float param0) {
        return this.quadSize;
    }

    @Override
    public Particle scale(float param0) {
        this.quadSize *= param0;
        return super.scale(param0);
    }

    protected abstract float getU0();

    protected abstract float getU1();

    protected abstract float getV0();

    protected abstract float getV1();

    @OnlyIn(Dist.CLIENT)
    public interface FacingCameraMode {
        SingleQuadParticle.FacingCameraMode LOOKAT_XYZ = (param0, param1, param2) -> param0.set(param1.rotation());
        SingleQuadParticle.FacingCameraMode LOOKAT_Y = (param0, param1, param2) -> param0.set(0.0F, param1.rotation().y, 0.0F, param1.rotation().w);

        void setRotation(Quaternionf var1, Camera var2, float var3);
    }
}
