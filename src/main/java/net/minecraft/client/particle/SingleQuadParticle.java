package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
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
    public void render(BufferBuilder param0, Camera param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        float var0 = this.getQuadSize(param2);
        float var1 = this.getU0();
        float var2 = this.getU1();
        float var3 = this.getV0();
        float var4 = this.getV1();
        float var5 = (float)(Mth.lerp((double)param2, this.xo, this.x) - xOff);
        float var6 = (float)(Mth.lerp((double)param2, this.yo, this.y) - yOff);
        float var7 = (float)(Mth.lerp((double)param2, this.zo, this.z) - zOff);
        int var8 = this.getLightColor(param2);
        int var9 = var8 >> 16 & 65535;
        int var10 = var8 & 65535;
        Vec3[] var11 = new Vec3[]{
            new Vec3((double)(-param3 * var0 - param6 * var0), (double)(-param4 * var0), (double)(-param5 * var0 - param7 * var0)),
            new Vec3((double)(-param3 * var0 + param6 * var0), (double)(param4 * var0), (double)(-param5 * var0 + param7 * var0)),
            new Vec3((double)(param3 * var0 + param6 * var0), (double)(param4 * var0), (double)(param5 * var0 + param7 * var0)),
            new Vec3((double)(param3 * var0 - param6 * var0), (double)(-param4 * var0), (double)(param5 * var0 - param7 * var0))
        };
        if (this.roll != 0.0F) {
            float var12 = Mth.lerp(param2, this.oRoll, this.roll);
            float var13 = Mth.cos(var12 * 0.5F);
            float var14 = (float)((double)Mth.sin(var12 * 0.5F) * param1.getLookVector().x);
            float var15 = (float)((double)Mth.sin(var12 * 0.5F) * param1.getLookVector().y);
            float var16 = (float)((double)Mth.sin(var12 * 0.5F) * param1.getLookVector().z);
            Vec3 var17 = new Vec3((double)var14, (double)var15, (double)var16);

            for(int var18 = 0; var18 < 4; ++var18) {
                var11[var18] = var17.scale(2.0 * var11[var18].dot(var17))
                    .add(var11[var18].scale((double)(var13 * var13) - var17.dot(var17)))
                    .add(var17.cross(var11[var18]).scale((double)(2.0F * var13)));
            }
        }

        param0.vertex((double)var5 + var11[0].x, (double)var6 + var11[0].y, (double)var7 + var11[0].z)
            .uv((double)var2, (double)var4)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var9, var10)
            .endVertex();
        param0.vertex((double)var5 + var11[1].x, (double)var6 + var11[1].y, (double)var7 + var11[1].z)
            .uv((double)var2, (double)var3)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var9, var10)
            .endVertex();
        param0.vertex((double)var5 + var11[2].x, (double)var6 + var11[2].y, (double)var7 + var11[2].z)
            .uv((double)var1, (double)var3)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var9, var10)
            .endVertex();
        param0.vertex((double)var5 + var11[3].x, (double)var6 + var11[3].y, (double)var7 + var11[3].z)
            .uv((double)var1, (double)var4)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(var9, var10)
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
