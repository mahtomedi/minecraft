package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.SignedMessage;
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
                        .executes(param0x -> sendMessage(param0x.getSource(), MessageArgument.getSignedMessage(param0x, "message")))
                )
        );
        param0.register(Commands.literal("tm").redirect(var0));
    }

    private static int sendMessage(CommandSourceStack param0, SignedMessage param1) throws CommandSyntaxException {
        Entity var0 = param0.getEntityOrException();
        PlayerTeam var1 = (PlayerTeam)var0.getTeam();
        if (var1 == null) {
            throw ERROR_NOT_ON_TEAM.create();
        } else {
            Component var2 = var1.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
            ChatSender var3 = param0.asChatSender().withTeamName(var2);
            List<ServerPlayer> var4 = param0.getServer().getPlayerList().getPlayers();

            for(ServerPlayer var5 : var4) {
                if (var5 == var0) {
                    var5.sendSystemMessage(Component.translatable("chat.type.team.sent", var2, param0.getDisplayName(), param1.content()));
                } else if (var5.getTeam() == var1) {
                    var5.sendChatMessage(param1, var3, ChatType.TEAM_MSG_COMMAND);
                }
            }

            return var4.size();
        }
    }
}
