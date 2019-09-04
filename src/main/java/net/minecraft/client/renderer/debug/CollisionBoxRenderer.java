package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CollisionBoxRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<VoxelShape> shapes = Collections.emptyList();

    public CollisionBoxRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        double var1 = (double)Util.getNanos();
        if (var1 - this.lastUpdateTime > 1.0E8) {
            this.lastUpdateTime = var1;
            this.shapes = var0.getEntity()
                .level
                .getCollisions(var0.getEntity(), var0.getEntity().getBoundingBox().inflate(6.0), Collections.emptySet())
                .collect(Collectors.toList());
        }

        double var2 = var0.getPosition().x;
        double var3 = var0.getPosition().y;
        double var4 = var0.getPosition().z;
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.lineWidth(2.0F);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);

        for(VoxelShape var5 : this.shapes) {
            LevelRenderer.renderVoxelShape(var5, -var2, -var3, -var4, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
