package net.minecraft.server.commands;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class TagCommand {
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.tag.add.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.tag.remove.failed")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("tag")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.entities())
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("name", StringArgumentType.word())
                                        .executes(
                                            param0x -> addTag(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    StringArgumentType.getString(param0x, "name")
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("name", StringArgumentType.word())
                                        .suggests(
                                            (param0x, param1) -> SharedSuggestionProvider.suggest(
                                                    getTags(EntityArgument.getEntities(param0x, "targets")), param1
                                                )
                                        )
                                        .executes(
                                            param0x -> removeTag(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    StringArgumentType.getString(param0x, "name")
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("list").executes(param0x -> listTags(param0x.getSource(), EntityArgument.getEntities(param0x, "targets"))))
                )
        );
    }

    private static Collection<String> getTags(Collection<? extends Entity> param0) {
        Set<String> var0 = Sets.newHashSet();

        for(Entity var1 : param0) {
            var0.addAll(var1.getTags());
        }

        return var0;
    }

    private static int addTag(CommandSourceStack param0, Collection<? extends Entity> param1, String param2) throws CommandSyntaxException {
        int var0 = 0;

        for(Entity var1 : param1) {
            if (var1.addTag(param2)) {
                ++var0;
            }
        }

        if (var0 == 0) {
            throw ERROR_ADD_FAILED.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(new TranslatableComponent("commands.tag.add.success.single", param2, param1.iterator().next().getDisplayName()), true);
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.tag.add.success.multiple", param2, param1.size()), true);
            }

            return var0;
        }
    }

    private static int removeTag(CommandSourceStack param0, Collection<? extends Entity> param1, String param2) throws CommandSyntaxException {
        int var0 = 0;

        for(Entity var1 : param1) {
            if (var1.removeTag(param2)) {
                ++var0;
            }
        }

        if (var0 == 0) {
            throw ERROR_REMOVE_FAILED.create();
        } else {
            if (param1.size() == 1) {
                param0.sendSuccess(new TranslatableComponent("commands.tag.remove.success.single", param2, param1.iterator().next().getDisplayName()), true);
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.tag.remove.success.multiple", param2, param1.size()), true);
            }

            return var0;
        }
    }

    private static int listTags(CommandSourceStack param0, Collection<? extends Entity> param1) {
        Set<String> var0 = Sets.newHashSet();

        for(Entity var1 : param1) {
            var0.addAll(var1.getTags());
        }

        if (param1.size() == 1) {
            Entity var2 = param1.iterator().next();
            if (var0.isEmpty()) {
                param0.sendSuccess(new TranslatableComponent("commands.tag.list.single.empty", var2.getDisplayName()), false);
            } else {
                param0.sendSuccess(
                    new TranslatableComponent("commands.tag.list.single.success", var2.getDisplayName(), var0.size(), ComponentUtils.formatList(var0)), false
                );
            }
        } else if (var0.isEmpty()) {
            param0.sendSuccess(new TranslatableComponent("commands.tag.list.multiple.empty", param1.size()), false);
        } else {
            param0.sendSuccess(
                new TranslatableComponent("commands.tag.list.multiple.success", param1.size(), var0.size(), ComponentUtils.formatList(var0)), false
            );
        }

        return var0.size();
    }
}
