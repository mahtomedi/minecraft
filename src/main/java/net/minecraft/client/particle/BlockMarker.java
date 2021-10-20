package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockMarker extends TextureSheetParticle {
    BlockMarker(ClientLevel param0, double param1, double param2, double param3, BlockState param4) {
        super(param0, param1, param2, param3);
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(param4));
        this.gravity = 0.0F;
        this.lifetime = 80;
        this.hasPhysics = false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public float getQuadSize(float param0) {
        return 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BlockParticleOption> {
        public Particle createParticle(
            BlockParticleOption param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new BlockMarker(param1, param2, param3, param4, param0.getState());
        }
    }
}
