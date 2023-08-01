package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST = 160;
    private final Minecraft minecraft;
    private final Int2ObjectMap<GoalSelectorDebugRenderer.EntityGoalInfo> goalSelectors = new Int2ObjectOpenHashMap<>();

    @Override
    public void clear() {
        this.goalSelectors.clear();
    }

    public void addGoalSelector(int param0, BlockPos param1, List<GoalDebugPayload.DebugGoal> param2) {
        this.goalSelectors.put(param0, new GoalSelectorDebugRenderer.EntityGoalInfo(param1, param2));
    }

    public void removeGoalSelector(int param0) {
        this.goalSelectors.remove(param0);
    }

    public GoalSelectorDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        BlockPos var1 = BlockPos.containing(var0.getPosition().x, 0.0, var0.getPosition().z);

        for(GoalSelectorDebugRenderer.EntityGoalInfo var2 : this.goalSelectors.values()) {
            BlockPos var3 = var2.entityPos;
            if (var1.closerThan(var3, 160.0)) {
                for(int var4 = 0; var4 < var2.goals.size(); ++var4) {
                    GoalDebugPayload.DebugGoal var5 = var2.goals.get(var4);
                    double var6 = (double)var3.getX() + 0.5;
                    double var7 = (double)var3.getY() + 2.0 + (double)var4 * 0.25;
                    double var8 = (double)var3.getZ() + 0.5;
                    int var9 = var5.isRunning() ? -16711936 : -3355444;
                    DebugRenderer.renderFloatingText(param0, param1, var5.name(), var6, var7, var8, var9);
                }
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    static record EntityGoalInfo(BlockPos entityPos, List<GoalDebugPayload.DebugGoal> goals) {
    }
}
