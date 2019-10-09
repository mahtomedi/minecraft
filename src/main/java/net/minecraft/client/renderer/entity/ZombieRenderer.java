package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieRenderer extends AbstractZombieRenderer<Zombie, ZombieModel<Zombie>> {
    public ZombieRenderer(EntityRenderDispatcher param0) {
        super(
            param0,
            new ZombieModel<>(RenderType::entitySolid, 0.0F, false),
            new ZombieModel<>(RenderType::entitySolid, 0.5F, true),
            new ZombieModel<>(RenderType::entitySolid, 1.0F, true)
        );
    }
}
