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
    private final Map<GameTestInfo, BlockPos> northWestCorners = Maps.newHashMap();
    private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches = Lists.newArrayList();
    private MultipleTestTracker currentBatchTracker;
    private int currentBatchIndex = 0;
    private BlockPos.MutableBlockPos nextTestNorthWestCorner;

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

    private void runBatch(int param0) {
        this.currentBatchIndex = param0;
        this.currentBatchTracker = new MultipleTestTracker();
        if (param0 < this.batches.size()) {
            Pair<GameTestBatch, Collection<GameTestInfo>> var0 = this.batches.get(this.currentBatchIndex);
            GameTestBatch var1 = var0.getFirst();
            Collection<GameTestInfo> var2 = var0.getSecond();
            this.createStructuresForBatch(var2);
            var1.runBeforeBatchFunction(this.level);
            String var3 = var1.getName();
            LOGGER.info("Running test batch '" + var3 + "' (" + var2.size() + " tests)...");
            var2.forEach(param0x -> {
                this.currentBatchTracker.addTestToTrack(param0x);
                this.currentBatchTracker.addListener(new GameTestListener() {
                    @Override
                    public void testStructureLoaded(GameTestInfo param0) {
                    }

                    @Override
                    public void testFailed(GameTestInfo param0) {
                        GameTestBatchRunner.this.testCompleted(param0);
                    }
                });
                BlockPos var0x = this.northWestCorners.get(param0x);
                GameTestRunner.runTest(param0x, var0x, this.testTicker);
            });
        }
    }

    private void testCompleted(GameTestInfo param0) {
        if (this.currentBatchTracker.isDone()) {
            this.runBatch(this.currentBatchIndex + 1);
        }

    }

    private void createStructuresForBatch(Collection<GameTestInfo> param0) {
        int var0 = 0;
        AABB var1 = new AABB(this.nextTestNorthWestCorner);

        for(GameTestInfo var2 : param0) {
            BlockPos var3 = new BlockPos(this.nextTestNorthWestCorner);
            StructureBlockEntity var4 = StructureUtils.spawnStructure(var2.getStructureName(), var3, var2.getRotation(), 2, this.level, true);
            AABB var5 = StructureUtils.getStructureBounds(var4);
            var2.setStructureBlockPos(var4.getBlockPos());
            this.northWestCorners.put(var2, new BlockPos(this.nextTestNorthWestCorner));
            var1 = var1.minmax(var5);
            this.nextTestNorthWestCorner.move((int)var5.getXsize() + 5, 0, 0);
            if (var0++ % this.testsPerRow == this.testsPerRow - 1) {
                this.nextTestNorthWestCorner.move(0, 0, (int)var1.getZsize() + 6);
                this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
                var1 = new AABB(this.nextTestNorthWestCorner);
            }
        }

    }
}
