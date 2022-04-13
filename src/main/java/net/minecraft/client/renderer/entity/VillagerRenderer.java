package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerRenderer extends MobRenderer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public VillagerRenderer(EntityRendererProvider.Context param0) {
        super(param0, new VillagerModel<>(param0.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, param0.getModelSet(), param0.getItemInHandRenderer()));
        this.addLayer(new VillagerProfessionLayer<>(this, param0.getResourceManager(), "villager"));
        this.addLayer(new CrossedArmsItemLayer<>(this, param0.getItemInHandRenderer()));
    }

    public ResourceLocation getTextureLocation(Villager param0) {
        return VILLAGER_BASE_SKIN;
    }

    protected void scale(Villager param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        if (param0.isBaby()) {
            var0 *= 0.5F;
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        param1.scale(var0, var0, var0);
    }
}
