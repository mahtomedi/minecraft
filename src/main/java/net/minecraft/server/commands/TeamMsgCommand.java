package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
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
            Commands.literal("teammsg")
                .then(
                    Commands.argument("message", MessageArgument.message())
                        .executes(
                            param0x -> {
                                CommandSourceStack var0x = param0x.getSource();
                                Entity var1x = var0x.getEntityOrException();
                                PlayerTeam var2 = (PlayerTeam)var1x.getTeam();
                                if (var2 == null) {
                                    throw ERROR_NOT_ON_TEAM.create();
                                } else {
                                    List<ServerPlayer> var3 = var0x.getServer()
                                        .getPlayerList()
                                        .getPlayers()
                                        .stream()
                                        .filter(param2 -> param2 == var1x || param2.getTeam() == var2)
                                        .toList();
                                    if (!var3.isEmpty()) {
                                        MessageArgument.resolveChatMessage(param0x, "message", param4 -> sendMessage(var0x, var1x, var2, var3, param4));
                                    }
                    
                                    return var3.size();
                                }
                            }
                        )
                )
        );
        param0.register(Commands.literal("tm").redirect(var0));
    }

    private static void sendMessage(CommandSourceStack param0, Entity param1, PlayerTeam param2, List<ServerPlayer> param3, PlayerChatMessage param4) {
        Component var0 = param2.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
        ChatType.Bound var1 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, param0).withTargetName(var0);
        ChatType.Bound var2 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, param0).withTargetName(var0);
        OutgoingChatMessage var3 = OutgoingChatMessage.create(param4);
        boolean var4 = false;

        for(ServerPlayer var5 : param3) {
            ChatType.Bound var6 = var5 == param1 ? var2 : var1;
            boolean var7 = param0.shouldFilterMessageTo(var5);
            var5.sendChatMessage(var3, var7, var6);
            var4 |= var7 && param4.isFullyFiltered();
        }

        if (var4) {
            param0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }

    }
}
