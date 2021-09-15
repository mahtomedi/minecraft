package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.profiling.jfr.JfrRecording;

public class JfrCommand {
    private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.jfr.start.failed"));
    private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.jfr.dump.failed", param0)
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
        JfrRecording.Environment var0 = JfrRecording.Environment.from(param0.getServer());
        if (!JfrRecording.start(var0)) {
            throw START_FAILED.create();
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.jfr.started"), false);
            return 1;
        }
    }

    private static int stopJfr(CommandSourceStack param0) throws CommandSyntaxException {
        try {
            Path var0 = JfrRecording.stop();
            param0.sendSuccess(new TranslatableComponent("commands.jfr.stopped", var0), false);
            return 1;
        } catch (Throwable var2) {
            throw DUMP_FAILED.create(var2.getMessage());
        }
    }
}
