package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
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
