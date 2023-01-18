package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        BlockPos var0 = this.minecraft.player.blockPosition();
        LevelReader var1 = this.minecraft.player.level;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 0.75F);
        RenderSystem.lineWidth(6.0F);

        for(BlockPos var2 : BlockPos.betweenClosed(var0.offset(-10, -10, -10), var0.offset(10, 10, 10))) {
            FluidState var3 = var1.getFluidState(var2);
            if (var3.is(FluidTags.WATER)) {
                double var4 = (double)((float)var2.getY() + var3.getHeight(var1, var2));
                DebugRenderer.renderFilledBox(
                    new AABB(
                            (double)((float)var2.getX() + 0.01F),
                            (double)((float)var2.getY() + 0.01F),
                            (double)((float)var2.getZ() + 0.01F),
                            (double)((float)var2.getX() + 0.99F),
                            var4,
                            (double)((float)var2.getZ() + 0.99F)
                        )
                        .move(-param2, -param3, -param4),
                    1.0F,
                    1.0F,
                    1.0F,
                    0.2F
                );
            }
        }

        for(BlockPos var5 : BlockPos.betweenClosed(var0.offset(-10, -10, -10), var0.offset(10, 10, 10))) {
            FluidState var6 = var1.getFluidState(var5);
            if (var6.is(FluidTags.WATER)) {
                DebugRenderer.renderFloatingText(
                    String.valueOf(var6.getAmount()),
                    (double)var5.getX() + 0.5,
                    (double)((float)var5.getY() + var6.getHeight(var1, var5)),
                    (double)var5.getZ() + 0.5,
                    -16777216
                );
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
