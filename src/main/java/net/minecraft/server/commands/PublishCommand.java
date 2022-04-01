package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GamemodeArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class PublishCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.publish.failed"));
    private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.publish.alreadyPublished", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("publish")
                .requires(param0x -> param0x.hasPermission(4))
                .executes(param0x -> publish(param0x.getSource(), HttpUtil.getAvailablePort(), false, null))
                .then(
                    Commands.argument("port", IntegerArgumentType.integer(0, 65535))
                        .executes(param0x -> publish(param0x.getSource(), IntegerArgumentType.getInteger(param0x, "port"), false, null))
                        .then(
                            Commands.argument("allowCommands", BoolArgumentType.bool())
                                .executes(
                                    param0x -> publish(
                                            param0x.getSource(),
                                            IntegerArgumentType.getInteger(param0x, "port"),
                                            BoolArgumentType.getBool(param0x, "allowCommands"),
                                            null
                                        )
                                )
                                .then(
                                    Commands.argument("gamemode", GamemodeArgument.gamemode())
                                        .executes(
                                            param0x -> publish(
                                                    param0x.getSource(),
                                                    IntegerArgumentType.getInteger(param0x, "port"),
                                                    BoolArgumentType.getBool(param0x, "allowCommands"),
                                                    GamemodeArgument.getGamemode(param0x, "gamemode")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int publish(CommandSourceStack param0, int param1, boolean param2, @Nullable GameType param3) throws CommandSyntaxException {
        if (param0.getServer().isPublished()) {
            throw ERROR_ALREADY_PUBLISHED.create(param0.getServer().getPort());
        } else if (!param0.getServer().publishServer(param3, param2, param1)) {
            throw ERROR_FAILED.create();
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.publish.success", param1), true);
            return param1;
        }
    }
}
