package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoRenderParticle extends Particle {
    protected NoRenderParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3);
    }

    protected NoRenderParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public final void render(VertexConsumer param0, Camera param1, float param2) {
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
    }
}
