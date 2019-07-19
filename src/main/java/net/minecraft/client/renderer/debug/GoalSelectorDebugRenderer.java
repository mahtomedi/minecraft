package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<Integer, List<GoalSelectorDebugRenderer.DebugGoal>> goalSelectors = Maps.newHashMap();

    @Override
    public void clear() {
        this.goalSelectors.clear();
    }

    public void addGoalSelector(int param0, List<GoalSelectorDebugRenderer.DebugGoal> param1) {
        this.goalSelectors.put(param0, param1);
    }

    public GoalSelectorDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableTexture();
        BlockPos var1 = new BlockPos(var0.getPosition().x, 0.0, var0.getPosition().z);
        this.goalSelectors.forEach((param1, param2) -> {
            for(int var0x = 0; var0x < param2.size(); ++var0x) {
                GoalSelectorDebugRenderer.DebugGoal var1x = param2.get(var0x);
                if (var1.closerThan(var1x.pos, 160.0)) {
                    double var2 = (double)var1x.pos.getX() + 0.5;
                    double var3x = (double)var1x.pos.getY() + 2.0 + (double)var0x * 0.25;
                    double var4x = (double)var1x.pos.getZ() + 0.5;
                    int var5 = var1x.isRunning ? -16711936 : -3355444;
                    DebugRenderer.renderFloatingText(var1x.name, var2, var3x, var4x, var5);
                }
            }

        });
        GlStateManager.enableDepthTest();
        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
    }

    @OnlyIn(Dist.CLIENT)
    public static class DebugGoal {
        public final BlockPos pos;
        public final int priority;
        public final String name;
        public final boolean isRunning;

        public DebugGoal(BlockPos param0, int param1, String param2, boolean param3) {
            this.pos = param0;
            this.priority = param1;
            this.name = param2;
            this.isRunning = param3;
        }
    }
}
