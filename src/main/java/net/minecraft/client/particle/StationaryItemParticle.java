package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StationaryItemParticle extends TextureSheetParticle {
    private StationaryItemParticle(ClientLevel param0, double param1, double param2, double param3, ItemLike param4) {
        super(param0, param1, param2, param3);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getItemModelShaper().getParticleIcon(param4));
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
    public static class BarrierProvider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new StationaryItemParticle(param1, param2, param3, param4, Blocks.BARRIER.asItem());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LightProvider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new StationaryItemParticle(param1, param2, param3, param4, Items.LIGHT);
        }
    }
}
