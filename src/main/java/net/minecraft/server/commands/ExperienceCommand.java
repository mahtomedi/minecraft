package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ExperienceCommand {
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(
        Component.translatable("commands.experience.set.points.invalid")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(
            Commands.literal("experience")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(
                                            param0x -> addExperience(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    IntegerArgumentType.getInteger(param0x, "amount"),
                                                    ExperienceCommand.Type.POINTS
                                                )
                                        )
                                        .then(
                                            Commands.literal("points")
                                                .executes(
                                                    param0x -> addExperience(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            IntegerArgumentType.getInteger(param0x, "amount"),
                                                            ExperienceCommand.Type.POINTS
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("levels")
                                                .executes(
                                                    param0x -> addExperience(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            IntegerArgumentType.getInteger(param0x, "amount"),
                                                            ExperienceCommand.Type.LEVELS
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("set")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(
                                            param0x -> setExperience(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    IntegerArgumentType.getInteger(param0x, "amount"),
                                                    ExperienceCommand.Type.POINTS
                                                )
                                        )
                                        .then(
                                            Commands.literal("points")
                                                .executes(
                                                    param0x -> setExperience(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            IntegerArgumentType.getInteger(param0x, "amount"),
                                                            ExperienceCommand.Type.POINTS
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("levels")
                                                .executes(
                                                    param0x -> setExperience(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            IntegerArgumentType.getInteger(param0x, "amount"),
                                                            ExperienceCommand.Type.LEVELS
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("query")
                        .then(
                            Commands.argument("targets", EntityArgument.player())
                                .then(
                                    Commands.literal("points")
                                        .executes(
                                            param0x -> queryExperience(
                                                    param0x.getSource(), EntityArgument.getPlayer(param0x, "targets"), ExperienceCommand.Type.POINTS
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("levels")
                                        .executes(
                                            param0x -> queryExperience(
                                                    param0x.getSource(), EntityArgument.getPlayer(param0x, "targets"), ExperienceCommand.Type.LEVELS
                                                )
                                        )
                                )
                        )
                )
        );
        param0.register(Commands.literal("xp").requires(param0x -> param0x.hasPermission(2)).redirect(var0));
    }

    private static int queryExperience(CommandSourceStack param0, ServerPlayer param1, ExperienceCommand.Type param2) {
        int var0 = param2.query.applyAsInt(param1);
        param0.sendSuccess(() -> Component.translatable("commands.experience.query." + param2.name, param1.getDisplayName(), var0), false);
        return var0;
    }

    private static int addExperience(CommandSourceStack param0, Collection<? extends ServerPlayer> param1, int param2, ExperienceCommand.Type param3) {
        for(ServerPlayer var0 : param1) {
            param3.add.accept(var0, param2);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(
                () -> Component.translatable("commands.experience.add." + param3.name + ".success.single", param2, param1.iterator().next().getDisplayName()),
                true
            );
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.experience.add." + param3.name + ".success.multiple", param2, param1.size()), true);
        }

        return param1.size();
    }

    private static int setExperience(CommandSourceStack param0, Collection<? extends ServerPlayer> param1, int param2, ExperienceCommand.Type param3) throws CommandSyntaxException {
        int var0 = 0;

        for(ServerPlayer var1 : param1) {
            if (param3.set.test(var1, param2)) {
                ++var0;
            }
        }

        if (var0 == 0) {
            throw ERROR_SET_POINTS_INVALID.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(
                    () -> Component.translatable(
                            "commands.experience.set." + param3.name + ".success.single", param2, param1.iterator().next().getDisplayName()
                        ),
                    true
                );
            } else {
                param0.sendSuccess(() -> Component.translatable("commands.experience.set." + param3.name + ".success.multiple", param2, param1.size()), true);
            }

            return param1.size();
        }
    }

    static enum Type {
        POINTS("points", Player::giveExperiencePoints, (param0, param1) -> {
            if (param1 >= param0.getXpNeededForNextLevel()) {
                return false;
            } else {
                param0.setExperiencePoints(param1);
                return true;
            }
        }, param0 -> Mth.floor(param0.experienceProgress * (float)param0.getXpNeededForNextLevel())),
        LEVELS("levels", ServerPlayer::giveExperienceLevels, (param0, param1) -> {
            param0.setExperienceLevels(param1);
            return true;
        }, param0 -> param0.experienceLevel);

        public final BiConsumer<ServerPlayer, Integer> add;
        public final BiPredicate<ServerPlayer, Integer> set;
        public final String name;
        final ToIntFunction<ServerPlayer> query;

        private Type(String param0, BiConsumer<ServerPlayer, Integer> param1, BiPredicate<ServerPlayer, Integer> param2, ToIntFunction<ServerPlayer> param3) {
            this.add = param1;
            this.name = param0;
            this.set = param2;
            this.query = param3;
        }
    }
}
