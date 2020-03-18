package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingDustParticle extends TextureSheetParticle {
    private final float rotSpeed;
    private final SpriteSet sprites;

    private FallingDustParticle(Level param0, double param1, double param2, double param3, float param4, float param5, float param6, SpriteSet param7) {
        super(param0, param1, param2, param3);
        this.sprites = param7;
        this.rCol = param4;
        this.gCol = param5;
        this.bCol = param6;
        float var0 = 0.9F;
        this.quadSize *= 0.67499995F;
        int var1 = (int)(32.0 / (Math.random() * 0.8 + 0.2));
        this.lifetime = (int)Math.max((float)var1 * 0.9F, 1.0F);
        this.setSpriteFromAge(param7);
        this.rotSpeed = ((float)Math.random() - 0.5F) * 0.1F;
        this.roll = (float)Math.random() * (float) (Math.PI * 2);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float param0) {
        return this.quadSize * Mth.clamp(((float)this.age + param0) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
            this.oRoll = this.roll;
            this.roll += (float) Math.PI * this.rotSpeed * 2.0F;
            if (this.onGround) {
                this.oRoll = this.roll = 0.0F;
            }

            this.move(this.xd, this.yd, this.zd);
            this.yd -= 0.003F;
            this.yd = Math.max(this.yd, -0.14F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BlockParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet param0) {
            this.sprite = param0;
        }

        @Nullable
        public Particle createParticle(
            BlockParticleOption param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            BlockState var0 = param0.getState();
            if (!var0.isAir() && var0.getRenderShape() == RenderShape.INVISIBLE) {
                return null;
            } else {
                BlockPos var1 = new BlockPos(param2, param3, param4);
                int var2 = Minecraft.getInstance().getBlockColors().getColor(var0, param1, var1);
                if (var0.getBlock() instanceof FallingBlock) {
                    var2 = ((FallingBlock)var0.getBlock()).getDustColor(var0, param1, var1);
                }

                float var3 = (float)(var2 >> 16 & 0xFF) / 255.0F;
                float var4 = (float)(var2 >> 8 & 0xFF) / 255.0F;
                float var5 = (float)(var2 & 0xFF) / 255.0F;
                return new FallingDustParticle(param1, param2, param3, param4, var3, var4, var5, this.sprite);
            }
        }
    }
}
