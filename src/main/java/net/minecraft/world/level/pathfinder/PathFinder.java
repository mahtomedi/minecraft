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
import net.minecraft.world.level.PathNavigationRegion;

public class PathFinder {
    private final BinaryHeap openSet = new BinaryHeap();
    private final Set<Node> closedSet = Sets.newHashSet();
    private final Node[] neighbors = new Node[32];
    private final int maxVisitedNodes;
    private final NodeEvaluator nodeEvaluator;

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
        Path var2 = this.findPath(var0, var1, param3, param4, param5);
        this.nodeEvaluator.done();
        return var2;
    }

    @Nullable
    private Path findPath(Node param0, Map<Target, BlockPos> param1, float param2, int param3, float param4) {
        Set<Target> var0 = param1.keySet();
        param0.g = 0.0F;
        param0.h = this.getBestH(param0, var0);
        param0.f = param0.h;
        this.openSet.clear();
        this.closedSet.clear();
        this.openSet.insert(param0);
        int var1 = 0;
        int var2 = (int)((float)this.maxVisitedNodes * param4);

        while(!this.openSet.isEmpty()) {
            if (++var1 >= var2) {
                break;
            }

            Node var3 = this.openSet.pop();
            var3.closed = true;
            var0.stream().filter(param2x -> var3.distanceManhattan(param2x) <= (float)param3).forEach(Target::setReached);
            if (var0.stream().anyMatch(Target::isReached)) {
                break;
            }

            if (!(var3.distanceTo(param0) >= param2)) {
                int var4 = this.nodeEvaluator.getNeighbors(this.neighbors, var3);

                for(int var5 = 0; var5 < var4; ++var5) {
                    Node var6 = this.neighbors[var5];
                    float var7 = var3.distanceTo(var6);
                    var6.walkedDistance = var3.walkedDistance + var7;
                    float var8 = var3.g + var7 + var6.costMalus;
                    if (var6.walkedDistance < param2 && (!var6.inOpenSet() || var8 < var6.g)) {
                        var6.cameFrom = var3;
                        var6.g = var8;
                        var6.h = this.getBestH(var6, var0) * 1.5F;
                        if (var6.inOpenSet()) {
                            this.openSet.changeCost(var6, var6.g + var6.h);
                        } else {
                            var6.f = var6.g + var6.h;
                            this.openSet.insert(var6);
                        }
                    }
                }
            }
        }

        Stream<Path> var9;
        if (var0.stream().anyMatch(Target::isReached)) {
            var9 = var0.stream()
                .filter(Target::isReached)
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param1.get(param1x), true))
                .sorted(Comparator.comparingInt(Path::getSize));
        } else {
            var9 = var0.stream()
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param1.get(param1x), false))
                .sorted(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getSize));
        }

        Optional<Path> var11 = var9.findFirst();
        return !var11.isPresent() ? null : var11.get();
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
