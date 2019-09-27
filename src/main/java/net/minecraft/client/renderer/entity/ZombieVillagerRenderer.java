package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieVillagerRenderer extends HumanoidMobRenderer<ZombieVillager, ZombieVillagerModel<ZombieVillager>> {
    private static final ResourceLocation ZOMBIE_VILLAGER_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_villager.png");

    public ZombieVillagerRenderer(EntityRenderDispatcher param0, ReloadableResourceManager param1) {
        super(param0, new ZombieVillagerModel<>(), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new ZombieVillagerModel(0.5F, true), new ZombieVillagerModel(1.0F, true)));
        this.addLayer(new VillagerProfessionLayer<>(this, param1, "zombie_villager"));
    }

    public ResourceLocation getTextureLocation(ZombieVillager param0) {
        return ZOMBIE_VILLAGER_LOCATION;
    }

    protected void setupRotations(ZombieVillager param0, PoseStack param1, float param2, float param3, float param4) {
        if (param0.isConverting()) {
            param3 += (float)(Math.cos((double)param0.tickCount * 3.25) * Math.PI * 0.25);
        }

        super.setupRotations(param0, param1, param2, param3, param4);
    }
}
