package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestBatchRunner {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlockPos startPos;
    private final ServerLevel level;
    private final GameTestTicker testTicker;
    private final List<GameTestInfo> allTestInfos = Lists.newArrayList();
    private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches = Lists.newArrayList();
    private MultipleTestTracker currentBatchTracker;
    private int currentBatchIndex = 0;
    private BlockPos.MutableBlockPos nextTestPos;
    private int maxDepthOnThisRow = 0;

    public GameTestBatchRunner(Collection<GameTestBatch> param0, BlockPos param1, ServerLevel param2, GameTestTicker param3) {
        this.nextTestPos = new BlockPos.MutableBlockPos(param1);
        this.startPos = param1;
        this.level = param2;
        this.testTicker = param3;
        param0.forEach(param1x -> {
            Collection<GameTestInfo> var0 = Lists.newArrayList();

            for(TestFunction var2x : param1x.getTestFunctions()) {
                GameTestInfo var3x = new GameTestInfo(var2x, param2);
                var0.add(var3x);
                this.allTestInfos.add(var3x);
            }

            this.batches.add(Pair.of(param1x, var0));
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
                this.currentBatchTracker.add(param0x);
                this.currentBatchTracker.setListener(new GameTestListener() {
                    @Override
                    public void testStructureLoaded(GameTestInfo param0) {
                    }

                    @Override
                    public void testFailed(GameTestInfo param0) {
                        GameTestBatchRunner.this.testCompleted(param0);
                    }
                });
                GameTestRunner.runTest(param0x, this.testTicker);
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

        for(GameTestInfo var1 : param0) {
            BlockPos var2 = new BlockPos(this.nextTestPos);
            var1.assignPosition(var2);
            StructureUtils.spawnStructure(var1.getStructureName(), var2, 2, this.level, true);
            BlockPos var3 = var1.getStructureSize();
            int var4 = var3 == null ? 1 : var3.getX();
            int var5 = var3 == null ? 1 : var3.getZ();
            this.maxDepthOnThisRow = Math.max(this.maxDepthOnThisRow, var5);
            this.nextTestPos.move(var4 + 4, 0, 0);
            if (var0++ % 8 == 0) {
                this.nextTestPos.move(0, 0, this.maxDepthOnThisRow + 5);
                this.nextTestPos.setX(this.startPos.getX());
                this.maxDepthOnThisRow = 0;
            }
        }

    }
}
