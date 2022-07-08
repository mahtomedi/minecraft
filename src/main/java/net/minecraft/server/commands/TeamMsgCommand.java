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
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
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
            ChatSender var3 = param0.asChatSender();
            ChatType.Bound var4 = ChatType.bind(ChatType.TEAM_MSG_COMMAND, param0).withTargetName(var2);
            List<ServerPlayer> var5 = param0.getServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .filter(param2 -> param2 == var0 || param2.getTeam() == var1)
                .toList();
            param1.resolve(param0).thenAcceptAsync(param6 -> {
                FilteredText<OutgoingPlayerChatMessage> var0x = OutgoingPlayerChatMessage.createFromFiltered(param6, var3);

                for(ServerPlayer var1x : var5) {
                    if (var1x == var0) {
                        var1x.sendSystemMessage(Component.translatable("chat.type.team.sent", var2, param0.getDisplayName(), param6.raw().serverContent()));
                    } else {
                        OutgoingPlayerChatMessage var2x = var0x.filter(param0, var1x);
                        if (var2x != null) {
                            var1x.sendChatMessage(var2x, var4);
                        }
                    }
                }

                var0x.raw().sendHeadersToRemainingPlayers(param0.getServer().getPlayerList());
            }, param0.getServer());
            return var5.size();
        }
    }
}
