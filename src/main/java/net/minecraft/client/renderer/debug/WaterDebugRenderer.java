package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WaterDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public WaterDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        double var1 = var0.getPosition().x;
        double var2 = var0.getPosition().y;
        double var3 = var0.getPosition().z;
        BlockPos var4 = this.minecraft.player.getCommandSenderBlockPosition();
        LevelReader var5 = this.minecraft.player.level;
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(6.0F);

        for(BlockPos var6 : BlockPos.betweenClosed(var4.offset(-10, -10, -10), var4.offset(10, 10, 10))) {
            FluidState var7 = var5.getFluidState(var6);
            if (var7.is(FluidTags.WATER)) {
                double var8 = (double)((float)var6.getY() + var7.getHeight(var5, var6));
                DebugRenderer.renderFilledBox(
                    new AABB(
                            (double)((float)var6.getX() + 0.01F),
                            (double)((float)var6.getY() + 0.01F),
                            (double)((float)var6.getZ() + 0.01F),
                            (double)((float)var6.getX() + 0.99F),
                            var8,
                            (double)((float)var6.getZ() + 0.99F)
                        )
                        .move(-var1, -var2, -var3),
                    1.0F,
                    1.0F,
                    1.0F,
                    0.2F
                );
            }
        }

        for(BlockPos var9 : BlockPos.betweenClosed(var4.offset(-10, -10, -10), var4.offset(10, 10, 10))) {
            FluidState var10 = var5.getFluidState(var9);
            if (var10.is(FluidTags.WATER)) {
                DebugRenderer.renderFloatingText(
                    String.valueOf(var10.getAmount()),
                    (double)var9.getX() + 0.5,
                    (double)((float)var9.getY() + var10.getHeight(var5, var9)),
                    (double)var9.getZ() + 0.5,
                    -16777216
                );
            }
        }

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
