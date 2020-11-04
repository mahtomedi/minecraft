package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestBatchRunner {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlockPos firstTestNorthWestCorner;
    private final ServerLevel level;
    private final GameTestTicker testTicker;
    private final int testsPerRow;
    private final List<GameTestInfo> allTestInfos = Lists.newArrayList();
    private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches = Lists.newArrayList();
    private final BlockPos.MutableBlockPos nextTestNorthWestCorner;

    public GameTestBatchRunner(Collection<GameTestBatch> param0, BlockPos param1, Rotation param2, ServerLevel param3, GameTestTicker param4, int param5) {
        this.nextTestNorthWestCorner = param1.mutable();
        this.firstTestNorthWestCorner = param1;
        this.level = param3;
        this.testTicker = param4;
        this.testsPerRow = param5;
        param0.forEach(param2x -> {
            Collection<GameTestInfo> var0 = Lists.newArrayList();

            for(TestFunction var2x : param2x.getTestFunctions()) {
                GameTestInfo var3x = new GameTestInfo(var2x, param2, param3);
                var0.add(var3x);
                this.allTestInfos.add(var3x);
            }

            this.batches.add(Pair.of(param2x, var0));
        });
    }

    public List<GameTestInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.runBatch(0);
    }

    private void runBatch(final int param0) {
        if (param0 < this.batches.size()) {
            Pair<GameTestBatch, Collection<GameTestInfo>> var0 = this.batches.get(param0);
            final GameTestBatch var1 = var0.getFirst();
            Collection<GameTestInfo> var2 = var0.getSecond();
            Map<GameTestInfo, BlockPos> var3 = this.createStructuresForBatch(var2);
            String var4 = var1.getName();
            LOGGER.info("Running test batch '{}' ({} tests)...", var4, var2.size());
            var1.runBeforeBatchFunction(this.level);
            final MultipleTestTracker var5 = new MultipleTestTracker();
            var2.forEach(var5::addTestToTrack);
            var5.addListener(new GameTestListener() {
                private void testCompleted() {
                    if (var5.isDone()) {
                        var1.runAfterBatchFunction(GameTestBatchRunner.this.level);
                        GameTestBatchRunner.this.runBatch(param0 + 1);
                    }

                }

                @Override
                public void testStructureLoaded(GameTestInfo param0x) {
                }

                @Override
                public void testFailed(GameTestInfo param0x) {
                    this.testCompleted();
                }
            });
            var2.forEach(param1 -> {
                BlockPos var0x = var3.get(param1);
                GameTestRunner.runTest(param1, var0x, this.testTicker);
            });
        }
    }

    private Map<GameTestInfo, BlockPos> createStructuresForBatch(Collection<GameTestInfo> param0) {
        Map<GameTestInfo, BlockPos> var0 = Maps.newHashMap();
        int var1 = 0;
        AABB var2 = new AABB(this.nextTestNorthWestCorner);

        for(GameTestInfo var3 : param0) {
            BlockPos var4 = new BlockPos(this.nextTestNorthWestCorner);
            StructureBlockEntity var5 = StructureUtils.spawnStructure(var3.getStructureName(), var4, var3.getRotation(), 2, this.level, true);
            AABB var6 = StructureUtils.getStructureBounds(var5);
            var3.setStructureBlockPos(var5.getBlockPos());
            var0.put(var3, new BlockPos(this.nextTestNorthWestCorner));
            var2 = var2.minmax(var6);
            this.nextTestNorthWestCorner.move((int)var6.getXsize() + 5, 0, 0);
            if (var1++ % this.testsPerRow == this.testsPerRow - 1) {
                this.nextTestNorthWestCorner.move(0, 0, (int)var2.getZsize() + 6);
                this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
                var2 = new AABB(this.nextTestNorthWestCorner);
            }
        }

        return var0;
    }
}
