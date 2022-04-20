package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.regex.Matcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.IpBanList;

public class PardonIpCommand {
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.invalid"));
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("pardon-ip")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.argument("target", StringArgumentType.word())
                        .suggests(
                            (param0x, param1) -> SharedSuggestionProvider.suggest(
                                    param0x.getSource().getServer().getPlayerList().getIpBans().getUserList(), param1
                                )
                        )
                        .executes(param0x -> unban(param0x.getSource(), StringArgumentType.getString(param0x, "target")))
                )
        );
    }

    private static int unban(CommandSourceStack param0, String param1) throws CommandSyntaxException {
        Matcher var0 = BanIpCommands.IP_ADDRESS_PATTERN.matcher(param1);
        if (!var0.matches()) {
            throw ERROR_INVALID.create();
        } else {
            IpBanList var1 = param0.getServer().getPlayerList().getIpBans();
            if (!var1.isBanned(param1)) {
                throw ERROR_NOT_BANNED.create();
            } else {
                var1.remove(param1);
                param0.sendSuccess(Component.translatable("commands.pardonip.success", param1), true);
                return 1;
            }
        }
    }
}
