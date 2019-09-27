package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerTradeItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WanderingTraderRenderer extends MobRenderer<WanderingTrader, VillagerModel<WanderingTrader>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/wandering_trader.png");

    public WanderingTraderRenderer(EntityRenderDispatcher param0) {
        super(param0, new VillagerModel<>(0.0F), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this));
        this.addLayer(new VillagerTradeItemLayer<>(this));
    }

    public ResourceLocation getTextureLocation(WanderingTrader param0) {
        return VILLAGER_BASE_SKIN;
    }

    protected void scale(WanderingTrader param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        param1.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
