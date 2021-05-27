package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugCommand {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(new TranslatableComponent("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.debug.alreadyRunning")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("debug")
                .requires(param0x -> param0x.hasPermission(3))
                .then(Commands.literal("start").executes(param0x -> start(param0x.getSource())))
                .then(Commands.literal("stop").executes(param0x -> stop(param0x.getSource())))
                .then(
                    Commands.literal("function")
                        .requires(param0x -> param0x.hasPermission(3))
                        .then(
                            Commands.argument("name", FunctionArgument.functions())
                                .suggests(FunctionCommand.SUGGEST_FUNCTION)
                                .executes(param0x -> traceFunction(param0x.getSource(), FunctionArgument.getFunctions(param0x, "name")))
                        )
                )
        );
    }

    private static int start(CommandSourceStack param0) throws CommandSyntaxException {
        MinecraftServer var0 = param0.getServer();
        if (var0.isTimeProfilerRunning()) {
            throw ERROR_ALREADY_RUNNING.create();
        } else {
            var0.startTimeProfiler();
            param0.sendSuccess(new TranslatableComponent("commands.debug.started"), true);
            return 0;
        }
    }

    private static int stop(CommandSourceStack param0) throws CommandSyntaxException {
        MinecraftServer var0 = param0.getServer();
        if (!var0.isTimeProfilerRunning()) {
            throw ERROR_NOT_RUNNING.create();
        } else {
            ProfileResults var1 = var0.stopTimeProfiler();
            double var2 = (double)var1.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
            double var3 = (double)var1.getTickDuration() / var2;
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.debug.stopped", String.format(Locale.ROOT, "%.2f", var2), var1.getTickDuration(), String.format("%.2f", var3)
                ),
                true
            );
            return (int)var3;
        }
    }

    private static int traceFunction(CommandSourceStack param0, Collection<CommandFunction> param1) {
        int var0 = 0;
        MinecraftServer var1 = param0.getServer();
        String var2 = "debug-trace-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt";

        try {
            Path var3 = var1.getFile("debug").toPath();
            Files.createDirectories(var3);

            try (Writer var4 = Files.newBufferedWriter(var3.resolve(var2), StandardCharsets.UTF_8)) {
                PrintWriter var5 = new PrintWriter(var4);

                for(CommandFunction var6 : param1) {
                    var5.println(var6.getId());
                    DebugCommand.Tracer var7 = new DebugCommand.Tracer(var5);
                    var0 += param0.getServer().getFunctions().execute(var6, param0.withSource(var7).withMaximumPermission(2), var7);
                }
            }
        } catch (IOException | UncheckedIOException var13) {
            LOGGER.warn("Tracing failed", (Throwable)var13);
            param0.sendFailure(new TranslatableComponent("commands.debug.function.traceFailed"));
        }

        if (param1.size() == 1) {
            param0.sendSuccess(new TranslatableComponent("commands.debug.function.success.single", var0, param1.iterator().next().getId(), var2), true);
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.debug.function.success.multiple", var0, param1.size(), var2), true);
        }

        return var0;
    }

    static class Tracer implements CommandSource, ServerFunctionManager.TraceCallbacks {
        public static final int INDENT_OFFSET = 1;
        private final PrintWriter output;
        private int lastIndent;
        private boolean waitingForResult;

        Tracer(PrintWriter param0) {
            this.output = param0;
        }

        private void indentAndSave(int param0) {
            this.printIndent(param0);
            this.lastIndent = param0;
        }

        private void printIndent(int param0) {
            for(int var0 = 0; var0 < param0 + 1; ++var0) {
                this.output.write("    ");
            }

        }

        private void newLine() {
            if (this.waitingForResult) {
                this.output.println();
                this.waitingForResult = false;
            }

        }

        @Override
        public void onCommand(int param0, String param1) {
            this.newLine();
            this.indentAndSave(param0);
            this.output.print("[C] ");
            this.output.print(param1);
            this.waitingForResult = true;
        }

        @Override
        public void onReturn(int param0, String param1, int param2) {
            if (this.waitingForResult) {
                this.output.print(" -> ");
                this.output.println(param2);
                this.waitingForResult = false;
            } else {
                this.indentAndSave(param0);
                this.output.print("[R = ");
                this.output.print(param2);
                this.output.print("] ");
                this.output.println(param1);
            }

        }

        @Override
        public void onCall(int param0, ResourceLocation param1, int param2) {
            this.newLine();
            this.indentAndSave(param0);
            this.output.print("[F] ");
            this.output.print(param1);
            this.output.print(" size=");
            this.output.println(param2);
        }

        @Override
        public void onError(int param0, String param1) {
            this.newLine();
            this.indentAndSave(param0 + 1);
            this.output.print("[E] ");
            this.output.print(param1);
        }

        @Override
        public void sendMessage(Component param0, UUID param1) {
            this.newLine();
            this.printIndent(this.lastIndent + 1);
            this.output.print("[M] ");
            if (param1 != Util.NIL_UUID) {
                this.output.print(param1);
                this.output.print(": ");
            }

            this.output.println(param0.getString());
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        @Override
        public boolean alwaysAccepts() {
            return true;
        }
    }
}
