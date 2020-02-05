package net.minecraft.server.commands;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugCommand {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(new TranslatableComponent("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.debug.alreadyRunning")
    );
    @Nullable
    private static final FileSystemProvider ZIP_FS_PROVIDER = FileSystemProvider.installedProviders()
        .stream()
        .filter(param0 -> param0.getScheme().equalsIgnoreCase("jar"))
        .findFirst()
        .orElse(null);

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("debug")
                .requires(param0x -> param0x.hasPermission(3))
                .then(Commands.literal("start").executes(param0x -> start(param0x.getSource())))
                .then(Commands.literal("stop").executes(param0x -> stop(param0x.getSource())))
                .then(Commands.literal("report").executes(param0x -> report(param0x.getSource())))
        );
    }

    private static int start(CommandSourceStack param0) throws CommandSyntaxException {
        MinecraftServer var0 = param0.getServer();
        if (var0.isProfiling()) {
            throw ERROR_ALREADY_RUNNING.create();
        } else {
            var0.startProfiling();
            param0.sendSuccess(new TranslatableComponent("commands.debug.started", "Started the debug profiler. Type '/debug stop' to stop it."), true);
            return 0;
        }
    }

    private static int stop(CommandSourceStack param0) throws CommandSyntaxException {
        MinecraftServer var0 = param0.getServer();
        if (!var0.isProfiling()) {
            throw ERROR_NOT_RUNNING.create();
        } else {
            ProfileResults var1 = var0.finishProfiling();
            File var2 = new File(var0.getFile("debug"), "profile-results-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt");
            var1.saveResults(var2);
            float var3 = (float)var1.getNanoDuration() / 1.0E9F;
            float var4 = (float)var1.getTickDuration() / var3;
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.debug.stopped", String.format(Locale.ROOT, "%.2f", var3), var1.getTickDuration(), String.format("%.2f", var4)
                ),
                true
            );
            return Mth.floor(var4);
        }
    }

    private static int report(CommandSourceStack param0) {
        MinecraftServer var0 = param0.getServer();
        String var1 = "debug-report-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());

        try {
            Path var2 = var0.getFile("debug").toPath();
            Files.createDirectories(var2);
            if (!SharedConstants.IS_RUNNING_IN_IDE && ZIP_FS_PROVIDER != null) {
                Path var4 = var2.resolve(var1 + ".zip");

                try (FileSystem var5 = ZIP_FS_PROVIDER.newFileSystem(var4, ImmutableMap.of("create", "true"))) {
                    var0.saveDebugReport(var5.getPath("/"));
                }
            } else {
                Path var3 = var2.resolve(var1);
                var0.saveDebugReport(var3);
            }

            param0.sendSuccess(new TranslatableComponent("commands.debug.reportSaved", var1), false);
            return 1;
        } catch (IOException var18) {
            LOGGER.error("Failed to save debug dump", (Throwable)var18);
            param0.sendFailure(new TranslatableComponent("commands.debug.reportFailed"));
            return 0;
        }
    }
}
