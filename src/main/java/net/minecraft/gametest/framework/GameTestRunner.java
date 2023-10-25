package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class GameTestRunner {
    private static final int MAX_TESTS_PER_BATCH = 50;
    public static final int SPACE_BETWEEN_COLUMNS = 5;
    public static final int SPACE_BETWEEN_ROWS = 6;
    public static final int DEFAULT_TESTS_PER_ROW = 8;

    public static void runTest(GameTestInfo param0, BlockPos param1, GameTestTicker param2) {
        param0.startExecution();
        param2.add(param0);
        param0.addListener(new ReportGameListener(param0, param2, param1));
        param0.spawnStructure(param1);
    }

    public static Collection<GameTestInfo> runTestBatches(
        Collection<GameTestBatch> param0, BlockPos param1, Rotation param2, ServerLevel param3, GameTestTicker param4, int param5
    ) {
        GameTestBatchRunner var0 = new GameTestBatchRunner(param0, param1, param2, param3, param4, param5);
        var0.start();
        return var0.getTestInfos();
    }

    public static Collection<GameTestInfo> runTests(
        Collection<TestFunction> param0, BlockPos param1, Rotation param2, ServerLevel param3, GameTestTicker param4, int param5
    ) {
        return runTestBatches(groupTestsIntoBatches(param0), param1, param2, param3, param4, param5);
    }

    public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> param0) {
        Map<String, List<TestFunction>> var0 = param0.stream().collect(Collectors.groupingBy(TestFunction::getBatchName));
        return var0.entrySet()
            .stream()
            .flatMap(
                param0x -> {
                    String var0x = param0x.getKey();
                    Consumer<ServerLevel> var1x = GameTestRegistry.getBeforeBatchFunction(var0x);
                    Consumer<ServerLevel> var2 = GameTestRegistry.getAfterBatchFunction(var0x);
                    MutableInt var3 = new MutableInt();
                    Collection<TestFunction> var4 = param0x.getValue();
                    return Streams.stream(Iterables.partition(var4, 50))
                        .map(param4 -> new GameTestBatch(var0x + ":" + var3.incrementAndGet(), ImmutableList.copyOf(param4), var1x, var2));
                }
            )
            .collect(ImmutableList.toImmutableList());
    }

    public static void clearAllTests(ServerLevel param0, BlockPos param1, GameTestTicker param2, int param3) {
        param2.clear();
        BlockPos var0 = param1.offset(-param3, 0, -param3);
        BlockPos var1 = param1.offset(param3, 0, param3);
        BlockPos.betweenClosedStream(var0, var1).filter(param1x -> param0.getBlockState(param1x).is(Blocks.STRUCTURE_BLOCK)).forEach(param1x -> {
            StructureBlockEntity var0x = (StructureBlockEntity)param0.getBlockEntity(param1x);
            BoundingBox var1x = StructureUtils.getStructureBoundingBox(var0x);
            StructureUtils.clearSpaceForStructure(var1x, param0);
        });
    }

    public static void clearMarkers(ServerLevel param0) {
        DebugPackets.sendGameTestClearPacket(param0);
    }
}
