package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.teammsg.failed.noteam")
    );

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
            Consumer<Style> var2 = param0x -> param0x.setHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.type.team.hover"))
                    )
                    .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
            Component var3 = var1.getFormattedDisplayName().withStyle(var2);

            for(Component var4 : var3.getSiblings()) {
                var4.withStyle(var2);
            }

            List<ServerPlayer> var5 = param0.getServer().getPlayerList().getPlayers();

            for(ServerPlayer var6 : var5) {
                if (var6 == var0) {
                    var6.sendMessage(new TranslatableComponent("chat.type.team.sent", var3, param0.getDisplayName(), param1.deepCopy()));
                } else if (var6.getTeam() == var1) {
                    var6.sendMessage(new TranslatableComponent("chat.type.team.text", var3, param0.getDisplayName(), param1.deepCopy()));
                }
            }

            return var5.size();
        }
    }
}
