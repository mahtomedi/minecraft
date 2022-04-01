package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FootprintParticle extends Particle {
    private final TextureAtlasSprite sprite;
    private final float rot;

    protected FootprintParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.rot = (float)param4;
        this.lifetime = 200;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.sprite = param7.get(this.random);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        float var0 = ((float)this.age + param2) / (float)this.lifetime;
        var0 *= var0;
        float var1 = 2.0F - var0 * 2.0F;
        var1 *= 0.2F;
        float var2 = 0.125F;
        Vec3 var3 = param1.getPosition();
        float var4 = (float)(this.x - var3.x);
        float var5 = (float)(this.y - var3.y);
        float var6 = (float)(this.z - var3.z);
        int var7 = this.getLightColor(param2);
        float var8 = this.sprite.getU0();
        float var9 = this.sprite.getU1();
        float var10 = this.sprite.getV0();
        float var11 = this.sprite.getV1();
        Quaternion var12 = Vector3f.YP.rotationDegrees(this.rot);
        Matrix4f var13 = Matrix4f.createTranslateMatrix(var4, var5, var6);
        var13.multiply(var12);
        param0.vertex(var13, -0.125F, 0.0F, 0.125F).uv(var8, var11).color(this.rCol, this.gCol, this.bCol, var1).uv2(var7).endVertex();
        param0.vertex(var13, 0.125F, 0.0F, 0.125F).uv(var9, var11).color(this.rCol, this.gCol, this.bCol, var1).uv2(var7).endVertex();
        param0.vertex(var13, 0.125F, 0.0F, -0.125F).uv(var9, var10).color(this.rCol, this.gCol, this.bCol, var1).uv2(var7).endVertex();
        param0.vertex(var13, -0.125F, 0.0F, -0.125F).uv(var8, var10).color(this.rCol, this.gCol, this.bCol, var1).uv2(var7).endVertex();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new FootprintParticle(param1, param2, param3, param4, param5, param6, param7, this.sprites);
        }
    }
}
