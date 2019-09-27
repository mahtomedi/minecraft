package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SingleQuadParticle extends Particle {
    protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

    protected SingleQuadParticle(Level param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3);
    }

    protected SingleQuadParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        float var0 = this.getQuadSize(param2);
        float var1 = this.getU0();
        float var2 = this.getU1();
        float var3 = this.getV0();
        float var4 = this.getV1();
        float var5 = (float)(Mth.lerp((double)param2, this.xo, this.x) - xOff);
        float var6 = (float)(Mth.lerp((double)param2, this.yo, this.y) - yOff);
        float var7 = (float)(Mth.lerp((double)param2, this.zo, this.z) - zOff);
        int var8 = this.getLightColor(param2);
        Vec3[] var9 = new Vec3[]{
            new Vec3((double)(-param3 * var0 - param6 * var0), (double)(-param4 * var0), (double)(-param5 * var0 - param7 * var0)),
            new Vec3((double)(-param3 * var0 + param6 * var0), (double)(param4 * var0), (double)(-param5 * var0 + param7 * var0)),
            new Vec3((double)(param3 * var0 + param6 * var0), (double)(param4 * var0), (double)(param5 * var0 + param7 * var0)),
            new Vec3((double)(param3 * var0 - param6 * var0), (double)(-param4 * var0), (double)(param5 * var0 - param7 * var0))
        };
        if (this.roll != 0.0F) {
            float var10 = Mth.lerp(param2, this.oRoll, this.roll);
            float var11 = Mth.cos(var10 * 0.5F);
            float var12 = (float)((double)Mth.sin(var10 * 0.5F) * param1.getLookVector().x);
            float var13 = (float)((double)Mth.sin(var10 * 0.5F) * param1.getLookVector().y);
            float var14 = (float)((double)Mth.sin(var10 * 0.5F) * param1.getLookVector().z);
            Vec3 var15 = new Vec3((double)var12, (double)var13, (double)var14);

            for(int var16 = 0; var16 < 4; ++var16) {
                var9[var16] = var15.scale(2.0 * var9[var16].dot(var15))
                    .add(var9[var16].scale((double)(var11 * var11) - var15.dot(var15)))
                    .add(var15.cross(var9[var16]).scale((double)(2.0F * var11)));
            }
        }

        param0.vertex((double)var5 + var9[0].x, (double)var6 + var9[0].y, (double)var7 + var9[0].z)
            .uv(var2, var4)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var8)
            .endVertex();
        param0.vertex((double)var5 + var9[1].x, (double)var6 + var9[1].y, (double)var7 + var9[1].z)
            .uv(var2, var3)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var8)
            .endVertex();
        param0.vertex((double)var5 + var9[2].x, (double)var6 + var9[2].y, (double)var7 + var9[2].z)
            .uv(var1, var3)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var8)
            .endVertex();
        param0.vertex((double)var5 + var9[3].x, (double)var6 + var9[3].y, (double)var7 + var9[3].z)
            .uv(var1, var4)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var8)
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
