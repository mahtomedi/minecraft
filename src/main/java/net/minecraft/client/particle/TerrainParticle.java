package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TerrainParticle extends TextureSheetParticle {
    private final BlockPos pos;
    private final float uo;
    private final float vo;

    public TerrainParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, BlockState param7) {
        this(param0, param1, param2, param3, param4, param5, param6, param7, BlockPos.containing(param1, param2, param3));
    }

    public TerrainParticle(
        ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, BlockState param7, BlockPos param8
    ) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.pos = param8;
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(param7));
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
        if (!param7.is(Blocks.GRASS_BLOCK)) {
            int var0 = Minecraft.getInstance().getBlockColors().getColor(param7, param0, param8, 0);
            this.rCol *= (float)(var0 >> 16 & 0xFF) / 255.0F;
            this.gCol *= (float)(var0 >> 8 & 0xFF) / 255.0F;
            this.bCol *= (float)(var0 & 0xFF) / 255.0F;
        }

        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F);
    }

    @Override
    public int getLightColor(float param0) {
        int var0 = super.getLightColor(param0);
        return var0 == 0 && this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.pos) : var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BlockParticleOption> {
        public Particle createParticle(
            BlockParticleOption param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            BlockState var0 = param0.getState();
            return !var0.isAir() && !var0.is(Blocks.MOVING_PISTON) && var0.shouldSpawnTerrainParticles()
                ? new TerrainParticle(param1, param2, param3, param4, param5, param6, param7, var0)
                : null;
        }
    }
}
