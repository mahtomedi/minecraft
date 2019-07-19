package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class StopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("stop").requires(param0x -> param0x.hasPermission(4)).executes(param0x -> {
            param0x.getSource().sendSuccess(new TranslatableComponent("commands.stop.stopping"), true);
            param0x.getSource().getServer().halt(false);
            return 1;
        }));
    }
}
