package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobAppearanceParticle extends Particle {
    private final Model model = new GuardianModel();
    private final RenderType renderType = RenderType.entityTranslucent(ElderGuardianRenderer.GUARDIAN_ELDER_LOCATION);

    private MobAppearanceParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3);
        this.gravity = 0.0F;
        this.lifetime = 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        float var0 = ((float)this.age + param2) / (float)this.lifetime;
        float var1 = 0.05F + 0.5F * Mth.sin(var0 * (float) Math.PI);
        PoseStack var2 = new PoseStack();
        var2.mulPose(param1.rotation());
        var2.mulPose(Vector3f.XP.rotationDegrees(150.0F * var0 - 60.0F));
        var2.scale(-1.0F, -1.0F, 1.0F);
        var2.translate(0.0, -1.101F, 1.5);
        MultiBufferSource.BufferSource var3 = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer var4 = var3.getBuffer(this.renderType);
        this.model.renderToBuffer(var2, var4, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, var1);
        var3.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new MobAppearanceParticle(param1, param2, param3, param4);
        }
    }
}
