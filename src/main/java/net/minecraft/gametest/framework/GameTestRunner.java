package net.minecraft.gametest.framework;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GameTestRunner {
    public static TestReporter TEST_REPORTER = new LogTestReporter();

    public static void runTest(GameTestInfo param0, GameTestTicker param1) {
        param0.startExecution();
        param1.add(param0);
        param0.addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo param0) {
                GameTestRunner.spawnBeacon(param0, Blocks.LIGHT_GRAY_STAINED_GLASS);
            }

            @Override
            public void testFailed(GameTestInfo param0) {
                GameTestRunner.spawnBeacon(param0, param0.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
                GameTestRunner.spawnLectern(param0, Util.describeError(param0.getError()));
                GameTestRunner.visualizeFailedTest(param0);
            }
        });
        param0.spawnStructure(2);
    }

    public static Collection<GameTestInfo> runTestBatches(Collection<GameTestBatch> param0, BlockPos param1, ServerLevel param2, GameTestTicker param3) {
        GameTestBatchRunner var0 = new GameTestBatchRunner(param0, param1, param2, param3);
        var0.start();
        return var0.getTestInfos();
    }

    public static Collection<GameTestInfo> runTests(Collection<TestFunction> param0, BlockPos param1, ServerLevel param2, GameTestTicker param3) {
        return runTestBatches(groupTestsIntoBatches(param0), param1, param2, param3);
    }

    public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> param0) {
        Map<String, Collection<TestFunction>> var0 = Maps.newHashMap();
        param0.forEach(param1 -> {
            String var0x = param1.getBatchName();
            Collection<TestFunction> var1x = var0.computeIfAbsent(var0x, param0x -> Lists.newArrayList());
            var1x.add(param1);
        });
        return var0.keySet().stream().flatMap(param1 -> {
            Collection<TestFunction> var0x = var0.get(param1);
            Consumer<ServerLevel> var1x = GameTestRegistry.getBeforeBatchFunction(param1);
            AtomicInteger var2 = new AtomicInteger();
            return Streams.stream(Iterables.partition(var0x, 100)).map(param4 -> new GameTestBatch(param1 + ":" + var2.incrementAndGet(), var0x, var1x));
        }).collect(Collectors.toList());
    }

    private static void visualizeFailedTest(GameTestInfo param0) {
        Throwable var0 = param0.getError();
        String var1 = param0.getTestName() + " failed! " + Util.describeError(var0);
        say(param0.getLevel(), ChatFormatting.RED, var1);
        if (var0 instanceof GameTestAssertPosException) {
            GameTestAssertPosException var2 = (GameTestAssertPosException)var0;
            showRedBox(param0.getLevel(), var2.getAbsolutePos(), var2.getMessageToShowAtBlock());
        }

        TEST_REPORTER.onTestFailed(param0);
    }

    private static void spawnBeacon(GameTestInfo param0, Block param1) {
        ServerLevel var0 = param0.getLevel();
        BlockPos var1 = param0.getTestPos();
        BlockPos var2 = var1.offset(-1, -1, -1);
        var0.setBlockAndUpdate(var2, Blocks.BEACON.defaultBlockState());
        BlockPos var3 = var2.offset(0, 1, 0);
        var0.setBlockAndUpdate(var3, param1.defaultBlockState());

        for(int var4 = -1; var4 <= 1; ++var4) {
            for(int var5 = -1; var5 <= 1; ++var5) {
                BlockPos var6 = var2.offset(var4, -1, var5);
                var0.setBlockAndUpdate(var6, Blocks.IRON_BLOCK.defaultBlockState());
            }
        }

    }

    private static void spawnLectern(GameTestInfo param0, String param1) {
        ServerLevel var0 = param0.getLevel();
        BlockPos var1 = param0.getTestPos();
        BlockPos var2 = var1.offset(-1, 1, -1);
        var0.setBlockAndUpdate(var2, Blocks.LECTERN.defaultBlockState());
        BlockState var3 = var0.getBlockState(var2);
        ItemStack var4 = createBook(param0.getTestName(), param0.isRequired(), param1);
        LecternBlock.tryPlaceBook(var0, var2, var3, var4);
    }

    private static ItemStack createBook(String param0, boolean param1, String param2) {
        ItemStack var0 = new ItemStack(Items.WRITABLE_BOOK);
        ListTag var1 = new ListTag();
        StringBuffer var2 = new StringBuffer();
        Arrays.stream(param0.split("\\.")).forEach(param1x -> var2.append(param1x).append('\n'));
        if (!param1) {
            var2.append("(optional)\n");
        }

        var2.append("-------------------\n");
        var1.add(StringTag.valueOf(var2.toString() + param2));
        var0.addTagElement("pages", var1);
        return var0;
    }

    private static void say(ServerLevel param0, ChatFormatting param1, String param2) {
        param0.getPlayers(param0x -> true).forEach(param2x -> param2x.sendMessage(new TextComponent(param2).withStyle(param1)));
    }

    public static void clearMarkers(ServerLevel param0) {
        DebugPackets.sendGameTestClearPacket(param0);
    }

    private static void showRedBox(ServerLevel param0, BlockPos param1, String param2) {
        DebugPackets.sendGameTestAddMarker(param0, param1, param2, -2130771968, Integer.MAX_VALUE);
    }

    public static void clearAllTests(ServerLevel param0, BlockPos param1, GameTestTicker param2, int param3) {
        param2.clear();
        BlockPos var0 = param1.offset(-param3, 0, -param3);
        BlockPos var1 = param1.offset(param3, 0, param3);
        BlockPos.betweenClosedStream(var0, var1).filter(param1x -> param0.getBlockState(param1x).is(Blocks.STRUCTURE_BLOCK)).forEach(param1x -> {
            StructureBlockEntity var0x = (StructureBlockEntity)param0.getBlockEntity(param1x);
            BlockPos var1x = var0x.getBlockPos();
            BoundingBox var2x = StructureUtils.createStructureBoundingBox(var1x, var0x.getStructureSize(), 2);
            StructureUtils.clearSpaceForStructure(var2x, var1x.getY(), param0);
        });
    }
}
