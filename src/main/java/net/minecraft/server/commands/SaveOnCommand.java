package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class SaveOnCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_ON = new SimpleCommandExceptionType(new TranslatableComponent("commands.save.alreadyOn"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("save-on").requires(param0x -> param0x.hasPermission(4)).executes(param0x -> {
            CommandSourceStack var0x = param0x.getSource();
            boolean var1 = false;

            for(ServerLevel var2 : var0x.getServer().getAllLevels()) {
                if (var2 != null && var2.noSave) {
                    var2.noSave = false;
                    var1 = true;
                }
            }

            if (!var1) {
                throw ERROR_ALREADY_ON.create();
            } else {
                var0x.sendSuccess(new TranslatableComponent("commands.save.enabled"), true);
                return 1;
            }
        }));
    }
}
