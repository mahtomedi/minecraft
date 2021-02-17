package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface BlockEntityRenderer<T extends BlockEntity> {
    void render(T var1, float var2, PoseStack var3, MultiBufferSource var4, int var5, int var6);

    default boolean shouldRenderOffScreen(T param0) {
        return false;
    }

    default int getViewDistance() {
        return 64;
    }

    default boolean shouldRender(T param0, Vec3 param1) {
        return Vec3.atCenterOf(param0.getBlockPos()).closerThan(param1, (double)this.getViewDistance());
    }
}
