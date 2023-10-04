package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestListener {
    private final GameTestInfo originalTestInfo;
    private final GameTestTicker testTicker;
    private final BlockPos structurePos;
    int attempts;
    int successes;

    public ReportGameListener(GameTestInfo param0, GameTestTicker param1, BlockPos param2) {
        this.originalTestInfo = param0;
        this.testTicker = param1;
        this.structurePos = param2;
        this.attempts = 0;
        this.successes = 0;
    }

    @Override
    public void testStructureLoaded(GameTestInfo param0) {
        spawnBeacon(this.originalTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
        ++this.attempts;
    }

    @Override
    public void testPassed(GameTestInfo param0) {
        ++this.successes;
        if (param0.rerunUntilFailed()) {
            reportPassed(param0, param0.getTestName() + " passed! (" + param0.getRunTime() + "ms). Rerunning until failed.");
            this.rerunTest();
        } else if (!param0.isFlaky()) {
            reportPassed(param0, param0.getTestName() + " passed! (" + param0.getRunTime() + "ms)");
        } else {
            if (this.successes >= param0.requiredSuccesses()) {
                reportPassed(param0, param0 + " passed " + this.successes + " times of " + this.attempts + " attempts.");
            } else {
                say(
                    this.originalTestInfo.getLevel(),
                    ChatFormatting.GREEN,
                    "Flaky test " + this.originalTestInfo + " succeeded, attempt: " + this.attempts + " successes: " + this.successes
                );
                this.rerunTest();
            }

        }
    }

    @Override
    public void testFailed(GameTestInfo param0) {
        if (!param0.isFlaky()) {
            reportFailure(param0, param0.getError());
        } else {
            TestFunction var0 = this.originalTestInfo.getTestFunction();
            String var1 = "Flaky test " + this.originalTestInfo + " failed, attempt: " + this.attempts + "/" + var0.getMaxAttempts();
            if (var0.getRequiredSuccesses() > 1) {
                var1 = var1 + ", successes: " + this.successes + " (" + var0.getRequiredSuccesses() + " required)";
            }

            say(this.originalTestInfo.getLevel(), ChatFormatting.YELLOW, var1);
            if (param0.maxAttempts() - this.attempts + this.successes >= param0.requiredSuccesses()) {
                this.rerunTest();
            } else {
                reportFailure(param0, new ExhaustedAttemptsException(this.attempts, this.successes, param0));
            }

        }
    }

    public static void reportPassed(GameTestInfo param0, String param1) {
        spawnBeacon(param0, Blocks.LIME_STAINED_GLASS);
        visualizePassedTest(param0, param1);
    }

    private static void visualizePassedTest(GameTestInfo param0, String param1) {
        say(param0.getLevel(), ChatFormatting.GREEN, param1);
        GlobalTestReporter.onTestSuccess(param0);
    }

    protected static void reportFailure(GameTestInfo param0, Throwable param1) {
        spawnBeacon(param0, param0.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
        spawnLectern(param0, Util.describeError(param1));
        visualizeFailedTest(param0, param1);
    }

    protected static void visualizeFailedTest(GameTestInfo param0, Throwable param1) {
        String var0 = param1.getMessage() + (param1.getCause() == null ? "" : " cause: " + Util.describeError(param1.getCause()));
        String var1 = (param0.isRequired() ? "" : "(optional) ") + param0.getTestName() + " failed! " + var0;
        say(param0.getLevel(), param0.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, var1);
        Throwable var2 = MoreObjects.firstNonNull(ExceptionUtils.getRootCause(param1), param1);
        if (var2 instanceof GameTestAssertPosException var3) {
            showRedBox(param0.getLevel(), var3.getAbsolutePos(), var3.getMessageToShowAtBlock());
        }

        GlobalTestReporter.onTestFailed(param0);
    }

    private void rerunTest() {
        this.originalTestInfo.clearStructure();
        GameTestInfo var0 = new GameTestInfo(this.originalTestInfo.getTestFunction(), this.originalTestInfo.getRotation(), this.originalTestInfo.getLevel());
        var0.setRerunUntilFailed(this.originalTestInfo.rerunUntilFailed());
        var0.startExecution();
        this.testTicker.add(var0);
        var0.addListener(this);
        var0.spawnStructure(this.structurePos, 2);
    }

    protected static void spawnBeacon(GameTestInfo param0, Block param1) {
        ServerLevel var0 = param0.getLevel();
        BlockPos var1 = param0.getStructureBlockPos();
        BlockPos var2 = new BlockPos(-1, -1, -1);
        BlockPos var3 = StructureTemplate.transform(var1.offset(var2), Mirror.NONE, param0.getRotation(), var1);
        var0.setBlockAndUpdate(var3, Blocks.BEACON.defaultBlockState().rotate(param0.getRotation()));
        BlockPos var4 = var3.offset(0, 1, 0);
        var0.setBlockAndUpdate(var4, param1.defaultBlockState());

        for(int var5 = -1; var5 <= 1; ++var5) {
            for(int var6 = -1; var6 <= 1; ++var6) {
                BlockPos var7 = var3.offset(var5, -1, var6);
                var0.setBlockAndUpdate(var7, Blocks.IRON_BLOCK.defaultBlockState());
            }
        }

    }

    private static void spawnLectern(GameTestInfo param0, String param1) {
        ServerLevel var0 = param0.getLevel();
        BlockPos var1 = param0.getStructureBlockPos();
        BlockPos var2 = new BlockPos(-1, 1, -1);
        BlockPos var3 = StructureTemplate.transform(var1.offset(var2), Mirror.NONE, param0.getRotation(), var1);
        var0.setBlockAndUpdate(var3, Blocks.LECTERN.defaultBlockState().rotate(param0.getRotation()));
        BlockState var4 = var0.getBlockState(var3);
        ItemStack var5 = createBook(param0.getTestName(), param0.isRequired(), param1);
        LecternBlock.tryPlaceBook(null, var0, var3, var4, var5);
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
        var1.add(StringTag.valueOf(var2 + param2));
        var0.addTagElement("pages", var1);
        return var0;
    }

    protected static void say(ServerLevel param0, ChatFormatting param1, String param2) {
        param0.getPlayers(param0x -> true).forEach(param2x -> param2x.sendSystemMessage(Component.literal(param2).withStyle(param1)));
    }

    private static void showRedBox(ServerLevel param0, BlockPos param1, String param2) {
        DebugPackets.sendGameTestAddMarker(param0, param1, param2, -2130771968, Integer.MAX_VALUE);
    }
}
