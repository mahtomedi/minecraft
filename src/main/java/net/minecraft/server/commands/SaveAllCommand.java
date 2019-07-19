package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;

public class SaveAllCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.save.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("save-all")
                .requires(param0x -> param0x.hasPermission(4))
                .executes(param0x -> saveAll(param0x.getSource(), false))
                .then(Commands.literal("flush").executes(param0x -> saveAll(param0x.getSource(), true)))
        );
    }

    private static int saveAll(CommandSourceStack param0, boolean param1) throws CommandSyntaxException {
        param0.sendSuccess(new TranslatableComponent("commands.save.saving"), false);
        MinecraftServer var0 = param0.getServer();
        var0.getPlayerList().saveAll();
        boolean var1 = var0.saveAllChunks(true, param1, true);
        if (!var1) {
            throw ERROR_FAILED.create();
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.save.success"), true);
            return 1;
        }
    }
}
