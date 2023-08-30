package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (param0, param1) -> {
        Collection<AdvancementHolder> var0 = param0.getSource().getServer().getAdvancements().getAllAdvancements();
        return SharedSuggestionProvider.suggestResource(var0.stream().map(AdvancementHolder::id), param1);
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("advancement")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("grant")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.literal("only")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.ONLY
                                                            )
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("criterion", StringArgumentType.greedyString())
                                                        .suggests(
                                                            (param0x, param1) -> SharedSuggestionProvider.suggest(
                                                                    ResourceLocationArgument.getAdvancement(param0x, "advancement").value().criteria().keySet(),
                                                                    param1
                                                                )
                                                        )
                                                        .executes(
                                                            param0x -> performCriterion(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                                    AdvancementCommands.Action.GRANT,
                                                                    ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                    StringArgumentType.getString(param0x, "criterion")
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("from")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.FROM
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("until")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.UNTIL
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("through")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.GRANT,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.THROUGH
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("everything")
                                        .executes(
                                            param0x -> perform(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    AdvancementCommands.Action.GRANT,
                                                    param0x.getSource().getServer().getAdvancements().getAllAdvancements()
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("revoke")
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .then(
                                    Commands.literal("only")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.ONLY
                                                            )
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("criterion", StringArgumentType.greedyString())
                                                        .suggests(
                                                            (param0x, param1) -> SharedSuggestionProvider.suggest(
                                                                    ResourceLocationArgument.getAdvancement(param0x, "advancement").value().criteria().keySet(),
                                                                    param1
                                                                )
                                                        )
                                                        .executes(
                                                            param0x -> performCriterion(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                                    AdvancementCommands.Action.REVOKE,
                                                                    ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                    StringArgumentType.getString(param0x, "criterion")
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("from")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.FROM
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("until")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.UNTIL
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("through")
                                        .then(
                                            Commands.argument("advancement", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_ADVANCEMENTS)
                                                .executes(
                                                    param0x -> perform(
                                                            param0x.getSource(),
                                                            EntityArgument.getPlayers(param0x, "targets"),
                                                            AdvancementCommands.Action.REVOKE,
                                                            getAdvancements(
                                                                param0x,
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"),
                                                                AdvancementCommands.Mode.THROUGH
                                                            )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("everything")
                                        .executes(
                                            param0x -> perform(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    AdvancementCommands.Action.REVOKE,
                                                    param0x.getSource().getServer().getAdvancements().getAllAdvancements()
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int perform(
        CommandSourceStack param0, Collection<ServerPlayer> param1, AdvancementCommands.Action param2, Collection<AdvancementHolder> param3
    ) {
        int var0 = 0;

        for(ServerPlayer var1 : param1) {
            var0 += param2.perform(var1, param3);
        }

        if (var0 == 0) {
            if (param3.size() == 1) {
                if (param1.size() == 1) {
                    throw new CommandRuntimeException(
                        Component.translatable(
                            param2.getKey() + ".one.to.one.failure", Advancement.name(param3.iterator().next()), param1.iterator().next().getDisplayName()
                        )
                    );
                } else {
                    throw new CommandRuntimeException(
                        Component.translatable(param2.getKey() + ".one.to.many.failure", Advancement.name(param3.iterator().next()), param1.size())
                    );
                }
            } else if (param1.size() == 1) {
                throw new CommandRuntimeException(
                    Component.translatable(param2.getKey() + ".many.to.one.failure", param3.size(), param1.iterator().next().getDisplayName())
                );
            } else {
                throw new CommandRuntimeException(Component.translatable(param2.getKey() + ".many.to.many.failure", param3.size(), param1.size()));
            }
        } else {
            if (param3.size() == 1) {
                if (param1.size() == 1) {
                    param0.sendSuccess(
                        () -> Component.translatable(
                                param2.getKey() + ".one.to.one.success", Advancement.name(param3.iterator().next()), param1.iterator().next().getDisplayName()
                            ),
                        true
                    );
                } else {
                    param0.sendSuccess(
                        () -> Component.translatable(param2.getKey() + ".one.to.many.success", Advancement.name(param3.iterator().next()), param1.size()), true
                    );
                }
            } else if (param1.size() == 1) {
                param0.sendSuccess(
                    () -> Component.translatable(param2.getKey() + ".many.to.one.success", param3.size(), param1.iterator().next().getDisplayName()), true
                );
            } else {
                param0.sendSuccess(() -> Component.translatable(param2.getKey() + ".many.to.many.success", param3.size(), param1.size()), true);
            }

            return var0;
        }
    }

    private static int performCriterion(
        CommandSourceStack param0, Collection<ServerPlayer> param1, AdvancementCommands.Action param2, AdvancementHolder param3, String param4
    ) {
        int var0 = 0;
        Advancement var1 = param3.value();
        if (!var1.criteria().containsKey(param4)) {
            throw new CommandRuntimeException(Component.translatable("commands.advancement.criterionNotFound", Advancement.name(param3), param4));
        } else {
            for(ServerPlayer var2 : param1) {
                if (param2.performCriterion(var2, param3, param4)) {
                    ++var0;
                }
            }

            if (var0 == 0) {
                if (param1.size() == 1) {
                    throw new CommandRuntimeException(
                        Component.translatable(
                            param2.getKey() + ".criterion.to.one.failure", param4, Advancement.name(param3), param1.iterator().next().getDisplayName()
                        )
                    );
                } else {
                    throw new CommandRuntimeException(
                        Component.translatable(param2.getKey() + ".criterion.to.many.failure", param4, Advancement.name(param3), param1.size())
                    );
                }
            } else {
                if (param1.size() == 1) {
                    param0.sendSuccess(
                        () -> Component.translatable(
                                param2.getKey() + ".criterion.to.one.success", param4, Advancement.name(param3), param1.iterator().next().getDisplayName()
                            ),
                        true
                    );
                } else {
                    param0.sendSuccess(
                        () -> Component.translatable(param2.getKey() + ".criterion.to.many.success", param4, Advancement.name(param3), param1.size()), true
                    );
                }

                return var0;
            }
        }
    }

    private static List<AdvancementHolder> getAdvancements(CommandContext<CommandSourceStack> param0, AdvancementHolder param1, AdvancementCommands.Mode param2) {
        AdvancementTree var0 = param0.getSource().getServer().getAdvancements().tree();
        AdvancementNode var1 = var0.get(param1);
        if (var1 == null) {
            return List.of(param1);
        } else {
            List<AdvancementHolder> var2 = new ArrayList<>();
            if (param2.parents) {
                for(AdvancementNode var3 = var1.parent(); var3 != null; var3 = var3.parent()) {
                    var2.add(var3.holder());
                }
            }

            var2.add(param1);
            if (param2.children) {
                addChildren(var1, var2);
            }

            return var2;
        }
    }

    private static void addChildren(AdvancementNode param0, List<AdvancementHolder> param1) {
        for(AdvancementNode var0 : param0.children()) {
            param1.add(var0.holder());
            addChildren(var0, param1);
        }

    }

    static enum Action {
        GRANT("grant") {
            @Override
            protected boolean perform(ServerPlayer param0, AdvancementHolder param1) {
                AdvancementProgress var0 = param0.getAdvancements().getOrStartProgress(param1);
                if (var0.isDone()) {
                    return false;
                } else {
                    for(String var1 : var0.getRemainingCriteria()) {
                        param0.getAdvancements().award(param1, var1);
                    }

                    return true;
                }
            }

            @Override
            protected boolean performCriterion(ServerPlayer param0, AdvancementHolder param1, String param2) {
                return param0.getAdvancements().award(param1, param2);
            }
        },
        REVOKE("revoke") {
            @Override
            protected boolean perform(ServerPlayer param0, AdvancementHolder param1) {
                AdvancementProgress var0 = param0.getAdvancements().getOrStartProgress(param1);
                if (!var0.hasProgress()) {
                    return false;
                } else {
                    for(String var1 : var0.getCompletedCriteria()) {
                        param0.getAdvancements().revoke(param1, var1);
                    }

                    return true;
                }
            }

            @Override
            protected boolean performCriterion(ServerPlayer param0, AdvancementHolder param1, String param2) {
                return param0.getAdvancements().revoke(param1, param2);
            }
        };

        private final String key;

        Action(String param0) {
            this.key = "commands.advancement." + param0;
        }

        public int perform(ServerPlayer param0, Iterable<AdvancementHolder> param1) {
            int var0 = 0;

            for(AdvancementHolder var1 : param1) {
                if (this.perform(param0, var1)) {
                    ++var0;
                }
            }

            return var0;
        }

        protected abstract boolean perform(ServerPlayer var1, AdvancementHolder var2);

        protected abstract boolean performCriterion(ServerPlayer var1, AdvancementHolder var2, String var3);

        protected String getKey() {
            return this.key;
        }
    }

    static enum Mode {
        ONLY(false, false),
        THROUGH(true, true),
        FROM(false, true),
        UNTIL(true, false),
        EVERYTHING(true, true);

        final boolean parents;
        final boolean children;

        private Mode(boolean param0, boolean param1) {
            this.parents = param0;
            this.children = param1;
        }
    }
}
