package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestBatchRunner {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<GameTestBatch> batches;
    private final BlockPos startPos;
    private final ServerLevel level;
    private final GameTestTicker testTicker;
    private final List<GameTestInfo> allTestInfos = Lists.newArrayList();
    private final Map<String, Collection<GameTestInfo>> testInfosPerBatch = Maps.newHashMap();
    private MultipleTestTracker currentBatchTracker;
    private int currentBatchIndex = 0;
    private BlockPos.MutableBlockPos nextTestPos;
    private int maxDepthOnThisRow = 0;

    public GameTestBatchRunner(Collection<GameTestBatch> param0, BlockPos param1, ServerLevel param2, GameTestTicker param3) {
        this.batches = Lists.newArrayList(param0);
        this.nextTestPos = new BlockPos.MutableBlockPos(param1);
        this.startPos = param1;
        this.level = param2;
        this.testTicker = param3;
    }

    private void spawnAllStructureBlocksAndShowBounds() {
        this.batches.forEach(param0 -> {
            Collection<GameTestInfo> var0 = Lists.newArrayList();

            for(TestFunction var2 : param0.getTestFunctions()) {
                BlockPos var3 = new BlockPos(this.nextTestPos);
                StructureUtils.spawnStructure(var2.getStructureName(), var3, 2, this.level, true);
                GameTestInfo var4 = new GameTestInfo(var2, var3, this.level);
                var0.add(var4);
                this.allTestInfos.add(var4);
                this.testInfosPerBatch.put(param0.getName(), var0);
                BlockPos var5 = var4.getStructureSize();
                int var6 = var5 == null ? 1 : var5.getX();
                int var7 = var5 == null ? 1 : var5.getZ();
                this.maxDepthOnThisRow = Math.max(this.maxDepthOnThisRow, var7);
                this.nextTestPos.move(var6 + 4, 0, 0);
                if (this.allTestInfos.size() % 8 == 0) {
                    this.nextTestPos.move(0, 0, this.maxDepthOnThisRow + 5);
                    this.nextTestPos.setX(this.startPos.getX());
                    this.maxDepthOnThisRow = 0;
                }
            }

        });
    }

    public List<GameTestInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.spawnAllStructureBlocksAndShowBounds();
        this.runBatch(0);
    }

    private void runBatch(int param0) {
        this.currentBatchIndex = param0;
        this.currentBatchTracker = new MultipleTestTracker();
        if (param0 < this.batches.size()) {
            GameTestBatch var0 = this.batches.get(this.currentBatchIndex);
            var0.runBeforeBatchFunction(this.level);
            this.spawnTestStructures(var0);
            String var1 = var0.getName();
            Collection<GameTestInfo> var2 = this.testInfosPerBatch.get(var1);
            LOGGER.info("Running test batch '" + var1 + "' (" + var2.size() + " tests)...");
            var2.forEach(param0x -> {
                this.currentBatchTracker.add(param0x);
                this.currentBatchTracker.setListener(new GameTestListener() {
                    @Override
                    public void testStructureLoaded(GameTestInfo param0) {
                    }

                    @Override
                    public void testPassed(GameTestInfo param0) {
                        GameTestBatchRunner.this.testCompleted(param0);
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

    private void spawnTestStructures(GameTestBatch param0) {
        Collection<GameTestInfo> var0 = this.testInfosPerBatch.get(param0.getName());
        var0.forEach(param0x -> param0x.spawnStructure(2));
    }
}
