package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class EntitySelectorOptions {
    private static final Map<String, EntitySelectorOptions.Option> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.entity.options.unknown", param0)
    );
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.entity.options.inapplicable", param0)
    );
    public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.entity.options.distance.negative")
    );
    public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.entity.options.level.negative")
    );
    public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(
        new TranslatableComponent("argument.entity.options.limit.toosmall")
    );
    public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.entity.options.sort.irreversible", param0)
    );
    public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.entity.options.mode.invalid", param0)
    );
    public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.entity.options.type.invalid", param0)
    );

    private static void register(String param0, EntitySelectorOptions.Modifier param1, Predicate<EntitySelectorParser> param2, Component param3) {
        OPTIONS.put(param0, new EntitySelectorOptions.Option(param1, param2, param3));
    }

    public static void bootStrap() {
        if (OPTIONS.isEmpty()) {
            register("name", param0 -> {
                int var0 = param0.getReader().getCursor();
                boolean var1 = param0.shouldInvertValue();
                String var2 = param0.getReader().readString();
                if (param0.hasNameNotEquals() && !var1) {
                    param0.getReader().setCursor(var0);
                    throw ERROR_INAPPLICABLE_OPTION.createWithContext(param0.getReader(), "name");
                } else {
                    if (var1) {
                        param0.setHasNameNotEquals(true);
                    } else {
                        param0.setHasNameEquals(true);
                    }

                    param0.addPredicate(param2 -> param2.getName().getContents().equals(var2) != var1);
                }
            }, param0 -> !param0.hasNameEquals(), new TranslatableComponent("argument.entity.options.name.description"));
            register("distance", param0 -> {
                int var0 = param0.getReader().getCursor();
                MinMaxBounds.Floats var1 = MinMaxBounds.Floats.fromReader(param0.getReader());
                if ((var1.getMin() == null || !(var1.getMin() < 0.0F)) && (var1.getMax() == null || !(var1.getMax() < 0.0F))) {
                    param0.setDistance(var1);
                    param0.setWorldLimited();
                } else {
                    param0.getReader().setCursor(var0);
                    throw ERROR_RANGE_NEGATIVE.createWithContext(param0.getReader());
                }
            }, param0 -> param0.getDistance().isAny(), new TranslatableComponent("argument.entity.options.distance.description"));
            register("level", param0 -> {
                int var0 = param0.getReader().getCursor();
                MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromReader(param0.getReader());
                if ((var1.getMin() == null || var1.getMin() >= 0) && (var1.getMax() == null || var1.getMax() >= 0)) {
                    param0.setLevel(var1);
                    param0.setIncludesEntities(false);
                } else {
                    param0.getReader().setCursor(var0);
                    throw ERROR_LEVEL_NEGATIVE.createWithContext(param0.getReader());
                }
            }, param0 -> param0.getLevel().isAny(), new TranslatableComponent("argument.entity.options.level.description"));
            register("x", param0 -> {
                param0.setWorldLimited();
                param0.setX(param0.getReader().readDouble());
            }, param0 -> param0.getX() == null, new TranslatableComponent("argument.entity.options.x.description"));
            register("y", param0 -> {
                param0.setWorldLimited();
                param0.setY(param0.getReader().readDouble());
            }, param0 -> param0.getY() == null, new TranslatableComponent("argument.entity.options.y.description"));
            register("z", param0 -> {
                param0.setWorldLimited();
                param0.setZ(param0.getReader().readDouble());
            }, param0 -> param0.getZ() == null, new TranslatableComponent("argument.entity.options.z.description"));
            register("dx", param0 -> {
                param0.setWorldLimited();
                param0.setDeltaX(param0.getReader().readDouble());
            }, param0 -> param0.getDeltaX() == null, new TranslatableComponent("argument.entity.options.dx.description"));
            register("dy", param0 -> {
                param0.setWorldLimited();
                param0.setDeltaY(param0.getReader().readDouble());
            }, param0 -> param0.getDeltaY() == null, new TranslatableComponent("argument.entity.options.dy.description"));
            register("dz", param0 -> {
                param0.setWorldLimited();
                param0.setDeltaZ(param0.getReader().readDouble());
            }, param0 -> param0.getDeltaZ() == null, new TranslatableComponent("argument.entity.options.dz.description"));
            register(
                "x_rotation",
                param0 -> param0.setRotX(WrappedMinMaxBounds.fromReader(param0.getReader(), true, Mth::wrapDegrees)),
                param0 -> param0.getRotX() == WrappedMinMaxBounds.ANY,
                new TranslatableComponent("argument.entity.options.x_rotation.description")
            );
            register(
                "y_rotation",
                param0 -> param0.setRotY(WrappedMinMaxBounds.fromReader(param0.getReader(), true, Mth::wrapDegrees)),
                param0 -> param0.getRotY() == WrappedMinMaxBounds.ANY,
                new TranslatableComponent("argument.entity.options.y_rotation.description")
            );
            register("limit", param0 -> {
                int var0 = param0.getReader().getCursor();
                int var1 = param0.getReader().readInt();
                if (var1 < 1) {
                    param0.getReader().setCursor(var0);
                    throw ERROR_LIMIT_TOO_SMALL.createWithContext(param0.getReader());
                } else {
                    param0.setMaxResults(var1);
                    param0.setLimited(true);
                }
            }, param0 -> !param0.isCurrentEntity() && !param0.isLimited(), new TranslatableComponent("argument.entity.options.limit.description"));
            register(
                "sort",
                param0 -> {
                    int var0 = param0.getReader().getCursor();
                    String var1 = param0.getReader().readUnquotedString();
                    param0.setSuggestions(
                        (param0x, param1) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), param0x)
                    );
                    BiConsumer<Vec3, List<? extends Entity>> var2;
                    switch(var1) {
                        case "nearest":
                            var2 = EntitySelectorParser.ORDER_NEAREST;
                            break;
                        case "furthest":
                            var2 = EntitySelectorParser.ORDER_FURTHEST;
                            break;
                        case "random":
                            var2 = EntitySelectorParser.ORDER_RANDOM;
                            break;
                        case "arbitrary":
                            var2 = EntitySelectorParser.ORDER_ARBITRARY;
                            break;
                        default:
                            param0.getReader().setCursor(var0);
                            throw ERROR_SORT_UNKNOWN.createWithContext(param0.getReader(), var1);
                    }
    
                    param0.setOrder(var2);
                    param0.setSorted(true);
                },
                param0 -> !param0.isCurrentEntity() && !param0.isSorted(),
                new TranslatableComponent("argument.entity.options.sort.description")
            );
            register("gamemode", param0 -> {
                param0.setSuggestions((param1, param2) -> {
                    String var0x = param1.getRemaining().toLowerCase(Locale.ROOT);
                    boolean var1x = !param0.hasGamemodeNotEquals();
                    boolean var2x = true;
                    if (!var0x.isEmpty()) {
                        if (var0x.charAt(0) == '!') {
                            var1x = false;
                            var0x = var0x.substring(1);
                        } else {
                            var2x = false;
                        }
                    }

                    for(GameType var3x : GameType.values()) {
                        if (var3x != GameType.NOT_SET && var3x.getName().toLowerCase(Locale.ROOT).startsWith(var0x)) {
                            if (var2x) {
                                param1.suggest('!' + var3x.getName());
                            }

                            if (var1x) {
                                param1.suggest(var3x.getName());
                            }
                        }
                    }

                    return param1.buildFuture();
                });
                int var0 = param0.getReader().getCursor();
                boolean var1 = param0.shouldInvertValue();
                if (param0.hasGamemodeNotEquals() && !var1) {
                    param0.getReader().setCursor(var0);
                    throw ERROR_INAPPLICABLE_OPTION.createWithContext(param0.getReader(), "gamemode");
                } else {
                    String var2 = param0.getReader().readUnquotedString();
                    GameType var3 = GameType.byName(var2, GameType.NOT_SET);
                    if (var3 == GameType.NOT_SET) {
                        param0.getReader().setCursor(var0);
                        throw ERROR_GAME_MODE_INVALID.createWithContext(param0.getReader(), var2);
                    } else {
                        param0.setIncludesEntities(false);
                        param0.addPredicate(param2 -> {
                            if (!(param2 instanceof ServerPlayer)) {
                                return false;
                            } else {
                                GameType var0x = ((ServerPlayer)param2).gameMode.getGameModeForPlayer();
                                return var1 ? var0x != var3 : var0x == var3;
                            }
                        });
                        if (var1) {
                            param0.setHasGamemodeNotEquals(true);
                        } else {
                            param0.setHasGamemodeEquals(true);
                        }

                    }
                }
            }, param0 -> !param0.hasGamemodeEquals(), new TranslatableComponent("argument.entity.options.gamemode.description"));
            register("team", param0 -> {
                boolean var0 = param0.shouldInvertValue();
                String var1 = param0.getReader().readUnquotedString();
                param0.addPredicate(param2 -> {
                    if (!(param2 instanceof LivingEntity)) {
                        return false;
                    } else {
                        Team var0x = param2.getTeam();
                        String var1x = var0x == null ? "" : var0x.getName();
                        return var1x.equals(var1) != var0;
                    }
                });
                if (var0) {
                    param0.setHasTeamNotEquals(true);
                } else {
                    param0.setHasTeamEquals(true);
                }

            }, param0 -> !param0.hasTeamEquals(), new TranslatableComponent("argument.entity.options.team.description"));
            register("type", param0 -> {
                param0.setSuggestions((param1, param2) -> {
                    SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), param1, String.valueOf('!'));
                    SharedSuggestionProvider.suggestResource(EntityTypeTags.getAllTags().getAvailableTags(), param1, "!#");
                    if (!param0.isTypeLimitedInversely()) {
                        SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), param1);
                        SharedSuggestionProvider.suggestResource(EntityTypeTags.getAllTags().getAvailableTags(), param1, String.valueOf('#'));
                    }

                    return param1.buildFuture();
                });
                int var0 = param0.getReader().getCursor();
                boolean var1 = param0.shouldInvertValue();
                if (param0.isTypeLimitedInversely() && !var1) {
                    param0.getReader().setCursor(var0);
                    throw ERROR_INAPPLICABLE_OPTION.createWithContext(param0.getReader(), "type");
                } else {
                    if (var1) {
                        param0.setTypeLimitedInversely();
                    }

                    if (param0.isTag()) {
                        ResourceLocation var2 = ResourceLocation.read(param0.getReader());
                        Tag<EntityType<?>> var3 = EntityTypeTags.getAllTags().getTag(var2);
                        if (var3 == null) {
                            param0.getReader().setCursor(var0);
                            throw ERROR_ENTITY_TYPE_INVALID.createWithContext(param0.getReader(), var2.toString());
                        }

                        param0.addPredicate(param2 -> var3.contains(param2.getType()) != var1);
                    } else {
                        ResourceLocation var4 = ResourceLocation.read(param0.getReader());
                        EntityType<?> var5 = Registry.ENTITY_TYPE.getOptional(var4).orElseThrow(() -> {
                            param0.getReader().setCursor(var0);
                            return ERROR_ENTITY_TYPE_INVALID.createWithContext(param0.getReader(), var4.toString());
                        });
                        if (Objects.equals(EntityType.PLAYER, var5) && !var1) {
                            param0.setIncludesEntities(false);
                        }

                        param0.addPredicate(param2 -> Objects.equals(var5, param2.getType()) != var1);
                        if (!var1) {
                            param0.limitToType(var5);
                        }
                    }

                }
            }, param0 -> !param0.isTypeLimited(), new TranslatableComponent("argument.entity.options.type.description"));
            register("tag", param0 -> {
                boolean var0 = param0.shouldInvertValue();
                String var1 = param0.getReader().readUnquotedString();
                param0.addPredicate(param2 -> {
                    if ("".equals(var1)) {
                        return param2.getTags().isEmpty() != var0;
                    } else {
                        return param2.getTags().contains(var1) != var0;
                    }
                });
            }, param0 -> true, new TranslatableComponent("argument.entity.options.tag.description"));
            register("nbt", param0 -> {
                boolean var0 = param0.shouldInvertValue();
                CompoundTag var1 = new TagParser(param0.getReader()).readStruct();
                param0.addPredicate(param2 -> {
                    CompoundTag var0x = param2.saveWithoutId(new CompoundTag());
                    if (param2 instanceof ServerPlayer) {
                        ItemStack var1x = ((ServerPlayer)param2).inventory.getSelected();
                        if (!var1x.isEmpty()) {
                            var0x.put("SelectedItem", var1x.save(new CompoundTag()));
                        }
                    }

                    return NbtUtils.compareNbt(var1, var0x, true) != var0;
                });
            }, param0 -> true, new TranslatableComponent("argument.entity.options.nbt.description"));
            register("scores", param0 -> {
                StringReader var0 = param0.getReader();
                Map<String, MinMaxBounds.Ints> var1 = Maps.newHashMap();
                var0.expect('{');
                var0.skipWhitespace();

                while(var0.canRead() && var0.peek() != '}') {
                    var0.skipWhitespace();
                    String var2 = var0.readUnquotedString();
                    var0.skipWhitespace();
                    var0.expect('=');
                    var0.skipWhitespace();
                    MinMaxBounds.Ints var3 = MinMaxBounds.Ints.fromReader(var0);
                    var1.put(var2, var3);
                    var0.skipWhitespace();
                    if (var0.canRead() && var0.peek() == ',') {
                        var0.skip();
                    }
                }

                var0.expect('}');
                if (!var1.isEmpty()) {
                    param0.addPredicate(param1 -> {
                        Scoreboard var0x = param1.getServer().getScoreboard();
                        String var1x = param1.getScoreboardName();

                        for(Entry<String, MinMaxBounds.Ints> var2x : var1.entrySet()) {
                            Objective var3x = var0x.getObjective(var2x.getKey());
                            if (var3x == null) {
                                return false;
                            }

                            if (!var0x.hasPlayerScore(var1x, var3x)) {
                                return false;
                            }

                            Score var4x = var0x.getOrCreatePlayerScore(var1x, var3x);
                            int var5 = var4x.getScore();
                            if (!var2x.getValue().matches(var5)) {
                                return false;
                            }
                        }

                        return true;
                    });
                }

                param0.setHasScores(true);
            }, param0 -> !param0.hasScores(), new TranslatableComponent("argument.entity.options.scores.description"));
            register("advancements", param0 -> {
                StringReader var0 = param0.getReader();
                Map<ResourceLocation, Predicate<AdvancementProgress>> var1 = Maps.newHashMap();
                var0.expect('{');
                var0.skipWhitespace();

                while(var0.canRead() && var0.peek() != '}') {
                    var0.skipWhitespace();
                    ResourceLocation var2 = ResourceLocation.read(var0);
                    var0.skipWhitespace();
                    var0.expect('=');
                    var0.skipWhitespace();
                    if (var0.canRead() && var0.peek() == '{') {
                        Map<String, Predicate<CriterionProgress>> var3 = Maps.newHashMap();
                        var0.skipWhitespace();
                        var0.expect('{');
                        var0.skipWhitespace();

                        while(var0.canRead() && var0.peek() != '}') {
                            var0.skipWhitespace();
                            String var4 = var0.readUnquotedString();
                            var0.skipWhitespace();
                            var0.expect('=');
                            var0.skipWhitespace();
                            boolean var5 = var0.readBoolean();
                            var3.put(var4, param1 -> param1.isDone() == var5);
                            var0.skipWhitespace();
                            if (var0.canRead() && var0.peek() == ',') {
                                var0.skip();
                            }
                        }

                        var0.skipWhitespace();
                        var0.expect('}');
                        var0.skipWhitespace();
                        var1.put(var2, param1 -> {
                            for(Entry<String, Predicate<CriterionProgress>> var0x : var3.entrySet()) {
                                CriterionProgress var1x = param1.getCriterion(var0x.getKey());
                                if (var1x == null || !var0x.getValue().test(var1x)) {
                                    return false;
                                }
                            }

                            return true;
                        });
                    } else {
                        boolean var6 = var0.readBoolean();
                        var1.put(var2, param1 -> param1.isDone() == var6);
                    }

                    var0.skipWhitespace();
                    if (var0.canRead() && var0.peek() == ',') {
                        var0.skip();
                    }
                }

                var0.expect('}');
                if (!var1.isEmpty()) {
                    param0.addPredicate(param1 -> {
                        if (!(param1 instanceof ServerPlayer)) {
                            return false;
                        } else {
                            ServerPlayer var0x = (ServerPlayer)param1;
                            PlayerAdvancements var1x = var0x.getAdvancements();
                            ServerAdvancementManager var2x = var0x.getServer().getAdvancements();

                            for(Entry<ResourceLocation, Predicate<AdvancementProgress>> var3x : var1.entrySet()) {
                                Advancement var4x = var2x.getAdvancement(var3x.getKey());
                                if (var4x == null || !var3x.getValue().test(var1x.getOrStartProgress(var4x))) {
                                    return false;
                                }
                            }

                            return true;
                        }
                    });
                    param0.setIncludesEntities(false);
                }

                param0.setHasAdvancements(true);
            }, param0 -> !param0.hasAdvancements(), new TranslatableComponent("argument.entity.options.advancements.description"));
        }
    }

    public static EntitySelectorOptions.Modifier get(EntitySelectorParser param0, String param1, int param2) throws CommandSyntaxException {
        EntitySelectorOptions.Option var0 = OPTIONS.get(param1);
        if (var0 != null) {
            if (var0.predicate.test(param0)) {
                return var0.modifier;
            } else {
                throw ERROR_INAPPLICABLE_OPTION.createWithContext(param0.getReader(), param1);
            }
        } else {
            param0.getReader().setCursor(param2);
            throw ERROR_UNKNOWN_OPTION.createWithContext(param0.getReader(), param1);
        }
    }

    public static void suggestNames(EntitySelectorParser param0, SuggestionsBuilder param1) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);

        for(Entry<String, EntitySelectorOptions.Option> var1 : OPTIONS.entrySet()) {
            if (var1.getValue().predicate.test(param0) && var1.getKey().toLowerCase(Locale.ROOT).startsWith(var0)) {
                param1.suggest((String)var1.getKey() + '=', var1.getValue().description);
            }
        }

    }

    public interface Modifier {
        void handle(EntitySelectorParser var1) throws CommandSyntaxException;
    }

    static class Option {
        public final EntitySelectorOptions.Modifier modifier;
        public final Predicate<EntitySelectorParser> predicate;
        public final Component description;

        private Option(EntitySelectorOptions.Modifier param0, Predicate<EntitySelectorParser> param1, Component param2) {
            this.modifier = param0;
            this.predicate = param1;
            this.description = param2;
        }
    }
}
