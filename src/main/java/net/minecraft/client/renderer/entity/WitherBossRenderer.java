package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherBossRenderer extends MobRenderer<WitherBoss, WitherBossModel<WitherBoss>> {
    private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");

    public WitherBossRenderer(EntityRenderDispatcher param0) {
        super(param0, new WitherBossModel<>(0.0F), 1.0F);
        this.addLayer(new WitherArmorLayer(this));
    }

    protected ResourceLocation getTextureLocation(WitherBoss param0) {
        int var0 = param0.getInvulnerableTicks();
        return var0 > 0 && (var0 > 80 || var0 / 5 % 2 != 1) ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }

    protected void scale(WitherBoss param0, float param1) {
        float var0 = 2.0F;
        int var1 = param0.getInvulnerableTicks();
        if (var1 > 0) {
            var0 -= ((float)var1 - param1) / 220.0F * 0.5F;
        }

        GlStateManager.scalef(var0, var0, var0);
    }
}
