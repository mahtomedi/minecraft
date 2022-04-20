package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class TitleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("title")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("clear").executes(param0x -> clearTitle(param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"))))
                        .then(Commands.literal("reset").executes(param0x -> resetTitle(param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"))))
                        .then(
                            Commands.literal("title")
                                .then(
                                    Commands.argument("title", ComponentArgument.textComponent())
                                        .executes(
                                            param0x -> showTitle(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    ComponentArgument.getComponent(param0x, "title"),
                                                    "title",
                                                    ClientboundSetTitleTextPacket::new
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("subtitle")
                                .then(
                                    Commands.argument("title", ComponentArgument.textComponent())
                                        .executes(
                                            param0x -> showTitle(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    ComponentArgument.getComponent(param0x, "title"),
                                                    "subtitle",
                                                    ClientboundSetSubtitleTextPacket::new
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("actionbar")
                                .then(
                                    Commands.argument("title", ComponentArgument.textComponent())
                                        .executes(
                                            param0x -> showTitle(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    ComponentArgument.getComponent(param0x, "title"),
                                                    "actionbar",
                                                    ClientboundSetActionBarTextPacket::new
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("times")
                                .then(
                                    Commands.argument("fadeIn", IntegerArgumentType.integer(0))
                                        .then(
                                            Commands.argument("stay", IntegerArgumentType.integer(0))
                                                .then(
                                                    Commands.argument("fadeOut", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            param0x -> setTimes(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                                    IntegerArgumentType.getInteger(param0x, "fadeIn"),
                                                                    IntegerArgumentType.getInteger(param0x, "stay"),
                                                                    IntegerArgumentType.getInteger(param0x, "fadeOut")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int clearTitle(CommandSourceStack param0, Collection<ServerPlayer> param1) {
        ClientboundClearTitlesPacket var0 = new ClientboundClearTitlesPacket(false);

        for(ServerPlayer var1 : param1) {
            var1.connection.send(var0);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(Component.translatable("commands.title.cleared.single", param1.iterator().next().getDisplayName()), true);
        } else {
            param0.sendSuccess(Component.translatable("commands.title.cleared.multiple", param1.size()), true);
        }

        return param1.size();
    }

    private static int resetTitle(CommandSourceStack param0, Collection<ServerPlayer> param1) {
        ClientboundClearTitlesPacket var0 = new ClientboundClearTitlesPacket(true);

        for(ServerPlayer var1 : param1) {
            var1.connection.send(var0);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(Component.translatable("commands.title.reset.single", param1.iterator().next().getDisplayName()), true);
        } else {
            param0.sendSuccess(Component.translatable("commands.title.reset.multiple", param1.size()), true);
        }

        return param1.size();
    }

    private static int showTitle(
        CommandSourceStack param0, Collection<ServerPlayer> param1, Component param2, String param3, Function<Component, Packet<?>> param4
    ) throws CommandSyntaxException {
        for(ServerPlayer var0 : param1) {
            var0.connection.send(param4.apply(ComponentUtils.updateForEntity(param0, param2, var0, 0)));
        }

        if (param1.size() == 1) {
            param0.sendSuccess(Component.translatable("commands.title.show." + param3 + ".single", param1.iterator().next().getDisplayName()), true);
        } else {
            param0.sendSuccess(Component.translatable("commands.title.show." + param3 + ".multiple", param1.size()), true);
        }

        return param1.size();
    }

    private static int setTimes(CommandSourceStack param0, Collection<ServerPlayer> param1, int param2, int param3, int param4) {
        ClientboundSetTitlesAnimationPacket var0 = new ClientboundSetTitlesAnimationPacket(param2, param3, param4);

        for(ServerPlayer var1 : param1) {
            var1.connection.send(var0);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(Component.translatable("commands.title.times.single", param1.iterator().next().getDisplayName()), true);
        } else {
            param0.sendSuccess(Component.translatable("commands.title.times.multiple", param1.size()), true);
        }

        return param1.size();
    }
}
