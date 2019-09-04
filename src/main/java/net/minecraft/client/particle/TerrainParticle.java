package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TerrainParticle extends TextureSheetParticle {
    private final BlockState blockState;
    private BlockPos pos;
    private final float uo;
    private final float vo;

    public TerrainParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6, BlockState param7) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.blockState = param7;
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(param7));
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    public TerrainParticle init(BlockPos param0) {
        this.pos = param0;
        if (this.blockState.getBlock() == Blocks.GRASS_BLOCK) {
            return this;
        } else {
            this.multiplyColor(param0);
            return this;
        }
    }

    public TerrainParticle init() {
        this.pos = new BlockPos(this.x, this.y, this.z);
        Block var0 = this.blockState.getBlock();
        if (var0 == Blocks.GRASS_BLOCK) {
            return this;
        } else {
            this.multiplyColor(this.pos);
            return this;
        }
    }

    protected void multiplyColor(@Nullable BlockPos param0) {
        int var0 = Minecraft.getInstance().getBlockColors().getColor(this.blockState, this.level, param0, 0);
        this.rCol *= (float)(var0 >> 16 & 0xFF) / 255.0F;
        this.gCol *= (float)(var0 >> 8 & 0xFF) / 255.0F;
        this.bCol *= (float)(var0 & 0xFF) / 255.0F;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((double)((this.uo + 1.0F) / 4.0F * 16.0F));
    }

    @Override
    protected float getU1() {
        return this.sprite.getU((double)(this.uo / 4.0F * 16.0F));
    }

    @Override
    protected float getV0() {
        return this.sprite.getV((double)(this.vo / 4.0F * 16.0F));
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((double)((this.vo + 1.0F) / 4.0F * 16.0F));
    }

    @Override
    public int getLightColor(float param0) {
        int var0 = super.getLightColor(param0);
        int var1 = 0;
        if (this.level.hasChunkAt(this.pos)) {
            var1 = this.level.getLightColor(this.pos);
        }

        return var0 == 0 ? var1 : var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BlockParticleOption> {
        public Particle createParticle(
            BlockParticleOption param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            BlockState var0 = param0.getState();
            return !var0.isAir() && var0.getBlock() != Blocks.MOVING_PISTON
                ? new TerrainParticle(param1, param2, param3, param4, param5, param6, param7, var0).init()
                : null;
        }
    }
}
