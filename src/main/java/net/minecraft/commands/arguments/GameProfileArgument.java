package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class GameProfileArgument implements ArgumentType<GameProfileArgument.Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "@e");
    public static final SimpleCommandExceptionType ERROR_UNKNOWN_PLAYER = new SimpleCommandExceptionType(new TranslatableComponent("argument.player.unknown"));

    public static Collection<GameProfile> getGameProfiles(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, GameProfileArgument.Result.class).getNames(param0.getSource());
    }

    public static GameProfileArgument gameProfile() {
        return new GameProfileArgument();
    }

    public GameProfileArgument.Result parse(StringReader param0) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '@') {
            EntitySelectorParser var0 = new EntitySelectorParser(param0);
            EntitySelector var1 = var0.parse();
            if (var1.includesEntities()) {
                throw EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.create();
            } else {
                return new GameProfileArgument.SelectorResult(var1);
            }
        } else {
            int var2 = param0.getCursor();

            while(param0.canRead() && param0.peek() != ' ') {
                param0.skip();
            }

            String var3 = param0.getString().substring(var2, param0.getCursor());
            return param1 -> {
                Optional<GameProfile> var0x = param1.getServer().getProfileCache().get(var3);
                return Collections.singleton(var0x.orElseThrow(ERROR_UNKNOWN_PLAYER::create));
            };
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        if (param0.getSource() instanceof SharedSuggestionProvider) {
            StringReader var0 = new StringReader(param1.getInput());
            var0.setCursor(param1.getStart());
            EntitySelectorParser var1 = new EntitySelectorParser(var0);

            try {
                var1.parse();
            } catch (CommandSyntaxException var6) {
            }

            return var1.fillSuggestions(
                param1, param1x -> SharedSuggestionProvider.suggest(((SharedSuggestionProvider)param0.getSource()).getOnlinePlayerNames(), param1x)
            );
        } else {
            return Suggestions.empty();
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @FunctionalInterface
    public interface Result {
        Collection<GameProfile> getNames(CommandSourceStack var1) throws CommandSyntaxException;
    }

    public static class SelectorResult implements GameProfileArgument.Result {
        private final EntitySelector selector;

        public SelectorResult(EntitySelector param0) {
            this.selector = param0;
        }

        @Override
        public Collection<GameProfile> getNames(CommandSourceStack param0) throws CommandSyntaxException {
            List<ServerPlayer> var0 = this.selector.findPlayers(param0);
            if (var0.isEmpty()) {
                throw EntityArgument.NO_PLAYERS_FOUND.create();
            } else {
                List<GameProfile> var1 = Lists.newArrayList();

                for(ServerPlayer var2 : var0) {
                    var1.add(var2.getGameProfile());
                }

                return var1;
            }
        }
    }
}
