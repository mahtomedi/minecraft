package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
    private static final Style SUGGEST_STYLE = Style.EMPTY
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.type.team.hover")))
        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
    private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(Component.translatable("commands.teammsg.failed.noteam"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(
            Commands.literal("teammsg")
                .then(
                    Commands.argument("message", MessageArgument.message())
                        .executes(param0x -> sendMessage(param0x.getSource(), MessageArgument.getMessage(param0x, "message")))
                )
        );
        param0.register(Commands.literal("tm").redirect(var0));
    }

    private static int sendMessage(CommandSourceStack param0, Component param1) throws CommandSyntaxException {
        Entity var0 = param0.getEntityOrException();
        PlayerTeam var1 = (PlayerTeam)var0.getTeam();
        if (var1 == null) {
            throw ERROR_NOT_ON_TEAM.create();
        } else {
            Component var2 = var1.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
            List<ServerPlayer> var3 = param0.getServer().getPlayerList().getPlayers();

            for(ServerPlayer var4 : var3) {
                if (var4 == var0) {
                    var4.sendMessage(Component.translatable("chat.type.team.sent", var2, param0.getDisplayName(), param1), var0.getUUID());
                } else if (var4.getTeam() == var1) {
                    var4.sendMessage(Component.translatable("chat.type.team.text", var2, param0.getDisplayName(), param1), var0.getUUID());
                }
            }

            return var3.size();
        }
    }
}
