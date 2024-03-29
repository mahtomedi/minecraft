package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.ScoreHolder;

public class ScoreHolderArgument implements ArgumentType<ScoreHolderArgument.Result> {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_SCORE_HOLDERS = (param0, param1) -> {
        StringReader var0 = new StringReader(param1.getInput());
        var0.setCursor(param1.getStart());
        EntitySelectorParser var1 = new EntitySelectorParser(var0);

        try {
            var1.parse();
        } catch (CommandSyntaxException var5) {
        }

        return var1.fillSuggestions(param1, param1x -> SharedSuggestionProvider.suggest(param0.getSource().getOnlinePlayerNames(), param1x));
    };
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
    private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType(Component.translatable("argument.scoreHolder.empty"));
    final boolean multiple;

    public ScoreHolderArgument(boolean param0) {
        this.multiple = param0;
    }

    public static ScoreHolder getName(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getNames(param0, param1).iterator().next();
    }

    public static Collection<ScoreHolder> getNames(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getNames(param0, param1, Collections::emptyList);
    }

    public static Collection<ScoreHolder> getNamesWithDefaultWildcard(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getNames(param0, param1, param0.getSource().getServer().getScoreboard()::getTrackedPlayers);
    }

    public static Collection<ScoreHolder> getNames(CommandContext<CommandSourceStack> param0, String param1, Supplier<Collection<ScoreHolder>> param2) throws CommandSyntaxException {
        Collection<ScoreHolder> var0 = param0.getArgument(param1, ScoreHolderArgument.Result.class).getNames(param0.getSource(), param2);
        if (var0.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        } else {
            return var0;
        }
    }

    public static ScoreHolderArgument scoreHolder() {
        return new ScoreHolderArgument(false);
    }

    public static ScoreHolderArgument scoreHolders() {
        return new ScoreHolderArgument(true);
    }

    public ScoreHolderArgument.Result parse(StringReader param0) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '@') {
            EntitySelectorParser var0 = new EntitySelectorParser(param0);
            EntitySelector var1 = var0.parse();
            if (!this.multiple && var1.getMaxResults() > 1) {
                throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            } else {
                return new ScoreHolderArgument.SelectorResult(var1);
            }
        } else {
            int var2 = param0.getCursor();

            while(param0.canRead() && param0.peek() != ' ') {
                param0.skip();
            }

            String var3 = param0.getString().substring(var2, param0.getCursor());
            if (var3.equals("*")) {
                return (param0x, param1) -> {
                    Collection<ScoreHolder> var0x = param1.get();
                    if (var0x.isEmpty()) {
                        throw ERROR_NO_RESULTS.create();
                    } else {
                        return var0x;
                    }
                };
            } else if (var3.startsWith("#")) {
                List<ScoreHolder> var4 = List.of(ScoreHolder.forNameOnly(var3));
                return (param1, param2) -> var4;
            } else {
                return (param1, param2) -> {
                    MinecraftServer var0x = param1.getServer();
                    ServerPlayer var1x = var0x.getPlayerList().getPlayerByName(var3);
                    if (var1x != null) {
                        return List.of(var1x);
                    } else {
                        try {
                            UUID var2x = UUID.fromString(var3);
                            List<ScoreHolder> var3x = new ArrayList<>();

                            for(ServerLevel var4x : var0x.getAllLevels()) {
                                Entity var5x = var4x.getEntity(var2x);
                                if (var5x != null) {
                                    var3x.add(var5x);
                                }
                            }

                            if (!var3x.isEmpty()) {
                                return var3x;
                            }
                        } catch (IllegalArgumentException var10) {
                        }

                        return List.of(ScoreHolder.forNameOnly(var3));
                    }
                };
            }
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Info implements ArgumentTypeInfo<ScoreHolderArgument, ScoreHolderArgument.Info.Template> {
        private static final byte FLAG_MULTIPLE = 1;

        public void serializeToNetwork(ScoreHolderArgument.Info.Template param0, FriendlyByteBuf param1) {
            int var0 = 0;
            if (param0.multiple) {
                var0 |= 1;
            }

            param1.writeByte(var0);
        }

        public ScoreHolderArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf param0) {
            byte var0 = param0.readByte();
            boolean var1 = (var0 & 1) != 0;
            return new ScoreHolderArgument.Info.Template(var1);
        }

        public void serializeToJson(ScoreHolderArgument.Info.Template param0, JsonObject param1) {
            param1.addProperty("amount", param0.multiple ? "multiple" : "single");
        }

        public ScoreHolderArgument.Info.Template unpack(ScoreHolderArgument param0) {
            return new ScoreHolderArgument.Info.Template(param0.multiple);
        }

        public final class Template implements ArgumentTypeInfo.Template<ScoreHolderArgument> {
            final boolean multiple;

            Template(boolean param1) {
                this.multiple = param1;
            }

            public ScoreHolderArgument instantiate(CommandBuildContext param0) {
                return new ScoreHolderArgument(this.multiple);
            }

            @Override
            public ArgumentTypeInfo<ScoreHolderArgument, ?> type() {
                return Info.this;
            }
        }
    }

    @FunctionalInterface
    public interface Result {
        Collection<ScoreHolder> getNames(CommandSourceStack var1, Supplier<Collection<ScoreHolder>> var2) throws CommandSyntaxException;
    }

    public static class SelectorResult implements ScoreHolderArgument.Result {
        private final EntitySelector selector;

        public SelectorResult(EntitySelector param0) {
            this.selector = param0;
        }

        @Override
        public Collection<ScoreHolder> getNames(CommandSourceStack param0, Supplier<Collection<ScoreHolder>> param1) throws CommandSyntaxException {
            List<? extends Entity> var0 = this.selector.findEntities(param0);
            if (var0.isEmpty()) {
                throw EntityArgument.NO_ENTITIES_FOUND.create();
            } else {
                return List.copyOf(var0);
            }
        }
    }
}
