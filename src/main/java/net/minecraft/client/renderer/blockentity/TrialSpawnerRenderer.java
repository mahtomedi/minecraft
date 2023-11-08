package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrialSpawnerRenderer implements BlockEntityRenderer<TrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public TrialSpawnerRenderer(BlockEntityRendererProvider.Context param0) {
        this.entityRenderer = param0.getEntityRenderer();
    }

    public void render(TrialSpawnerBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Level var0 = param0.getLevel();
        if (var0 != null) {
            TrialSpawner var1 = param0.getTrialSpawner();
            TrialSpawnerData var2 = var1.getData();
            Entity var3 = var2.getOrCreateDisplayEntity(var1, var0, var1.getState());
            if (var3 != null) {
                SpawnerRenderer.renderEntityInSpawner(param1, param2, param3, param4, var3, this.entityRenderer, var2.getOSpin(), var2.getSpin());
            }

        }
    }
}
