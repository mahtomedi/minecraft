package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
    private static final Style SUGGEST_STYLE = Style.EMPTY
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.type.team.hover")))
        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
    private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(Component.translatable("commands.teammsg.failed.noteam"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(
            Commands.literal("teammsg").then(Commands.argument("message", MessageArgument.message()).executes(param0x -> {
                MessageArgument.ChatMessage var0x = MessageArgument.getChatMessage(param0x, "message");
    
                try {
                    return sendMessage(param0x.getSource(), var0x);
                } catch (Exception var3) {
                    var0x.consume(param0x.getSource());
                    throw var3;
                }
            }))
        );
        param0.register(Commands.literal("tm").redirect(var0));
    }

    private static int sendMessage(CommandSourceStack param0, MessageArgument.ChatMessage param1) throws CommandSyntaxException {
        Entity var0 = param0.getEntityOrException();
        PlayerTeam var1 = (PlayerTeam)var0.getTeam();
        if (var1 == null) {
            throw ERROR_NOT_ON_TEAM.create();
        } else {
            Component var2 = var1.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
            ChatType.Bound var3 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, param0).withTargetName(var2);
            ChatType.Bound var4 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, param0).withTargetName(var2);
            List<ServerPlayer> var5 = param0.getServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .filter(param2 -> param2 == var0 || param2.getTeam() == var1)
                .toList();
            param1.resolve(param0, param5 -> {
                OutgoingPlayerChatMessage var0x = OutgoingPlayerChatMessage.create(param5);
                boolean var1x = param5.isFullyFiltered();
                boolean var2x = false;

                for(ServerPlayer var3x : var5) {
                    ChatType.Bound var4x = var3x == var0 ? var4 : var3;
                    boolean var5x = param0.shouldFilterMessageTo(var3x);
                    var3x.sendChatMessage(var0x, var5x, var4x);
                    var2x |= var1x && var5x && var3x != var0;
                }

                if (var2x) {
                    param0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
                }

                var0x.sendHeadersToRemainingPlayers(param0.getServer().getPlayerList());
            });
            return var5.size();
        }
    }
}
