package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;

public class PublishCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.publish.failed"));
    private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.publish.alreadyPublished", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("publish")
                .requires(param0x -> param0x.getServer().isSingleplayer() && param0x.hasPermission(4))
                .executes(param0x -> publish(param0x.getSource(), HttpUtil.getAvailablePort()))
                .then(
                    Commands.argument("port", IntegerArgumentType.integer(0, 65535))
                        .executes(param0x -> publish(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "port")))
                )
        );
    }

    private static int publish(CommandSourceStack param0, int param1) throws CommandSyntaxException {
        if (param0.getServer().isPublished()) {
            throw ERROR_ALREADY_PUBLISHED.create(param0.getServer().getPort());
        } else if (!param0.getServer().publishServer(param0.getServer().getDefaultGameType(), false, param1)) {
            throw ERROR_FAILED.create();
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.publish.success", param1), true);
            return param1;
        }
    }
}
