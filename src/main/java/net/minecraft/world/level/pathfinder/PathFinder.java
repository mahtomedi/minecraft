package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;

public class PathFinder {
    private final BinaryHeap openSet = new BinaryHeap();
    private final Set<Node> closedSet = Sets.newHashSet();
    private final Node[] neighbors = new Node[32];
    private final int maxVisitedNodes;
    private NodeEvaluator nodeEvaluator;

    public PathFinder(NodeEvaluator param0, int param1) {
        this.nodeEvaluator = param0;
        this.maxVisitedNodes = param1;
    }

    @Nullable
    public Path findPath(LevelReader param0, Mob param1, Set<BlockPos> param2, float param3, int param4) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(param0, param1);
        Node var0 = this.nodeEvaluator.getStart();
        Map<Target, BlockPos> var1 = param2.stream()
            .collect(
                Collectors.toMap(
                    param0x -> this.nodeEvaluator.getGoal((double)param0x.getX(), (double)param0x.getY(), (double)param0x.getZ()), Function.identity()
                )
            );
        Path var2 = this.findPath(var0, var1, param3, param4);
        this.nodeEvaluator.done();
        return var2;
    }

    @Nullable
    private Path findPath(Node param0, Map<Target, BlockPos> param1, float param2, int param3) {
        Set<Target> var0 = param1.keySet();
        param0.g = 0.0F;
        param0.h = this.getBestH(param0, var0);
        param0.f = param0.h;
        this.openSet.clear();
        this.closedSet.clear();
        this.openSet.insert(param0);
        int var1 = 0;

        while(!this.openSet.isEmpty()) {
            if (++var1 >= this.maxVisitedNodes) {
                break;
            }

            Node var2 = this.openSet.pop();
            var2.closed = true;
            var0.stream().filter(param2x -> var2.distanceManhattan(param2x) <= (float)param3).forEach(Target::setReached);
            if (var0.stream().anyMatch(Target::isReached)) {
                break;
            }

            if (!(var2.distanceTo(param0) >= param2)) {
                int var3 = this.nodeEvaluator.getNeighbors(this.neighbors, var2);

                for(int var4 = 0; var4 < var3; ++var4) {
                    Node var5 = this.neighbors[var4];
                    float var6 = var2.distanceTo(var5);
                    var5.walkedDistance = var2.walkedDistance + var6;
                    float var7 = var2.g + var6 + var5.costMalus;
                    if (var5.walkedDistance < param2 && (!var5.inOpenSet() || var7 < var5.g)) {
                        var5.cameFrom = var2;
                        var5.g = var7;
                        var5.h = this.getBestH(var5, var0) * 1.5F;
                        if (var5.inOpenSet()) {
                            this.openSet.changeCost(var5, var5.g + var5.h);
                        } else {
                            var5.f = var5.g + var5.h;
                            this.openSet.insert(var5);
                        }
                    }
                }
            }
        }

        Stream<Path> var8;
        if (var0.stream().anyMatch(Target::isReached)) {
            var8 = var0.stream()
                .filter(Target::isReached)
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param1.get(param1x), true))
                .sorted(Comparator.comparingInt(Path::getSize));
        } else {
            var8 = var0.stream()
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param1.get(param1x), false))
                .sorted(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getSize));
        }

        Optional<Path> var10 = var8.findFirst();
        return !var10.isPresent() ? null : var10.get();
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
