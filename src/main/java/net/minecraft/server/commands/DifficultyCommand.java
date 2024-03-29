package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {
    private static final DynamicCommandExceptionType ERROR_ALREADY_DIFFICULT = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("commands.difficulty.failure", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("difficulty");

        for(Difficulty var1 : Difficulty.values()) {
            var0.then(Commands.literal(var1.getKey()).executes(param1 -> setDifficulty(param1.getSource(), var1)));
        }

        param0.register(var0.requires(param0x -> param0x.hasPermission(2)).executes(param0x -> {
            Difficulty var0x = param0x.getSource().getLevel().getDifficulty();
            param0x.getSource().sendSuccess(() -> Component.translatable("commands.difficulty.query", var0x.getDisplayName()), false);
            return var0x.getId();
        }));
    }

    public static int setDifficulty(CommandSourceStack param0, Difficulty param1) throws CommandSyntaxException {
        MinecraftServer var0 = param0.getServer();
        if (var0.getWorldData().getDifficulty() == param1) {
            throw ERROR_ALREADY_DIFFICULT.create(param1.getKey());
        } else {
            var0.setDifficulty(param1, true);
            param0.sendSuccess(() -> Component.translatable("commands.difficulty.success", param1.getDisplayName()), true);
            return 0;
        }
    }
}
