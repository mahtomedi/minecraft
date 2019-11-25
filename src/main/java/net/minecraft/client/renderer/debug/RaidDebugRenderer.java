package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaidDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private Collection<BlockPos> raidCenters = Lists.newArrayList();

    public RaidDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void setRaidCenters(Collection<BlockPos> param0) {
        this.raidCenters = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        BlockPos var0 = this.getCamera().getBlockPosition();

        for(BlockPos var1 : this.raidCenters) {
            if (var0.closerThan(var1, 160.0)) {
                highlightRaidCenter(var1);
            }
        }

    }

    private static void highlightRaidCenter(BlockPos param0) {
        DebugRenderer.renderFilledBox(param0.offset(-0.5, -0.5, -0.5), param0.offset(1.5, 1.5, 1.5), 1.0F, 0.0F, 0.0F, 0.15F);
        int var0 = -65536;
        renderTextOverBlock("Raid center", param0, -65536);
    }

    private static void renderTextOverBlock(String param0, BlockPos param1, int param2) {
        double var0 = (double)param1.getX() + 0.5;
        double var1 = (double)param1.getY() + 1.3;
        double var2 = (double)param1.getZ() + 0.5;
        DebugRenderer.renderFloatingText(param0, var0, var1, var2, param2, 0.04F, true, 0.0F, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }
}
