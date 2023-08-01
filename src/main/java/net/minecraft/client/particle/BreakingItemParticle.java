package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreakingItemParticle extends TextureSheetParticle {
    private final float uo;
    private final float vo;

    BreakingItemParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, ItemStack param7) {
        this(param0, param1, param2, param3, param7);
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.xd += param4;
        this.yd += param5;
        this.zd += param6;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected BreakingItemParticle(ClientLevel param0, double param1, double param2, double param3, ItemStack param4) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(param4, param0, null, 0).getParticleIcon());
        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
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

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ItemParticleOption> {
        public Particle createParticle(
            ItemParticleOption param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new BreakingItemParticle(param1, param2, param3, param4, param5, param6, param7, param0.getItem());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SlimeProvider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new BreakingItemParticle(param1, param2, param3, param4, new ItemStack(Items.SLIME_BALL));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SnowballProvider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new BreakingItemParticle(param1, param2, param3, param4, new ItemStack(Items.SNOWBALL));
        }
    }
}
