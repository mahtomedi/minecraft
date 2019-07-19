package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (param0, param1) -> {
        Collection<Advancement> var0 = param0.getSource().getServer().getAdvancements().getAllAdvancements();
        return SharedSuggestionProvider.suggestResource(var0.stream().map(Advancement::getId), param1);
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
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"), AdvancementCommands.Mode.ONLY
                                                            )
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("criterion", StringArgumentType.greedyString())
                                                        .suggests(
                                                            (param0x, param1) -> SharedSuggestionProvider.suggest(
                                                                    ResourceLocationArgument.getAdvancement(param0x, "advancement").getCriteria().keySet(),
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
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"), AdvancementCommands.Mode.FROM
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
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"), AdvancementCommands.Mode.UNTIL
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
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"), AdvancementCommands.Mode.ONLY
                                                            )
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("criterion", StringArgumentType.greedyString())
                                                        .suggests(
                                                            (param0x, param1) -> SharedSuggestionProvider.suggest(
                                                                    ResourceLocationArgument.getAdvancement(param0x, "advancement").getCriteria().keySet(),
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
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"), AdvancementCommands.Mode.FROM
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
                                                                ResourceLocationArgument.getAdvancement(param0x, "advancement"), AdvancementCommands.Mode.UNTIL
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

    private static int perform(CommandSourceStack param0, Collection<ServerPlayer> param1, AdvancementCommands.Action param2, Collection<Advancement> param3) {
        int var0 = 0;

        for(ServerPlayer var1 : param1) {
            var0 += param2.perform(var1, param3);
        }

        if (var0 == 0) {
            if (param3.size() == 1) {
                if (param1.size() == 1) {
                    throw new CommandRuntimeException(
                        new TranslatableComponent(
                            param2.getKey() + ".one.to.one.failure", param3.iterator().next().getChatComponent(), param1.iterator().next().getDisplayName()
                        )
                    );
                } else {
                    throw new CommandRuntimeException(
                        new TranslatableComponent(param2.getKey() + ".one.to.many.failure", param3.iterator().next().getChatComponent(), param1.size())
                    );
                }
            } else if (param1.size() == 1) {
                throw new CommandRuntimeException(
                    new TranslatableComponent(param2.getKey() + ".many.to.one.failure", param3.size(), param1.iterator().next().getDisplayName())
                );
            } else {
                throw new CommandRuntimeException(new TranslatableComponent(param2.getKey() + ".many.to.many.failure", param3.size(), param1.size()));
            }
        } else {
            if (param3.size() == 1) {
                if (param1.size() == 1) {
                    param0.sendSuccess(
                        new TranslatableComponent(
                            param2.getKey() + ".one.to.one.success", param3.iterator().next().getChatComponent(), param1.iterator().next().getDisplayName()
                        ),
                        true
                    );
                } else {
                    param0.sendSuccess(
                        new TranslatableComponent(param2.getKey() + ".one.to.many.success", param3.iterator().next().getChatComponent(), param1.size()), true
                    );
                }
            } else if (param1.size() == 1) {
                param0.sendSuccess(
                    new TranslatableComponent(param2.getKey() + ".many.to.one.success", param3.size(), param1.iterator().next().getDisplayName()), true
                );
            } else {
                param0.sendSuccess(new TranslatableComponent(param2.getKey() + ".many.to.many.success", param3.size(), param1.size()), true);
            }

            return var0;
        }
    }

    private static int performCriterion(
        CommandSourceStack param0, Collection<ServerPlayer> param1, AdvancementCommands.Action param2, Advancement param3, String param4
    ) {
        int var0 = 0;
        if (!param3.getCriteria().containsKey(param4)) {
            throw new CommandRuntimeException(new TranslatableComponent("commands.advancement.criterionNotFound", param3.getChatComponent(), param4));
        } else {
            for(ServerPlayer var1 : param1) {
                if (param2.performCriterion(var1, param3, param4)) {
                    ++var0;
                }
            }

            if (var0 == 0) {
                if (param1.size() == 1) {
                    throw new CommandRuntimeException(
                        new TranslatableComponent(
                            param2.getKey() + ".criterion.to.one.failure", param4, param3.getChatComponent(), param1.iterator().next().getDisplayName()
                        )
                    );
                } else {
                    throw new CommandRuntimeException(
                        new TranslatableComponent(param2.getKey() + ".criterion.to.many.failure", param4, param3.getChatComponent(), param1.size())
                    );
                }
            } else {
                if (param1.size() == 1) {
                    param0.sendSuccess(
                        new TranslatableComponent(
                            param2.getKey() + ".criterion.to.one.success", param4, param3.getChatComponent(), param1.iterator().next().getDisplayName()
                        ),
                        true
                    );
                } else {
                    param0.sendSuccess(
                        new TranslatableComponent(param2.getKey() + ".criterion.to.many.success", param4, param3.getChatComponent(), param1.size()), true
                    );
                }

                return var0;
            }
        }
    }

    private static List<Advancement> getAdvancements(Advancement param0, AdvancementCommands.Mode param1) {
        List<Advancement> var0 = Lists.newArrayList();
        if (param1.parents) {
            for(Advancement var1 = param0.getParent(); var1 != null; var1 = var1.getParent()) {
                var0.add(var1);
            }
        }

        var0.add(param0);
        if (param1.children) {
            addChildren(param0, var0);
        }

        return var0;
    }

    private static void addChildren(Advancement param0, List<Advancement> param1) {
        for(Advancement var0 : param0.getChildren()) {
            param1.add(var0);
            addChildren(var0, param1);
        }

    }

    static enum Action {
        GRANT("grant") {
            @Override
            protected boolean perform(ServerPlayer param0, Advancement param1) {
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
            protected boolean performCriterion(ServerPlayer param0, Advancement param1, String param2) {
                return param0.getAdvancements().award(param1, param2);
            }
        },
        REVOKE("revoke") {
            @Override
            protected boolean perform(ServerPlayer param0, Advancement param1) {
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
            protected boolean performCriterion(ServerPlayer param0, Advancement param1, String param2) {
                return param0.getAdvancements().revoke(param1, param2);
            }
        };

        private final String key;

        private Action(String param0) {
            this.key = "commands.advancement." + param0;
        }

        public int perform(ServerPlayer param0, Iterable<Advancement> param1) {
            int var0 = 0;

            for(Advancement var1 : param1) {
                if (this.perform(param0, var1)) {
                    ++var0;
                }
            }

            return var0;
        }

        protected abstract boolean perform(ServerPlayer var1, Advancement var2);

        protected abstract boolean performCriterion(ServerPlayer var1, Advancement var2, String var3);

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

        private final boolean parents;
        private final boolean children;

        private Mode(boolean param0, boolean param1) {
            this.parents = param0;
            this.children = param1;
        }
    }
}
