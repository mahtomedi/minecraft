package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
    private static final int MAX_RENDER_DIST = 10;

    public LightDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        Level var0 = this.minecraft.level;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BlockPos var1 = new BlockPos(param2, param3, param4);
        LongSet var2 = new LongOpenHashSet();

        for(BlockPos var3 : BlockPos.betweenClosed(var1.offset(-10, -10, -10), var1.offset(10, 10, 10))) {
            int var4 = var0.getBrightness(LightLayer.SKY, var3);
            float var5 = (float)(15 - var4) / 15.0F * 0.5F + 0.16F;
            int var6 = Mth.hsvToRgb(var5, 0.9F, 0.9F);
            long var7 = SectionPos.blockToSection(var3.asLong());
            if (var2.add(var7)) {
                DebugRenderer.renderFloatingText(
                    var0.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(var7)),
                    (double)SectionPos.sectionToBlockCoord(SectionPos.x(var7), 8),
                    (double)SectionPos.sectionToBlockCoord(SectionPos.y(var7), 8),
                    (double)SectionPos.sectionToBlockCoord(SectionPos.z(var7), 8),
                    16711680,
                    0.3F
                );
            }

            if (var4 != 15) {
                DebugRenderer.renderFloatingText(String.valueOf(var4), (double)var3.getX() + 0.5, (double)var3.getY() + 0.25, (double)var3.getZ() + 0.5, var6);
            }
        }

    }
}
