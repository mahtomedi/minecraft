package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;

public class BossBarCommands {
    private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.bossbar.create.failed", param0)
    );
    private static final DynamicCommandExceptionType ERROR_DOESNT_EXIST = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.bossbar.unknown", param0)
    );
    private static final SimpleCommandExceptionType ERROR_NO_PLAYER_CHANGE = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.players.unchanged")
    );
    private static final SimpleCommandExceptionType ERROR_NO_NAME_CHANGE = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.name.unchanged")
    );
    private static final SimpleCommandExceptionType ERROR_NO_COLOR_CHANGE = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.color.unchanged")
    );
    private static final SimpleCommandExceptionType ERROR_NO_STYLE_CHANGE = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.style.unchanged")
    );
    private static final SimpleCommandExceptionType ERROR_NO_VALUE_CHANGE = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.value.unchanged")
    );
    private static final SimpleCommandExceptionType ERROR_NO_MAX_CHANGE = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.max.unchanged")
    );
    private static final SimpleCommandExceptionType ERROR_ALREADY_HIDDEN = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.visibility.unchanged.hidden")
    );
    private static final SimpleCommandExceptionType ERROR_ALREADY_VISIBLE = new SimpleCommandExceptionType(
        Component.translatable("commands.bossbar.set.visibility.unchanged.visible")
    );
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS_BAR = (param0, param1) -> SharedSuggestionProvider.suggestResource(
            param0.getSource().getServer().getCustomBossEvents().getIds(), param1
        );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("bossbar")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("id", ResourceLocationArgument.id())
                                .then(
                                    Commands.argument("name", ComponentArgument.textComponent())
                                        .executes(
                                            param0x -> createBar(
                                                    param0x.getSource(),
                                                    ResourceLocationArgument.getId(param0x, "id"),
                                                    ComponentArgument.getComponent(param0x, "name")
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_BOSS_BAR)
                                .executes(param0x -> removeBar(param0x.getSource(), getBossBar(param0x)))
                        )
                )
                .then(Commands.literal("list").executes(param0x -> listBars(param0x.getSource())))
                .then(
                    Commands.literal("set")
                        .then(
                            Commands.argument("id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_BOSS_BAR)
                                .then(
                                    Commands.literal("name")
                                        .then(
                                            Commands.argument("name", ComponentArgument.textComponent())
                                                .executes(
                                                    param0x -> setName(
                                                            param0x.getSource(), getBossBar(param0x), ComponentArgument.getComponent(param0x, "name")
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("color")
                                        .then(
                                            Commands.literal("pink")
                                                .executes(param0x -> setColor(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarColor.PINK))
                                        )
                                        .then(
                                            Commands.literal("blue")
                                                .executes(param0x -> setColor(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarColor.BLUE))
                                        )
                                        .then(
                                            Commands.literal("red")
                                                .executes(param0x -> setColor(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarColor.RED))
                                        )
                                        .then(
                                            Commands.literal("green")
                                                .executes(param0x -> setColor(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarColor.GREEN))
                                        )
                                        .then(
                                            Commands.literal("yellow")
                                                .executes(param0x -> setColor(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarColor.YELLOW))
                                        )
                                        .then(
                                            Commands.literal("purple")
                                                .executes(param0x -> setColor(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarColor.PURPLE))
                                        )
                                        .then(
                                            Commands.literal("white")
                                                .executes(param0x -> setColor(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarColor.WHITE))
                                        )
                                )
                                .then(
                                    Commands.literal("style")
                                        .then(
                                            Commands.literal("progress")
                                                .executes(param0x -> setStyle(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarOverlay.PROGRESS))
                                        )
                                        .then(
                                            Commands.literal("notched_6")
                                                .executes(param0x -> setStyle(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarOverlay.NOTCHED_6))
                                        )
                                        .then(
                                            Commands.literal("notched_10")
                                                .executes(param0x -> setStyle(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarOverlay.NOTCHED_10))
                                        )
                                        .then(
                                            Commands.literal("notched_12")
                                                .executes(param0x -> setStyle(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarOverlay.NOTCHED_12))
                                        )
                                        .then(
                                            Commands.literal("notched_20")
                                                .executes(param0x -> setStyle(param0x.getSource(), getBossBar(param0x), BossEvent.BossBarOverlay.NOTCHED_20))
                                        )
                                )
                                .then(
                                    Commands.literal("value")
                                        .then(
                                            Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(
                                                    param0x -> setValue(
                                                            param0x.getSource(), getBossBar(param0x), IntegerArgumentType.getInteger(param0x, "value")
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("max")
                                        .then(
                                            Commands.argument("max", IntegerArgumentType.integer(1))
                                                .executes(
                                                    param0x -> setMax(param0x.getSource(), getBossBar(param0x), IntegerArgumentType.getInteger(param0x, "max"))
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("visible")
                                        .then(
                                            Commands.argument("visible", BoolArgumentType.bool())
                                                .executes(
                                                    param0x -> setVisible(
                                                            param0x.getSource(), getBossBar(param0x), BoolArgumentType.getBool(param0x, "visible")
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("players")
                                        .executes(param0x -> setPlayers(param0x.getSource(), getBossBar(param0x), Collections.emptyList()))
                                        .then(
                                            Commands.argument("targets", EntityArgument.players())
                                                .executes(
                                                    param0x -> setPlayers(
                                                            param0x.getSource(), getBossBar(param0x), EntityArgument.getOptionalPlayers(param0x, "targets")
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("get")
                        .then(
                            Commands.argument("id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_BOSS_BAR)
                                .then(Commands.literal("value").executes(param0x -> getValue(param0x.getSource(), getBossBar(param0x))))
                                .then(Commands.literal("max").executes(param0x -> getMax(param0x.getSource(), getBossBar(param0x))))
                                .then(Commands.literal("visible").executes(param0x -> getVisible(param0x.getSource(), getBossBar(param0x))))
                                .then(Commands.literal("players").executes(param0x -> getPlayers(param0x.getSource(), getBossBar(param0x))))
                        )
                )
        );
    }

    private static int getValue(CommandSourceStack param0, CustomBossEvent param1) {
        param0.sendSuccess(Component.translatable("commands.bossbar.get.value", param1.getDisplayName(), param1.getValue()), true);
        return param1.getValue();
    }

    private static int getMax(CommandSourceStack param0, CustomBossEvent param1) {
        param0.sendSuccess(Component.translatable("commands.bossbar.get.max", param1.getDisplayName(), param1.getMax()), true);
        return param1.getMax();
    }

    private static int getVisible(CommandSourceStack param0, CustomBossEvent param1) {
        if (param1.isVisible()) {
            param0.sendSuccess(Component.translatable("commands.bossbar.get.visible.visible", param1.getDisplayName()), true);
            return 1;
        } else {
            param0.sendSuccess(Component.translatable("commands.bossbar.get.visible.hidden", param1.getDisplayName()), true);
            return 0;
        }
    }

    private static int getPlayers(CommandSourceStack param0, CustomBossEvent param1) {
        if (param1.getPlayers().isEmpty()) {
            param0.sendSuccess(Component.translatable("commands.bossbar.get.players.none", param1.getDisplayName()), true);
        } else {
            param0.sendSuccess(
                Component.translatable(
                    "commands.bossbar.get.players.some",
                    param1.getDisplayName(),
                    param1.getPlayers().size(),
                    ComponentUtils.formatList(param1.getPlayers(), Player::getDisplayName)
                ),
                true
            );
        }

        return param1.getPlayers().size();
    }

    private static int setVisible(CommandSourceStack param0, CustomBossEvent param1, boolean param2) throws CommandSyntaxException {
        if (param1.isVisible() == param2) {
            if (param2) {
                throw ERROR_ALREADY_VISIBLE.create();
            } else {
                throw ERROR_ALREADY_HIDDEN.create();
            }
        } else {
            param1.setVisible(param2);
            if (param2) {
                param0.sendSuccess(Component.translatable("commands.bossbar.set.visible.success.visible", param1.getDisplayName()), true);
            } else {
                param0.sendSuccess(Component.translatable("commands.bossbar.set.visible.success.hidden", param1.getDisplayName()), true);
            }

            return 0;
        }
    }

    private static int setValue(CommandSourceStack param0, CustomBossEvent param1, int param2) throws CommandSyntaxException {
        if (param1.getValue() == param2) {
            throw ERROR_NO_VALUE_CHANGE.create();
        } else {
            param1.setValue(param2);
            param0.sendSuccess(Component.translatable("commands.bossbar.set.value.success", param1.getDisplayName(), param2), true);
            return param2;
        }
    }

    private static int setMax(CommandSourceStack param0, CustomBossEvent param1, int param2) throws CommandSyntaxException {
        if (param1.getMax() == param2) {
            throw ERROR_NO_MAX_CHANGE.create();
        } else {
            param1.setMax(param2);
            param0.sendSuccess(Component.translatable("commands.bossbar.set.max.success", param1.getDisplayName(), param2), true);
            return param2;
        }
    }

    private static int setColor(CommandSourceStack param0, CustomBossEvent param1, BossEvent.BossBarColor param2) throws CommandSyntaxException {
        if (param1.getColor().equals(param2)) {
            throw ERROR_NO_COLOR_CHANGE.create();
        } else {
            param1.setColor(param2);
            param0.sendSuccess(Component.translatable("commands.bossbar.set.color.success", param1.getDisplayName()), true);
            return 0;
        }
    }

    private static int setStyle(CommandSourceStack param0, CustomBossEvent param1, BossEvent.BossBarOverlay param2) throws CommandSyntaxException {
        if (param1.getOverlay().equals(param2)) {
            throw ERROR_NO_STYLE_CHANGE.create();
        } else {
            param1.setOverlay(param2);
            param0.sendSuccess(Component.translatable("commands.bossbar.set.style.success", param1.getDisplayName()), true);
            return 0;
        }
    }

    private static int setName(CommandSourceStack param0, CustomBossEvent param1, Component param2) throws CommandSyntaxException {
        Component var0 = ComponentUtils.updateForEntity(param0, param2, null, 0);
        if (param1.getName().equals(var0)) {
            throw ERROR_NO_NAME_CHANGE.create();
        } else {
            param1.setName(var0);
            param0.sendSuccess(Component.translatable("commands.bossbar.set.name.success", param1.getDisplayName()), true);
            return 0;
        }
    }

    private static int setPlayers(CommandSourceStack param0, CustomBossEvent param1, Collection<ServerPlayer> param2) throws CommandSyntaxException {
        boolean var0 = param1.setPlayers(param2);
        if (!var0) {
            throw ERROR_NO_PLAYER_CHANGE.create();
        } else {
            if (param1.getPlayers().isEmpty()) {
                param0.sendSuccess(Component.translatable("commands.bossbar.set.players.success.none", param1.getDisplayName()), true);
            } else {
                param0.sendSuccess(
                    Component.translatable(
                        "commands.bossbar.set.players.success.some",
                        param1.getDisplayName(),
                        param2.size(),
                        ComponentUtils.formatList(param2, Player::getDisplayName)
                    ),
                    true
                );
            }

            return param1.getPlayers().size();
        }
    }

    private static int listBars(CommandSourceStack param0) {
        Collection<CustomBossEvent> var0 = param0.getServer().getCustomBossEvents().getEvents();
        if (var0.isEmpty()) {
            param0.sendSuccess(Component.translatable("commands.bossbar.list.bars.none"), false);
        } else {
            param0.sendSuccess(
                Component.translatable("commands.bossbar.list.bars.some", var0.size(), ComponentUtils.formatList(var0, CustomBossEvent::getDisplayName)), false
            );
        }

        return var0.size();
    }

    private static int createBar(CommandSourceStack param0, ResourceLocation param1, Component param2) throws CommandSyntaxException {
        CustomBossEvents var0 = param0.getServer().getCustomBossEvents();
        if (var0.get(param1) != null) {
            throw ERROR_ALREADY_EXISTS.create(param1.toString());
        } else {
            CustomBossEvent var1 = var0.create(param1, ComponentUtils.updateForEntity(param0, param2, null, 0));
            param0.sendSuccess(Component.translatable("commands.bossbar.create.success", var1.getDisplayName()), true);
            return var0.getEvents().size();
        }
    }

    private static int removeBar(CommandSourceStack param0, CustomBossEvent param1) {
        CustomBossEvents var0 = param0.getServer().getCustomBossEvents();
        param1.removeAllPlayers();
        var0.remove(param1);
        param0.sendSuccess(Component.translatable("commands.bossbar.remove.success", param1.getDisplayName()), true);
        return var0.getEvents().size();
    }

    public static CustomBossEvent getBossBar(CommandContext<CommandSourceStack> param0) throws CommandSyntaxException {
        ResourceLocation var0 = ResourceLocationArgument.getId(param0, "id");
        CustomBossEvent var1 = param0.getSource().getServer().getCustomBossEvents().get(var0);
        if (var1 == null) {
            throw ERROR_DOESNT_EXIST.create(var0.toString());
        } else {
            return var1;
        }
    }
}
