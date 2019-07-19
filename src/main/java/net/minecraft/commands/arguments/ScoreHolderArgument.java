package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

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
    private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType(new TranslatableComponent("argument.scoreHolder.empty"));
    private final boolean multiple;

    public ScoreHolderArgument(boolean param0) {
        this.multiple = param0;
    }

    public static String getName(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getNames(param0, param1).iterator().next();
    }

    public static Collection<String> getNames(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getNames(param0, param1, Collections::emptyList);
    }

    public static Collection<String> getNamesWithDefaultWildcard(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return getNames(param0, param1, param0.getSource().getServer().getScoreboard()::getTrackedPlayers);
    }

    public static Collection<String> getNames(CommandContext<CommandSourceStack> param0, String param1, Supplier<Collection<String>> param2) throws CommandSyntaxException {
        Collection<String> var0 = param0.getArgument(param1, ScoreHolderArgument.Result.class).getNames(param0.getSource(), param2);
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
                    Collection<String> var0x = param1.get();
                    if (var0x.isEmpty()) {
                        throw ERROR_NO_RESULTS.create();
                    } else {
                        return var0x;
                    }
                };
            } else {
                Collection<String> var4 = Collections.singleton(var3);
                return (param1, param2) -> var4;
            }
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @FunctionalInterface
    public interface Result {
        Collection<String> getNames(CommandSourceStack var1, Supplier<Collection<String>> var2) throws CommandSyntaxException;
    }

    public static class SelectorResult implements ScoreHolderArgument.Result {
        private final EntitySelector selector;

        public SelectorResult(EntitySelector param0) {
            this.selector = param0;
        }

        @Override
        public Collection<String> getNames(CommandSourceStack param0, Supplier<Collection<String>> param1) throws CommandSyntaxException {
            List<? extends Entity> var0 = this.selector.findEntities(param0);
            if (var0.isEmpty()) {
                throw EntityArgument.NO_ENTITIES_FOUND.create();
            } else {
                List<String> var1 = Lists.newArrayList();

                for(Entity var2 : var0) {
                    var1.add(var2.getScoreboardName());
                }

                return var1;
            }
        }
    }

    public static class Serializer implements ArgumentSerializer<ScoreHolderArgument> {
        public void serializeToNetwork(ScoreHolderArgument param0, FriendlyByteBuf param1) {
            byte var0 = 0;
            if (param0.multiple) {
                var0 = (byte)(var0 | 1);
            }

            param1.writeByte(var0);
        }

        public ScoreHolderArgument deserializeFromNetwork(FriendlyByteBuf param0) {
            byte var0 = param0.readByte();
            boolean var1 = (var0 & 1) != 0;
            return new ScoreHolderArgument(var1);
        }

        public void serializeToJson(ScoreHolderArgument param0, JsonObject param1) {
            param1.addProperty("amount", param0.multiple ? "multiple" : "single");
        }
    }
}
