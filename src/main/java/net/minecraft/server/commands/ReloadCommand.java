package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class ReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("reload").requires(param0x -> param0x.hasPermission(2)).executes(param0x -> {
            param0x.getSource().sendSuccess(new TranslatableComponent("commands.reload.success"), true);
            param0x.getSource().getServer().reloadResources();
            return 0;
        }));
    }
}
