package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;

public class JfrCommand {
    private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.jfr.start.failed"));
    private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.jfr.dump.failed", param0)
    );

    private JfrCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("jfr")
                .requires(param0x -> param0x.hasPermission(4))
                .then(Commands.literal("start").executes(param0x -> startJfr(param0x.getSource())))
                .then(Commands.literal("stop").executes(param0x -> stopJfr(param0x.getSource())))
        );
    }

    private static int startJfr(CommandSourceStack param0) throws CommandSyntaxException {
        Environment var0 = Environment.from(param0.getServer());
        if (!JvmProfiler.INSTANCE.start(var0)) {
            throw START_FAILED.create();
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.jfr.started"), false);
            return 1;
        }
    }

    private static int stopJfr(CommandSourceStack param0) throws CommandSyntaxException {
        try {
            Path var0 = Paths.get(".").relativize(JvmProfiler.INSTANCE.stop().normalize());
            Path var1 = param0.getServer().isPublished() && !SharedConstants.IS_RUNNING_IN_IDE ? var0 : var0.toAbsolutePath();
            Component var2 = Component.literal(var0.toString())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(
                    param1 -> param1.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, var1.toString()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                );
            param0.sendSuccess(() -> Component.translatable("commands.jfr.stopped", var2), false);
            return 1;
        } catch (Throwable var4) {
            throw DUMP_FAILED.create(var4.getMessage());
        }
    }
}
