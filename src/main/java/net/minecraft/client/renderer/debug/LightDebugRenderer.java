package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public LightDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        Level var1 = this.minecraft.level;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        BlockPos var2 = new BlockPos(var0.getPosition());
        LongSet var3 = new LongOpenHashSet();

        for(BlockPos var4 : BlockPos.betweenClosed(var2.offset(-10, -10, -10), var2.offset(10, 10, 10))) {
            int var5 = var1.getBrightness(LightLayer.SKY, var4);
            float var6 = (float)(15 - var5) / 15.0F * 0.5F + 0.16F;
            int var7 = Mth.hsvToRgb(var6, 0.9F, 0.9F);
            long var8 = SectionPos.blockToSection(var4.asLong());
            if (var3.add(var8)) {
                DebugRenderer.renderFloatingText(
                    var1.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(var8)),
                    (double)(SectionPos.x(var8) * 16 + 8),
                    (double)(SectionPos.y(var8) * 16 + 8),
                    (double)(SectionPos.z(var8) * 16 + 8),
                    16711680,
                    0.3F
                );
            }

            if (var5 != 15) {
                DebugRenderer.renderFloatingText(String.valueOf(var5), (double)var4.getX() + 0.5, (double)var4.getY() + 0.25, (double)var4.getZ() + 0.5, var7);
            }
        }

        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}
