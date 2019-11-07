package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkParticles {
    @OnlyIn(Dist.CLIENT)
    public static class FlashProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public FlashProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            FireworkParticles.OverlayParticle var0 = new FireworkParticles.OverlayParticle(param1, param2, param3, param4);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class OverlayParticle extends TextureSheetParticle {
        private OverlayParticle(Level param0, double param1, double param2, double param3) {
            super(param0, param1, param2, param3);
            this.lifetime = 4;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        public void render(VertexConsumer param0, Camera param1, float param2) {
            this.setAlpha(0.6F - ((float)this.age + param2 - 1.0F) * 0.25F * 0.5F);
            super.render(param0, param1, param2);
        }

        @Override
        public float getQuadSize(float param0) {
            return 7.1F * Mth.sin(((float)this.age + param0 - 1.0F) * 0.25F * (float) Math.PI);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class SparkParticle extends SimpleAnimatedParticle {
        private boolean trail;
        private boolean flicker;
        private final ParticleEngine engine;
        private float fadeR;
        private float fadeG;
        private float fadeB;
        private boolean hasFade;

        private SparkParticle(
            Level param0, double param1, double param2, double param3, double param4, double param5, double param6, ParticleEngine param7, SpriteSet param8
        ) {
            super(param0, param1, param2, param3, param8, -0.004F);
            this.xd = param4;
            this.yd = param5;
            this.zd = param6;
            this.engine = param7;
            this.quadSize *= 0.75F;
            this.lifetime = 48 + this.random.nextInt(12);
            this.setSpriteFromAge(param8);
        }

        public void setTrail(boolean param0) {
            this.trail = param0;
        }

        public void setFlicker(boolean param0) {
            this.flicker = param0;
        }

        @Override
        public void render(VertexConsumer param0, Camera param1, float param2) {
            if (!this.flicker || this.age < this.lifetime / 3 || (this.age + this.lifetime) / 3 % 2 == 0) {
                super.render(param0, param1, param2);
            }

        }

        @Override
        public void tick() {
            super.tick();
            if (this.trail && this.age < this.lifetime / 2 && (this.age + this.lifetime) % 2 == 0) {
                FireworkParticles.SparkParticle var0 = new FireworkParticles.SparkParticle(
                    this.level, this.x, this.y, this.z, 0.0, 0.0, 0.0, this.engine, this.sprites
                );
                var0.setAlpha(0.99F);
                var0.setColor(this.rCol, this.gCol, this.bCol);
                var0.age = var0.lifetime / 2;
                if (this.hasFade) {
                    var0.hasFade = true;
                    var0.fadeR = this.fadeR;
                    var0.fadeG = this.fadeG;
                    var0.fadeB = this.fadeB;
                }

                var0.flicker = this.flicker;
                this.engine.add(var0);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SparkProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SparkProvider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            FireworkParticles.SparkParticle var0 = new FireworkParticles.SparkParticle(
                param1, param2, param3, param4, param5, param6, param7, Minecraft.getInstance().particleEngine, this.sprites
            );
            var0.setAlpha(0.99F);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Starter extends NoRenderParticle {
        private int life;
        private final ParticleEngine engine;
        private ListTag explosions;
        private boolean twinkleDelay;

        public Starter(
            Level param0,
            double param1,
            double param2,
            double param3,
            double param4,
            double param5,
            double param6,
            ParticleEngine param7,
            @Nullable CompoundTag param8
        ) {
            super(param0, param1, param2, param3);
            this.xd = param4;
            this.yd = param5;
            this.zd = param6;
            this.engine = param7;
            this.lifetime = 8;
            if (param8 != null) {
                this.explosions = param8.getList("Explosions", 10);
                if (this.explosions.isEmpty()) {
                    this.explosions = null;
                } else {
                    this.lifetime = this.explosions.size() * 2 - 1;

                    for(int var0 = 0; var0 < this.explosions.size(); ++var0) {
                        CompoundTag var1 = this.explosions.getCompound(var0);
                        if (var1.getBoolean("Flicker")) {
                            this.twinkleDelay = true;
                            this.lifetime += 15;
                            break;
                        }
                    }
                }
            }

        }

        @Override
        public void tick() {
            if (this.life == 0 && this.explosions != null) {
                boolean var0 = this.isFarAwayFromCamera();
                boolean var1 = false;
                if (this.explosions.size() >= 3) {
                    var1 = true;
                } else {
                    for(int var2 = 0; var2 < this.explosions.size(); ++var2) {
                        CompoundTag var3 = this.explosions.getCompound(var2);
                        if (FireworkRocketItem.Shape.byId(var3.getByte("Type")) == FireworkRocketItem.Shape.LARGE_BALL) {
                            var1 = true;
                            break;
                        }
                    }
                }

                SoundEvent var4;
                if (var1) {
                    var4 = var0 ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST;
                } else {
                    var4 = var0 ? SoundEvents.FIREWORK_ROCKET_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_BLAST;
                }

                this.level.playLocalSound(this.x, this.y, this.z, var4, SoundSource.AMBIENT, 20.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
            }

            if (this.life % 2 == 0 && this.explosions != null && this.life / 2 < this.explosions.size()) {
                int var6 = this.life / 2;
                CompoundTag var7 = this.explosions.getCompound(var6);
                FireworkRocketItem.Shape var8 = FireworkRocketItem.Shape.byId(var7.getByte("Type"));
                boolean var9 = var7.getBoolean("Trail");
                boolean var10 = var7.getBoolean("Flicker");
                int[] var11 = var7.getIntArray("Colors");
                int[] var12 = var7.getIntArray("FadeColors");
                if (var11.length == 0) {
                    var11 = new int[]{DyeColor.BLACK.getFireworkColor()};
                }

                switch(var8) {
                    case SMALL_BALL:
                    default:
                        this.createParticleBall(0.25, 2, var11, var12, var9, var10);
                        break;
                    case LARGE_BALL:
                        this.createParticleBall(0.5, 4, var11, var12, var9, var10);
                        break;
                    case STAR:
                        this.createParticleShape(
                            0.5,
                            new double[][]{
                                {0.0, 1.0},
                                {0.3455, 0.309},
                                {0.9511, 0.309},
                                {0.3795918367346939, -0.12653061224489795},
                                {0.6122448979591837, -0.8040816326530612},
                                {0.0, -0.35918367346938773}
                            },
                            var11,
                            var12,
                            var9,
                            var10,
                            false
                        );
                        break;
                    case CREEPER:
                        this.createParticleShape(
                            0.5,
                            new double[][]{
                                {0.0, 0.2},
                                {0.2, 0.2},
                                {0.2, 0.6},
                                {0.6, 0.6},
                                {0.6, 0.2},
                                {0.2, 0.2},
                                {0.2, 0.0},
                                {0.4, 0.0},
                                {0.4, -0.6},
                                {0.2, -0.6},
                                {0.2, -0.4},
                                {0.0, -0.4}
                            },
                            var11,
                            var12,
                            var9,
                            var10,
                            true
                        );
                        break;
                    case BURST:
                        this.createParticleBurst(var11, var12, var9, var10);
                }

                int var13 = var11[0];
                float var14 = (float)((var13 & 0xFF0000) >> 16) / 255.0F;
                float var15 = (float)((var13 & 0xFF00) >> 8) / 255.0F;
                float var16 = (float)((var13 & 0xFF) >> 0) / 255.0F;
                Particle var17 = this.engine.createParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                var17.setColor(var14, var15, var16);
            }

            ++this.life;
            if (this.life > this.lifetime) {
                if (this.twinkleDelay) {
                    boolean var18 = this.isFarAwayFromCamera();
                    SoundEvent var19 = var18 ? SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.FIREWORK_ROCKET_TWINKLE;
                    this.level.playLocalSound(this.x, this.y, this.z, var19, SoundSource.AMBIENT, 20.0F, 0.9F + this.random.nextFloat() * 0.15F, true);
                }

                this.remove();
            }

        }

        private boolean isFarAwayFromCamera() {
            Minecraft var0 = Minecraft.getInstance();
            return var0.gameRenderer.getMainCamera().getPosition().distanceToSqr(this.x, this.y, this.z) >= 256.0;
        }

        private void createParticle(
            double param0,
            double param1,
            double param2,
            double param3,
            double param4,
            double param5,
            int[] param6,
            int[] param7,
            boolean param8,
            boolean param9
        ) {
            FireworkParticles.SparkParticle var0 = (FireworkParticles.SparkParticle)this.engine
                .createParticle(ParticleTypes.FIREWORK, param0, param1, param2, param3, param4, param5);
            var0.setTrail(param8);
            var0.setFlicker(param9);
            var0.setAlpha(0.99F);
            int var1 = this.random.nextInt(param6.length);
            var0.setColor(param6[var1]);
            if (param7.length > 0) {
                var0.setFadeColor(param7[this.random.nextInt(param7.length)]);
            }

        }

        private void createParticleBall(double param0, int param1, int[] param2, int[] param3, boolean param4, boolean param5) {
            double var0 = this.x;
            double var1 = this.y;
            double var2 = this.z;

            for(int var3 = -param1; var3 <= param1; ++var3) {
                for(int var4 = -param1; var4 <= param1; ++var4) {
                    for(int var5 = -param1; var5 <= param1; ++var5) {
                        double var6 = (double)var4 + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double var7 = (double)var3 + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double var8 = (double)var5 + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double var9 = (double)Mth.sqrt(var6 * var6 + var7 * var7 + var8 * var8) / param0 + this.random.nextGaussian() * 0.05;
                        this.createParticle(var0, var1, var2, var6 / var9, var7 / var9, var8 / var9, param2, param3, param4, param5);
                        if (var3 != -param1 && var3 != param1 && var4 != -param1 && var4 != param1) {
                            var5 += param1 * 2 - 1;
                        }
                    }
                }
            }

        }

        private void createParticleShape(double param0, double[][] param1, int[] param2, int[] param3, boolean param4, boolean param5, boolean param6) {
            double var0 = param1[0][0];
            double var1 = param1[0][1];
            this.createParticle(this.x, this.y, this.z, var0 * param0, var1 * param0, 0.0, param2, param3, param4, param5);
            float var2 = this.random.nextFloat() * (float) Math.PI;
            double var3 = param6 ? 0.034 : 0.34;

            for(int var4 = 0; var4 < 3; ++var4) {
                double var5 = (double)var2 + (double)((float)var4 * (float) Math.PI) * var3;
                double var6 = var0;
                double var7 = var1;

                for(int var8 = 1; var8 < param1.length; ++var8) {
                    double var9 = param1[var8][0];
                    double var10 = param1[var8][1];

                    for(double var11 = 0.25; var11 <= 1.0; var11 += 0.25) {
                        double var12 = Mth.lerp(var11, var6, var9) * param0;
                        double var13 = Mth.lerp(var11, var7, var10) * param0;
                        double var14 = var12 * Math.sin(var5);
                        var12 *= Math.cos(var5);

                        for(double var15 = -1.0; var15 <= 1.0; var15 += 2.0) {
                            this.createParticle(this.x, this.y, this.z, var12 * var15, var13, var14 * var15, param2, param3, param4, param5);
                        }
                    }

                    var6 = var9;
                    var7 = var10;
                }
            }

        }

        private void createParticleBurst(int[] param0, int[] param1, boolean param2, boolean param3) {
            double var0 = this.random.nextGaussian() * 0.05;
            double var1 = this.random.nextGaussian() * 0.05;

            for(int var2 = 0; var2 < 70; ++var2) {
                double var3 = this.xd * 0.5 + this.random.nextGaussian() * 0.15 + var0;
                double var4 = this.zd * 0.5 + this.random.nextGaussian() * 0.15 + var1;
                double var5 = this.yd * 0.5 + this.random.nextDouble() * 0.5;
                this.createParticle(this.x, this.y, this.z, var3, var5, var4, param0, param1, param2, param3);
            }

        }
    }
}
