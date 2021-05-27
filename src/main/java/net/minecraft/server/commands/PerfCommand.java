package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileZipper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PerfCommand {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(new TranslatableComponent("commands.perf.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.perf.alreadyRunning")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("perf")
                .requires(param0x -> param0x.hasPermission(4))
                .then(Commands.literal("start").executes(param0x -> startProfilingDedicatedServer(param0x.getSource())))
                .then(Commands.literal("stop").executes(param0x -> stopProfilingDedicatedServer(param0x.getSource())))
        );
    }

    private static int startProfilingDedicatedServer(CommandSourceStack param0) throws CommandSyntaxException {
        MinecraftServer var0 = param0.getServer();
        if (var0.isRecordingMetrics()) {
            throw ERROR_ALREADY_RUNNING.create();
        } else {
            Consumer<ProfileResults> var1 = param1 -> whenStopped(param0, param1);
            Consumer<Path> var2 = param2 -> saveResults(param0, param2, var0);
            var0.startRecordingMetrics(var1, var2);
            param0.sendSuccess(new TranslatableComponent("commands.perf.started"), false);
            return 0;
        }
    }

    private static int stopProfilingDedicatedServer(CommandSourceStack param0) throws CommandSyntaxException {
        MinecraftServer var0 = param0.getServer();
        if (!var0.isRecordingMetrics()) {
            throw ERROR_NOT_RUNNING.create();
        } else {
            var0.finishRecordingMetrics();
            return 0;
        }
    }

    private static void saveResults(CommandSourceStack param0, Path param1, MinecraftServer param2) {
        String var0 = String.format(
            "%s-%s-%s",
            new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()),
            param2.getWorldData().getLevelName(),
            SharedConstants.getCurrentVersion().getId()
        );

        String var1;
        try {
            var1 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, var0, ".zip");
        } catch (IOException var11) {
            param0.sendFailure(new TranslatableComponent("commands.perf.reportFailed"));
            LOGGER.error(var11);
            return;
        }

        try (FileZipper var4 = new FileZipper(MetricsPersister.PROFILING_RESULTS_DIR.resolve(var1))) {
            var4.add(Paths.get("system.txt"), param2.fillSystemReport(new SystemReport()).toLineSeparatedString());
            var4.add(param1);
        }

        try {
            FileUtils.forceDelete(param1.toFile());
        } catch (IOException var9) {
            LOGGER.warn("Failed to delete temporary profiling file {}", param1, var9);
        }

        param0.sendSuccess(new TranslatableComponent("commands.perf.reportSaved", var1), false);
    }

    private static void whenStopped(CommandSourceStack param0, ProfileResults param1) {
        int var0 = param1.getTickDuration();
        double var1 = (double)param1.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
        param0.sendSuccess(
            new TranslatableComponent(
                "commands.perf.stopped", String.format(Locale.ROOT, "%.2f", var1), var0, String.format(Locale.ROOT, "%.2f", (double)var0 / var1)
            ),
            false
        );
    }
}
