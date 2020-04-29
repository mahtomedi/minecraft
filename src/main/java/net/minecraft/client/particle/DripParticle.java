package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DripParticle extends TextureSheetParticle {
    private final Fluid type;
    protected boolean isGlowing;

    private DripParticle(ClientLevel param0, double param1, double param2, double param3, Fluid param4) {
        super(param0, param1, param2, param3);
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.type = param4;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float param0) {
        return this.isGlowing ? 240 : super.getLightColor(param0);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.preMoveUpdate();
        if (!this.removed) {
            this.yd -= (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.postMoveUpdate();
            if (!this.removed) {
                this.xd *= 0.98F;
                this.yd *= 0.98F;
                this.zd *= 0.98F;
                BlockPos var0 = new BlockPos(this.x, this.y, this.z);
                FluidState var1 = this.level.getFluidState(var0);
                if (var1.getType() == this.type && this.y < (double)((float)var0.getY() + var1.getHeight(this.level, var0))) {
                    this.remove();
                }

            }
        }
    }

    protected void preMoveUpdate() {
        if (this.lifetime-- <= 0) {
            this.remove();
        }

    }

    protected void postMoveUpdate() {
    }

    @OnlyIn(Dist.CLIENT)
    static class CoolingDripHangParticle extends DripParticle.DripHangParticle {
        private CoolingDripHangParticle(ClientLevel param0, double param1, double param2, double param3, Fluid param4, ParticleOptions param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        protected void preMoveUpdate() {
            this.rCol = 1.0F;
            this.gCol = 16.0F / (float)(40 - this.lifetime + 16);
            this.bCol = 4.0F / (float)(40 - this.lifetime + 8);
            super.preMoveUpdate();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class DripHangParticle extends DripParticle {
        private final ParticleOptions fallingParticle;

        private DripHangParticle(ClientLevel param0, double param1, double param2, double param3, Fluid param4, ParticleOptions param5) {
            super(param0, param1, param2, param3, param4);
            this.fallingParticle = param5;
            this.gravity *= 0.02F;
            this.lifetime = 40;
        }

        @Override
        protected void preMoveUpdate() {
            if (this.lifetime-- <= 0) {
                this.remove();
                this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
            }

        }

        @Override
        protected void postMoveUpdate() {
            this.xd *= 0.02;
            this.yd *= 0.02;
            this.zd *= 0.02;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class DripLandParticle extends DripParticle {
        private DripLandParticle(ClientLevel param0, double param1, double param2, double param3, Fluid param4) {
            super(param0, param1, param2, param3, param4);
            this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class FallAndLandParticle extends DripParticle.FallingParticle {
        protected final ParticleOptions landParticle;

        private FallAndLandParticle(ClientLevel param0, double param1, double param2, double param3, Fluid param4, ParticleOptions param5) {
            super(param0, param1, param2, param3, param4);
            this.landParticle = param5;
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class FallingParticle extends DripParticle {
        private FallingParticle(ClientLevel param0, double param1, double param2, double param3, Fluid param4) {
            super(param0, param1, param2, param3, param4);
            this.lifetime = (int)(64.0 / (Math.random() * 0.8 + 0.2));
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static class HoneyFallAndLandParticle extends DripParticle.FallAndLandParticle {
        private HoneyFallAndLandParticle(ClientLevel param0, double param1, double param2, double param3, Fluid param4, ParticleOptions param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                this.level
                    .playLocalSound(
                        this.x + 0.5,
                        this.y,
                        this.z + 0.5,
                        SoundEvents.BEEHIVE_DRIP,
                        SoundSource.BLOCKS,
                        0.3F + this.level.random.nextFloat() * 2.0F / 3.0F,
                        1.0F,
                        false
                    );
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class HoneyFallProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public HoneyFallProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.HoneyFallAndLandParticle(param1, param2, param3, param4, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
            var0.gravity = 0.01F;
            var0.setColor(0.582F, 0.448F, 0.082F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class HoneyHangProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public HoneyHangProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle.DripHangParticle var0 = new DripParticle.DripHangParticle(param1, param2, param3, param4, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
            var0.gravity *= 0.01F;
            var0.lifetime = 100;
            var0.setColor(0.622F, 0.508F, 0.082F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class HoneyLandProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public HoneyLandProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.DripLandParticle(param1, param2, param3, param4, Fluids.EMPTY);
            var0.lifetime = (int)(128.0 / (Math.random() * 0.8 + 0.2));
            var0.setColor(0.522F, 0.408F, 0.082F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LavaFallProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public LavaFallProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.FallAndLandParticle(param1, param2, param3, param4, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
            var0.setColor(1.0F, 0.2857143F, 0.083333336F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LavaHangProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public LavaHangProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle.CoolingDripHangParticle var0 = new DripParticle.CoolingDripHangParticle(
                param1, param2, param3, param4, Fluids.LAVA, ParticleTypes.FALLING_LAVA
            );
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LavaLandProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public LavaLandProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.DripLandParticle(param1, param2, param3, param4, Fluids.LAVA);
            var0.setColor(1.0F, 0.2857143F, 0.083333336F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NectarFallProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public NectarFallProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.FallingParticle(param1, param2, param3, param4, Fluids.EMPTY);
            var0.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
            var0.gravity = 0.007F;
            var0.setColor(0.92F, 0.782F, 0.72F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ObsidianTearFallProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public ObsidianTearFallProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.FallAndLandParticle(param1, param2, param3, param4, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
            var0.isGlowing = true;
            var0.gravity = 0.01F;
            var0.setColor(0.51171875F, 0.03125F, 0.890625F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ObsidianTearHangProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public ObsidianTearHangProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle.DripHangParticle var0 = new DripParticle.DripHangParticle(
                param1, param2, param3, param4, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR
            );
            var0.isGlowing = true;
            var0.gravity *= 0.01F;
            var0.lifetime = 100;
            var0.setColor(0.51171875F, 0.03125F, 0.890625F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ObsidianTearLandProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public ObsidianTearLandProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.DripLandParticle(param1, param2, param3, param4, Fluids.EMPTY);
            var0.isGlowing = true;
            var0.lifetime = (int)(28.0 / (Math.random() * 0.8 + 0.2));
            var0.setColor(0.51171875F, 0.03125F, 0.890625F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WaterFallProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public WaterFallProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.FallAndLandParticle(param1, param2, param3, param4, Fluids.WATER, ParticleTypes.SPLASH);
            var0.setColor(0.2F, 0.3F, 1.0F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WaterHangProvider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public WaterHangProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            DripParticle var0 = new DripParticle.DripHangParticle(param1, param2, param3, param4, Fluids.WATER, ParticleTypes.FALLING_WATER);
            var0.setColor(0.2F, 0.3F, 1.0F);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
