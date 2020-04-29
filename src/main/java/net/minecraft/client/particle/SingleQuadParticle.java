package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SingleQuadParticle extends Particle {
    protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

    protected SingleQuadParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3);
    }

    protected SingleQuadParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        Vec3 var0 = param1.getPosition();
        float var1 = (float)(Mth.lerp((double)param2, this.xo, this.x) - var0.x());
        float var2 = (float)(Mth.lerp((double)param2, this.yo, this.y) - var0.y());
        float var3 = (float)(Mth.lerp((double)param2, this.zo, this.z) - var0.z());
        Quaternion var4;
        if (this.roll == 0.0F) {
            var4 = param1.rotation();
        } else {
            var4 = new Quaternion(param1.rotation());
            float var6 = Mth.lerp(param2, this.oRoll, this.roll);
            var4.mul(Vector3f.ZP.rotation(var6));
        }

        Vector3f var7 = new Vector3f(-1.0F, -1.0F, 0.0F);
        var7.transform(var4);
        Vector3f[] var8 = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float var9 = this.getQuadSize(param2);

        for(int var10 = 0; var10 < 4; ++var10) {
            Vector3f var11 = var8[var10];
            var11.transform(var4);
            var11.mul(var9);
            var11.add(var1, var2, var3);
        }

        float var12 = this.getU0();
        float var13 = this.getU1();
        float var14 = this.getV0();
        float var15 = this.getV1();
        int var16 = this.getLightColor(param2);
        param0.vertex((double)var8[0].x(), (double)var8[0].y(), (double)var8[0].z())
            .uv(var13, var15)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var16)
            .endVertex();
        param0.vertex((double)var8[1].x(), (double)var8[1].y(), (double)var8[1].z())
            .uv(var13, var14)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var16)
            .endVertex();
        param0.vertex((double)var8[2].x(), (double)var8[2].y(), (double)var8[2].z())
            .uv(var12, var14)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var16)
            .endVertex();
        param0.vertex((double)var8[3].x(), (double)var8[3].y(), (double)var8[3].z())
            .uv(var12, var15)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var16)
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
}
