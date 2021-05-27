package net.minecraft.world.level.pathfinder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;

public class PathFinder {
    private static final float FUDGING = 1.5F;
    private final Node[] neighbors = new Node[32];
    private final int maxVisitedNodes;
    private final NodeEvaluator nodeEvaluator;
    private static final boolean DEBUG = false;
    private final BinaryHeap openSet = new BinaryHeap();

    public PathFinder(NodeEvaluator param0, int param1) {
        this.nodeEvaluator = param0;
        this.maxVisitedNodes = param1;
    }

    @Nullable
    public Path findPath(PathNavigationRegion param0, Mob param1, Set<BlockPos> param2, float param3, int param4, float param5) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(param0, param1);
        Node var0 = this.nodeEvaluator.getStart();
        Map<Target, BlockPos> var1 = param2.stream()
            .collect(
                Collectors.toMap(
                    param0x -> this.nodeEvaluator.getGoal((double)param0x.getX(), (double)param0x.getY(), (double)param0x.getZ()), Function.identity()
                )
            );
        Path var2 = this.findPath(param0.getProfiler(), var0, var1, param3, param4, param5);
        this.nodeEvaluator.done();
        return var2;
    }

    @Nullable
    private Path findPath(ProfilerFiller param0, Node param1, Map<Target, BlockPos> param2, float param3, int param4, float param5) {
        param0.push("find_path");
        param0.markForCharting(MetricCategory.PATH_FINDING);
        Set<Target> var0 = param2.keySet();
        param1.g = 0.0F;
        param1.h = this.getBestH(param1, var0);
        param1.f = param1.h;
        this.openSet.clear();
        this.openSet.insert(param1);
        Set<Node> var1 = ImmutableSet.of();
        int var2 = 0;
        Set<Target> var3 = Sets.newHashSetWithExpectedSize(var0.size());
        int var4 = (int)((float)this.maxVisitedNodes * param5);

        while(!this.openSet.isEmpty()) {
            if (++var2 >= var4) {
                break;
            }

            Node var5 = this.openSet.pop();
            var5.closed = true;

            for(Target var6 : var0) {
                if (var5.distanceManhattan(var6) <= (float)param4) {
                    var6.setReached();
                    var3.add(var6);
                }
            }

            if (!var3.isEmpty()) {
                break;
            }

            if (!(var5.distanceTo(param1) >= param3)) {
                int var7 = this.nodeEvaluator.getNeighbors(this.neighbors, var5);

                for(int var8 = 0; var8 < var7; ++var8) {
                    Node var9 = this.neighbors[var8];
                    float var10 = var5.distanceTo(var9);
                    var9.walkedDistance = var5.walkedDistance + var10;
                    float var11 = var5.g + var10 + var9.costMalus;
                    if (var9.walkedDistance < param3 && (!var9.inOpenSet() || var11 < var9.g)) {
                        var9.cameFrom = var5;
                        var9.g = var11;
                        var9.h = this.getBestH(var9, var0) * 1.5F;
                        if (var9.inOpenSet()) {
                            this.openSet.changeCost(var9, var9.g + var9.h);
                        } else {
                            var9.f = var9.g + var9.h;
                            this.openSet.insert(var9);
                        }
                    }
                }
            }
        }

        Optional<Path> var12 = !var3.isEmpty()
            ? var3.stream()
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param2.get(param1x), true))
                .min(Comparator.comparingInt(Path::getNodeCount))
            : var0.stream()
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param2.get(param1x), false))
                .min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
        param0.pop();
        return !var12.isPresent() ? null : var12.get();
    }

    private float getBestH(Node param0, Set<Target> param1) {
        float var0 = Float.MAX_VALUE;

        for(Target var1 : param1) {
            float var2 = param0.distanceTo(var1);
            var1.updateBest(var2, param0);
            var0 = Math.min(var2, var0);
        }

        return var0;
    }

    private Path reconstructPath(Node param0, BlockPos param1, boolean param2) {
        List<Node> var0 = Lists.newArrayList();
        Node var1 = param0;
        var0.add(0, param0);

        while(var1.cameFrom != null) {
            var1 = var1.cameFrom;
            var0.add(0, var1);
        }

        return new Path(var0, param1, param2);
    }
}
