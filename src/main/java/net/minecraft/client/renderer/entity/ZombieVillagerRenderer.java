package net.minecraft.client.renderer.entity;

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
        super(param0, new ZombieVillagerModel<>(0.0F, false), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new ZombieVillagerModel(0.5F, true), new ZombieVillagerModel(1.0F, true)));
        this.addLayer(new VillagerProfessionLayer<>(this, param1, "zombie_villager"));
    }

    public ResourceLocation getTextureLocation(ZombieVillager param0) {
        return ZOMBIE_VILLAGER_LOCATION;
    }

    protected boolean isShaking(ZombieVillager param0) {
        return param0.isConverting();
    }
}
