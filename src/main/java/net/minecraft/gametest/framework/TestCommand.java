package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class TestCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_CLEAR_RADIUS = 200;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
    private static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int SHOW_POS_DURATION_MS = 10000;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("test")
                .then(
                    Commands.literal("runthis")
                        .executes(param0x -> runNearbyTest(param0x.getSource(), false))
                        .then(Commands.literal("untilFailed").executes(param0x -> runNearbyTest(param0x.getSource(), true)))
                )
                .then(Commands.literal("resetthis").executes(param0x -> resetNearbyTest(param0x.getSource())))
                .then(Commands.literal("runthese").executes(param0x -> runAllNearbyTests(param0x.getSource(), false)))
                .then(
                    Commands.literal("runfailed")
                        .executes(param0x -> runLastFailedTests(param0x.getSource(), false, 0, 8))
                        .then(
                            Commands.argument("onlyRequiredTests", BoolArgumentType.bool())
                                .executes(param0x -> runLastFailedTests(param0x.getSource(), BoolArgumentType.getBool(param0x, "onlyRequiredTests"), 0, 8))
                                .then(
                                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> runLastFailedTests(
                                                    param0x.getSource(),
                                                    BoolArgumentType.getBool(param0x, "onlyRequiredTests"),
                                                    IntegerArgumentType.getInteger(param0x, "rotationSteps"),
                                                    8
                                                )
                                        )
                                        .then(
                                            Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                                .executes(
                                                    param0x -> runLastFailedTests(
                                                            param0x.getSource(),
                                                            BoolArgumentType.getBool(param0x, "onlyRequiredTests"),
                                                            IntegerArgumentType.getInteger(param0x, "rotationSteps"),
                                                            IntegerArgumentType.getInteger(param0x, "testsPerRow")
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("run")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(param0x -> runTest(param0x.getSource(), TestFunctionArgument.getTestFunction(param0x, "testName"), 0))
                                .then(
                                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> runTest(
                                                    param0x.getSource(),
                                                    TestFunctionArgument.getTestFunction(param0x, "testName"),
                                                    IntegerArgumentType.getInteger(param0x, "rotationSteps")
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("runall")
                        .executes(param0x -> runAllTests(param0x.getSource(), 0, 8))
                        .then(
                            Commands.argument("testClassName", TestClassNameArgument.testClassName())
                                .executes(
                                    param0x -> runAllTestsInClass(param0x.getSource(), TestClassNameArgument.getTestClassName(param0x, "testClassName"), 0, 8)
                                )
                                .then(
                                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> runAllTestsInClass(
                                                    param0x.getSource(),
                                                    TestClassNameArgument.getTestClassName(param0x, "testClassName"),
                                                    IntegerArgumentType.getInteger(param0x, "rotationSteps"),
                                                    8
                                                )
                                        )
                                        .then(
                                            Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                                .executes(
                                                    param0x -> runAllTestsInClass(
                                                            param0x.getSource(),
                                                            TestClassNameArgument.getTestClassName(param0x, "testClassName"),
                                                            IntegerArgumentType.getInteger(param0x, "rotationSteps"),
                                                            IntegerArgumentType.getInteger(param0x, "testsPerRow")
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.argument("rotationSteps", IntegerArgumentType.integer())
                                .executes(param0x -> runAllTests(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "rotationSteps"), 8))
                                .then(
                                    Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> runAllTests(
                                                    param0x.getSource(),
                                                    IntegerArgumentType.getInteger(param0x, "rotationSteps"),
                                                    IntegerArgumentType.getInteger(param0x, "testsPerRow")
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("export")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(param0x -> exportTestStructure(param0x.getSource(), StringArgumentType.getString(param0x, "testName")))
                        )
                )
                .then(Commands.literal("exportthis").executes(param0x -> exportNearestTestStructure(param0x.getSource())))
                .then(Commands.literal("exportthese").executes(param0x -> exportAllNearbyTests(param0x.getSource())))
                .then(
                    Commands.literal("import")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(param0x -> importTestStructure(param0x.getSource(), StringArgumentType.getString(param0x, "testName")))
                        )
                )
                .then(
                    Commands.literal("pos")
                        .executes(param0x -> showPos(param0x.getSource(), "pos"))
                        .then(
                            Commands.argument("var", StringArgumentType.word())
                                .executes(param0x -> showPos(param0x.getSource(), StringArgumentType.getString(param0x, "var")))
                        )
                )
                .then(
                    Commands.literal("create")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(param0x -> createNewStructure(param0x.getSource(), StringArgumentType.getString(param0x, "testName"), 5, 5, 5))
                                .then(
                                    Commands.argument("width", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> createNewStructure(
                                                    param0x.getSource(),
                                                    StringArgumentType.getString(param0x, "testName"),
                                                    IntegerArgumentType.getInteger(param0x, "width"),
                                                    IntegerArgumentType.getInteger(param0x, "width"),
                                                    IntegerArgumentType.getInteger(param0x, "width")
                                                )
                                        )
                                        .then(
                                            Commands.argument("height", IntegerArgumentType.integer())
                                                .then(
                                                    Commands.argument("depth", IntegerArgumentType.integer())
                                                        .executes(
                                                            param0x -> createNewStructure(
                                                                    param0x.getSource(),
                                                                    StringArgumentType.getString(param0x, "testName"),
                                                                    IntegerArgumentType.getInteger(param0x, "width"),
                                                                    IntegerArgumentType.getInteger(param0x, "height"),
                                                                    IntegerArgumentType.getInteger(param0x, "depth")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("clearall")
                        .executes(param0x -> clearAllTests(param0x.getSource(), 200))
                        .then(
                            Commands.argument("radius", IntegerArgumentType.integer())
                                .executes(param0x -> clearAllTests(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "radius")))
                        )
                )
        );
    }

    private static int createNewStructure(CommandSourceStack param0, String param1, int param2, int param3, int param4) {
        if (param2 <= 48 && param3 <= 48 && param4 <= 48) {
            ServerLevel var0 = param0.getLevel();
            BlockPos var1 = createTestPositionAround(param0).below();
            StructureUtils.createNewEmptyStructureBlock(param1.toLowerCase(), var1, new Vec3i(param2, param3, param4), Rotation.NONE, var0);

            for(int var2 = 0; var2 < param2; ++var2) {
                for(int var3 = 0; var3 < param4; ++var3) {
                    BlockPos var4 = new BlockPos(var1.getX() + var2, var1.getY() + 1, var1.getZ() + var3);
                    Block var5 = Blocks.POLISHED_ANDESITE;
                    BlockInput var6 = new BlockInput(var5.defaultBlockState(), Collections.emptySet(), null);
                    var6.place(var0, var4, 2);
                }
            }

            StructureUtils.addCommandBlockAndButtonToStartTest(var1, new BlockPos(1, 0, -1), Rotation.NONE, var0);
            return 0;
        } else {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
    }

    private static int showPos(CommandSourceStack param0, String param1) throws CommandSyntaxException {
        BlockHitResult var0 = (BlockHitResult)param0.getPlayerOrException().pick(10.0, 1.0F, false);
        BlockPos var1 = var0.getBlockPos();
        ServerLevel var2 = param0.getLevel();
        Optional<BlockPos> var3 = StructureUtils.findStructureBlockContainingPos(var1, 15, var2);
        if (var3.isEmpty()) {
            var3 = StructureUtils.findStructureBlockContainingPos(var1, 200, var2);
        }

        if (var3.isEmpty()) {
            param0.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + var1));
            return 0;
        } else {
            StructureBlockEntity var4 = (StructureBlockEntity)var2.getBlockEntity(var3.get());
            BlockPos var5 = var1.subtract(var3.get());
            String var6 = var5.getX() + ", " + var5.getY() + ", " + var5.getZ();
            String var7 = var4.getMetaData();
            Component var8 = Component.literal(var6)
                .setStyle(
                    Style.EMPTY
                        .withBold(true)
                        .withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + param1 + " = new BlockPos(" + var6 + ");"))
                );
            param0.sendSuccess(() -> Component.literal("Position relative to " + var7 + ": ").append(var8), false);
            DebugPackets.sendGameTestAddMarker(var2, new BlockPos(var1), var6, -2147418368, 10000);
            return 1;
        }
    }

    private static int runNearbyTest(CommandSourceStack param0, boolean param1) {
        BlockPos var0 = BlockPos.containing(param0.getPosition());
        ServerLevel var1 = param0.getLevel();
        BlockPos var2 = StructureUtils.findNearestStructureBlock(var0, 15, var1);
        if (var2 == null) {
            say(var1, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
            return 0;
        } else {
            GameTestRunner.clearMarkers(var1);
            runTest(var1, var2, null, param1);
            return 1;
        }
    }

    private static int resetNearbyTest(CommandSourceStack param0) {
        BlockPos var0 = BlockPos.containing(param0.getPosition());
        ServerLevel var1 = param0.getLevel();
        BlockPos var2 = StructureUtils.findNearestStructureBlock(var0, 15, var1);
        if (var2 == null) {
            say(var1, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
            return 0;
        } else {
            StructureBlockEntity var3 = (StructureBlockEntity)var1.getBlockEntity(var2);
            var3.placeStructure(var1);
            String var4 = var3.getMetaData();
            TestFunction var5 = GameTestRegistry.getTestFunction(var4);
            say(var1, "Reset succeded for: " + var5, ChatFormatting.GREEN);
            return 1;
        }
    }

    private static int runAllNearbyTests(CommandSourceStack param0, boolean param1) {
        BlockPos var0 = BlockPos.containing(param0.getPosition());
        ServerLevel var1 = param0.getLevel();
        Collection<BlockPos> var2 = StructureUtils.findStructureBlocks(var0, 200, var1);
        if (var2.isEmpty()) {
            say(var1, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
            return 1;
        } else {
            GameTestRunner.clearMarkers(var1);
            say(param0, "Running " + var2.size() + " tests...");
            MultipleTestTracker var3 = new MultipleTestTracker();
            var2.forEach(param3 -> runTest(var1, param3, var3, param1));
            return 1;
        }
    }

    private static void runTest(ServerLevel param0, BlockPos param1, @Nullable MultipleTestTracker param2, boolean param3) {
        StructureBlockEntity var0 = (StructureBlockEntity)param0.getBlockEntity(param1);
        String var1 = var0.getMetaData();
        Optional<TestFunction> var2 = GameTestRegistry.findTestFunction(var1);
        if (var2.isEmpty()) {
            say(param0, "Test function for test " + var1 + " could not be found", ChatFormatting.RED);
        } else {
            TestFunction var3 = var2.get();
            GameTestInfo var4 = new GameTestInfo(var3, var0.getRotation(), param0);
            var4.setRerunUntilFailed(param3);
            if (param2 != null) {
                param2.addTestToTrack(var4);
                var4.addListener(new TestCommand.TestSummaryDisplayer(param0, param2));
            }

            if (verifyStructureExists(param0, var4)) {
                runTestPreparation(var3, param0);
                BoundingBox var5 = StructureUtils.getStructureBoundingBox(var0);
                BlockPos var6 = new BlockPos(var5.minX(), var5.minY(), var5.minZ());
                GameTestRunner.runTest(var4, var6, GameTestTicker.SINGLETON);
            }
        }
    }

    private static boolean verifyStructureExists(ServerLevel param0, GameTestInfo param1) {
        if (param0.getStructureManager().get(new ResourceLocation(param1.getStructureName())).isEmpty()) {
            say(param0, "Test structure " + param1.getStructureName() + " could not be found", ChatFormatting.RED);
            return false;
        } else {
            return true;
        }
    }

    static void showTestSummaryIfAllDone(ServerLevel param0, MultipleTestTracker param1) {
        if (param1.isDone()) {
            say(param0, "GameTest done! " + param1.getTotalCount() + " tests were run", ChatFormatting.WHITE);
            if (param1.hasFailedRequired()) {
                say(param0, param1.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
            } else {
                say(param0, "All required tests passed :)", ChatFormatting.GREEN);
            }

            if (param1.hasFailedOptional()) {
                say(param0, param1.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
            }
        }

    }

    private static int clearAllTests(CommandSourceStack param0, int param1) {
        ServerLevel var0 = param0.getLevel();
        GameTestRunner.clearMarkers(var0);
        BlockPos var1 = BlockPos.containing(
            param0.getPosition().x,
            (double)param0.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos.containing(param0.getPosition())).getY(),
            param0.getPosition().z
        );
        GameTestRunner.clearAllTests(var0, var1, GameTestTicker.SINGLETON, Mth.clamp(param1, 0, 1024));
        return 1;
    }

    private static int runTest(CommandSourceStack param0, TestFunction param1, int param2) {
        ServerLevel var0 = param0.getLevel();
        BlockPos var1 = createTestPositionAround(param0);
        GameTestRunner.clearMarkers(var0);
        runTestPreparation(param1, var0);
        Rotation var2 = StructureUtils.getRotationForRotationSteps(param2);
        GameTestInfo var3 = new GameTestInfo(param1, var2, var0);
        if (!verifyStructureExists(var0, var3)) {
            return 0;
        } else {
            GameTestRunner.runTest(var3, var1, GameTestTicker.SINGLETON);
            return 1;
        }
    }

    private static BlockPos createTestPositionAround(CommandSourceStack param0) {
        BlockPos var0 = BlockPos.containing(param0.getPosition());
        int var1 = param0.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, var0).getY();
        return new BlockPos(var0.getX(), var1 + 1, var0.getZ() + 3);
    }

    private static void runTestPreparation(TestFunction param0, ServerLevel param1) {
        Consumer<ServerLevel> var0 = GameTestRegistry.getBeforeBatchFunction(param0.getBatchName());
        if (var0 != null) {
            var0.accept(param1);
        }

    }

    private static int runAllTests(CommandSourceStack param0, int param1, int param2) {
        GameTestRunner.clearMarkers(param0.getLevel());
        Collection<TestFunction> var0 = GameTestRegistry.getAllTestFunctions();
        say(param0, "Running all " + var0.size() + " tests...");
        GameTestRegistry.forgetFailedTests();
        runTests(param0, var0, param1, param2);
        return 1;
    }

    private static int runAllTestsInClass(CommandSourceStack param0, String param1, int param2, int param3) {
        Collection<TestFunction> var0 = GameTestRegistry.getTestFunctionsForClassName(param1);
        GameTestRunner.clearMarkers(param0.getLevel());
        say(param0, "Running " + var0.size() + " tests from " + param1 + "...");
        GameTestRegistry.forgetFailedTests();
        runTests(param0, var0, param2, param3);
        return 1;
    }

    private static int runLastFailedTests(CommandSourceStack param0, boolean param1, int param2, int param3) {
        Collection<TestFunction> var0;
        if (param1) {
            var0 = GameTestRegistry.getLastFailedTests().stream().filter(TestFunction::isRequired).collect(Collectors.toList());
        } else {
            var0 = GameTestRegistry.getLastFailedTests();
        }

        if (var0.isEmpty()) {
            say(param0, "No failed tests to rerun");
            return 0;
        } else {
            GameTestRunner.clearMarkers(param0.getLevel());
            say(param0, "Rerunning " + var0.size() + " failed tests (" + (param1 ? "only required tests" : "including optional tests") + ")");
            runTests(param0, var0, param2, param3);
            return 1;
        }
    }

    private static void runTests(CommandSourceStack param0, Collection<TestFunction> param1, int param2, int param3) {
        BlockPos var0 = createTestPositionAround(param0);
        ServerLevel var1 = param0.getLevel();
        Rotation var2 = StructureUtils.getRotationForRotationSteps(param2);
        Collection<GameTestInfo> var3 = GameTestRunner.runTests(param1, var0, var2, var1, GameTestTicker.SINGLETON, param3);
        MultipleTestTracker var4 = new MultipleTestTracker(var3);
        var4.addListener(new TestCommand.TestSummaryDisplayer(var1, var4));
        var4.addFailureListener(param0x -> GameTestRegistry.rememberFailedTest(param0x.getTestFunction()));
    }

    private static void say(CommandSourceStack param0, String param1) {
        param0.sendSuccess(() -> Component.literal(param1), false);
    }

    private static int exportNearestTestStructure(CommandSourceStack param0) {
        BlockPos var0 = BlockPos.containing(param0.getPosition());
        ServerLevel var1 = param0.getLevel();
        BlockPos var2 = StructureUtils.findNearestStructureBlock(var0, 15, var1);
        if (var2 == null) {
            say(var1, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
            return 0;
        } else {
            StructureBlockEntity var3 = (StructureBlockEntity)var1.getBlockEntity(var2);
            return saveAndExportTestStructure(param0, var3);
        }
    }

    private static int exportAllNearbyTests(CommandSourceStack param0) {
        BlockPos var0 = BlockPos.containing(param0.getPosition());
        ServerLevel var1 = param0.getLevel();
        Collection<BlockPos> var2 = StructureUtils.findStructureBlocks(var0, 200, var1);
        if (var2.isEmpty()) {
            say(var1, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
            return 1;
        } else {
            boolean var3 = true;

            for(BlockPos var4 : var2) {
                StructureBlockEntity var5 = (StructureBlockEntity)var1.getBlockEntity(var4);
                if (saveAndExportTestStructure(param0, var5) != 0) {
                    var3 = false;
                }
            }

            return var3 ? 0 : 1;
        }
    }

    private static int saveAndExportTestStructure(CommandSourceStack param0, StructureBlockEntity param1) {
        String var0 = param1.getStructureName();
        if (!param1.saveStructure(true)) {
            say(param0, "Failed to save structure " + var0);
        }

        return exportTestStructure(param0, var0);
    }

    private static int exportTestStructure(CommandSourceStack param0, String param1) {
        Path var0 = Paths.get(StructureUtils.testStructuresDir);
        ResourceLocation var1 = new ResourceLocation(param1);
        Path var2 = param0.getLevel().getStructureManager().getPathToGeneratedStructure(var1, ".nbt");
        Path var3 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, var2, param1, var0);
        if (var3 == null) {
            say(param0, "Failed to export " + var2);
            return 1;
        } else {
            try {
                FileUtil.createDirectoriesSafe(var3.getParent());
            } catch (IOException var7) {
                say(param0, "Could not create folder " + var3.getParent());
                LOGGER.error("Could not create export folder", (Throwable)var7);
                return 1;
            }

            say(param0, "Exported " + param1 + " to " + var3.toAbsolutePath());
            return 0;
        }
    }

    private static int importTestStructure(CommandSourceStack param0, String param1) {
        Path var0 = Paths.get(StructureUtils.testStructuresDir, param1 + ".snbt");
        ResourceLocation var1 = new ResourceLocation(param1);
        Path var2 = param0.getLevel().getStructureManager().getPathToGeneratedStructure(var1, ".nbt");

        try {
            BufferedReader var3 = Files.newBufferedReader(var0);
            String var4 = IOUtils.toString((Reader)var3);
            Files.createDirectories(var2.getParent());

            try (OutputStream var5 = Files.newOutputStream(var2)) {
                NbtIo.writeCompressed(NbtUtils.snbtToStructure(var4), var5);
            }

            say(param0, "Imported to " + var2.toAbsolutePath());
            return 0;
        } catch (CommandSyntaxException | IOException var12) {
            LOGGER.error("Failed to load structure {}", param1, var12);
            return 1;
        }
    }

    private static void say(ServerLevel param0, String param1, ChatFormatting param2) {
        param0.getPlayers(param0x -> true).forEach(param2x -> param2x.sendSystemMessage(Component.literal(param1).withStyle(param2)));
    }

    static class TestSummaryDisplayer implements GameTestListener {
        private final ServerLevel level;
        private final MultipleTestTracker tracker;

        public TestSummaryDisplayer(ServerLevel param0, MultipleTestTracker param1) {
            this.level = param0;
            this.tracker = param1;
        }

        @Override
        public void testStructureLoaded(GameTestInfo param0) {
        }

        @Override
        public void testPassed(GameTestInfo param0) {
            TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testFailed(GameTestInfo param0) {
            TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
        }
    }
}
