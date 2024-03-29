package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class SaveOffCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_OFF = new SimpleCommandExceptionType(Component.translatable("commands.save.alreadyOff"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("save-off").requires(param0x -> param0x.hasPermission(4)).executes(param0x -> {
            CommandSourceStack var0x = param0x.getSource();
            boolean var1 = false;

            for(ServerLevel var2 : var0x.getServer().getAllLevels()) {
                if (var2 != null && !var2.noSave) {
                    var2.noSave = true;
                    var1 = true;
                }
            }

            if (!var1) {
                throw ERROR_ALREADY_OFF.create();
            } else {
                var0x.sendSuccess(() -> Component.translatable("commands.save.disabled"), true);
                return 1;
            }
        }));
    }
}
