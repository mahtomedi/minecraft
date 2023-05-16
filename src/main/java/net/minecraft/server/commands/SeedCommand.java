package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class SeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0, boolean param1) {
        param0.register(Commands.literal("seed").requires(param1x -> !param1 || param1x.hasPermission(2)).executes(param0x -> {
            long var0x = param0x.getSource().getLevel().getSeed();
            Component var1x = ComponentUtils.copyOnClickText(String.valueOf(var0x));
            param0x.getSource().sendSuccess(() -> Component.translatable("commands.seed.success", var1x), false);
            return (int)var0x;
        }));
    }
}
