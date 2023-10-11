package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class DebugCommand {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(
        Component.translatable("commands.debug.alreadyRunning")
    );
    static final SimpleCommandExceptionType NO_RECURSIVE_TRACES = new SimpleCommandExceptionType(Component.translatable("commands.debug.function.noRecursion"));

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
                                .executes(new DebugCommand.TraceCustomExecutor())
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
            param0.sendSuccess(() -> Component.translatable("commands.debug.started"), true);
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
                () -> Component.translatable(
                        "commands.debug.stopped", String.format(Locale.ROOT, "%.2f", var2), var1.getTickDuration(), String.format(Locale.ROOT, "%.2f", var3)
                    ),
                true
            );
            return (int)var3;
        }
    }

    static class TraceCustomExecutor
        extends CustomCommandExecutor.WithErrorHandling<CommandSourceStack>
        implements CustomCommandExecutor.CommandAdapter<CommandSourceStack> {
        public void runGuarded(CommandSourceStack param0, ContextChain<CommandSourceStack> param1, boolean param2, ExecutionControl<CommandSourceStack> param3) throws CommandSyntaxException {
            if (param3.tracer() != null) {
                throw DebugCommand.NO_RECURSIVE_TRACES.create();
            } else {
                CommandContext<CommandSourceStack> var0 = param1.getTopContext();
                Collection<CommandFunction<CommandSourceStack>> var1 = FunctionArgument.getFunctions(var0, "name");
                MinecraftServer var2 = param0.getServer();
                String var3 = "debug-trace-" + Util.getFilenameFormattedDateTime() + ".txt";
                CommandDispatcher<CommandSourceStack> var4 = param0.getServer().getFunctions().getDispatcher();
                int var5 = 0;

                try {
                    Path var6 = var2.getFile("debug").toPath();
                    Files.createDirectories(var6);
                    final PrintWriter var7 = new PrintWriter(Files.newBufferedWriter(var6.resolve(var3), StandardCharsets.UTF_8));
                    DebugCommand.Tracer var8 = new DebugCommand.Tracer(var7);
                    param3.tracer(var8);

                    for(final CommandFunction<CommandSourceStack> var9 : var1) {
                        try {
                            CommandSourceStack var10 = param0.withSource(var8).withMaximumPermission(2);
                            InstantiatedFunction<CommandSourceStack> var11 = var9.instantiate(null, var4, var10);
                            param3.queueNext((new CallFunction<CommandSourceStack>(var11) {
                                public void execute(CommandSourceStack param0, ExecutionContext<CommandSourceStack> param1, int param2) {
                                    var7.println(var9.id());
                                    super.execute(param0, param1, param2);
                                }
                            }).bind(var10));
                            var5 += var11.entries().size();
                        } catch (FunctionInstantiationException var18) {
                            param0.sendFailure(var18.messageComponent());
                        }
                    }
                } catch (IOException | UncheckedIOException var19) {
                    DebugCommand.LOGGER.warn("Tracing failed", (Throwable)var19);
                    param0.sendFailure(Component.translatable("commands.debug.function.traceFailed"));
                }

                int var14 = var5;
                param3.queueNext(
                    (param4, param5) -> {
                        if (var1.size() == 1) {
                            param0.sendSuccess(
                                () -> Component.translatable(
                                        "commands.debug.function.success.single", var14, Component.translationArg(var1.iterator().next().id()), var3
                                    ),
                                true
                            );
                        } else {
                            param0.sendSuccess(() -> Component.translatable("commands.debug.function.success.multiple", var14, var1.size(), var3), true);
                        }
    
                    }
                );
            }
        }

        protected void onError(CommandSyntaxException param0, CommandSourceStack param1, boolean param2) {
            if (!param2) {
                param1.sendFailure(ComponentUtils.fromMessage(param0.getRawMessage()));
            }

        }
    }

    static class Tracer implements CommandSource, TraceCallbacks {
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
        public void sendSystemMessage(Component param0) {
            this.newLine();
            this.printIndent(this.lastIndent + 1);
            this.output.print("[M] ");
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

        @Override
        public void close() {
            IOUtils.closeQuietly((Writer)this.output);
        }
    }
}
