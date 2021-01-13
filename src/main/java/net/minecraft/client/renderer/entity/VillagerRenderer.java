package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerRenderer extends MobRenderer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public VillagerRenderer(EntityRenderDispatcher param0, ReloadableResourceManager param1) {
        super(param0, new VillagerModel<>(0.0F), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this));
        this.addLayer(new VillagerProfessionLayer<>(this, param1, "villager"));
        this.addLayer(new CrossedArmsItemLayer<>(this));
    }

    public ResourceLocation getTextureLocation(Villager param0) {
        return VILLAGER_BASE_SKIN;
    }

    protected void scale(Villager param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        if (param0.isBaby()) {
            var0 = (float)((double)var0 * 0.5);
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        param1.scale(var0, var0, var0);
    }
}
