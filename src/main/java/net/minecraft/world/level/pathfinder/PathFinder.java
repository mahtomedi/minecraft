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
    private final Node[] neighbors = new Node[32];
    private final int maxVisitedNodes;
    private final NodeEvaluator nodeEvaluator;
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
        this.openSet.insert(param0);
        Set<Node> var1 = Sets.newHashSet();
        int var2 = 0;
        int var3 = (int)((float)this.maxVisitedNodes * param4);

        while(!this.openSet.isEmpty()) {
            if (++var2 >= var3) {
                break;
            }

            Node var4 = this.openSet.pop();
            var4.closed = true;
            var0.stream().filter(param2x -> var4.distanceManhattan(param2x) <= (float)param3).forEach(Target::setReached);
            if (var0.stream().anyMatch(Target::isReached)) {
                break;
            }

            if (!(var4.distanceTo(param0) >= param2)) {
                int var5 = this.nodeEvaluator.getNeighbors(this.neighbors, var4);

                for(int var6 = 0; var6 < var5; ++var6) {
                    Node var7 = this.neighbors[var6];
                    float var8 = var4.distanceTo(var7);
                    var7.walkedDistance = var4.walkedDistance + var8;
                    float var9 = var4.g + var8 + var7.costMalus;
                    if (var7.walkedDistance < param2 && (!var7.inOpenSet() || var9 < var7.g)) {
                        var7.cameFrom = var4;
                        var7.g = var9;
                        var7.h = this.getBestH(var7, var0) * 1.5F;
                        if (var7.inOpenSet()) {
                            this.openSet.changeCost(var7, var7.g + var7.h);
                        } else {
                            var7.f = var7.g + var7.h;
                            this.openSet.insert(var7);
                        }
                    }
                }
            }
        }

        Stream<Path> var10;
        if (var0.stream().anyMatch(Target::isReached)) {
            var10 = var0.stream()
                .filter(Target::isReached)
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param1.get(param1x), true))
                .sorted(Comparator.comparingInt(Path::getSize));
        } else {
            var10 = var0.stream()
                .map(param1x -> this.reconstructPath(param1x.getBestNode(), param1.get(param1x), false))
                .sorted(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getSize));
        }

        Optional<Path> var12 = var10.findFirst();
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
