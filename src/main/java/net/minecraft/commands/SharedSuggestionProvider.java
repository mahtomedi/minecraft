package net.minecraft.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface SharedSuggestionProvider {
    Collection<String> getOnlinePlayerNames();

    default Collection<String> getSelectedEntities() {
        return Collections.emptyList();
    }

    Collection<String> getAllTeams();

    Collection<ResourceLocation> getAvailableSoundEvents();

    Stream<ResourceLocation> getRecipeNames();

    CompletableFuture<Suggestions> customSuggestion(CommandContext<SharedSuggestionProvider> var1, SuggestionsBuilder var2);

    default Collection<SharedSuggestionProvider.TextCoordinates> getRelevantCoordinates() {
        return Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_GLOBAL);
    }

    default Collection<SharedSuggestionProvider.TextCoordinates> getAbsoluteCoordinates() {
        return Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_GLOBAL);
    }

    Set<ResourceKey<Level>> levels();

    RegistryAccess registryAccess();

    boolean hasPermission(int var1);

    static <T> void filterResources(Iterable<T> param0, String param1, Function<T, ResourceLocation> param2, Consumer<T> param3) {
        boolean var0 = param1.indexOf(58) > -1;

        for(T var1 : param0) {
            ResourceLocation var2 = param2.apply(var1);
            if (var0) {
                String var3 = var2.toString();
                if (matchesSubStr(param1, var3)) {
                    param3.accept(var1);
                }
            } else if (matchesSubStr(param1, var2.getNamespace()) || var2.getNamespace().equals("minecraft") && matchesSubStr(param1, var2.getPath())) {
                param3.accept(var1);
            }
        }

    }

    static <T> void filterResources(Iterable<T> param0, String param1, String param2, Function<T, ResourceLocation> param3, Consumer<T> param4) {
        if (param1.isEmpty()) {
            param0.forEach(param4);
        } else {
            String var0 = Strings.commonPrefix(param1, param2);
            if (!var0.isEmpty()) {
                String var1 = param1.substring(var0.length());
                filterResources(param0, var1, param3, param4);
            }
        }

    }

    static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> param0, SuggestionsBuilder param1, String param2) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);
        filterResources(param0, var0, param2, param0x -> param0x, param2x -> param1.suggest(param2 + param2x));
        return param1.buildFuture();
    }

    static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> param0, SuggestionsBuilder param1, String param2) {
        return suggestResource(param0::iterator, param1, param2);
    }

    static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> param0, SuggestionsBuilder param1) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);
        filterResources(param0, var0, param0x -> param0x, param1x -> param1.suggest(param1x.toString()));
        return param1.buildFuture();
    }

    static <T> CompletableFuture<Suggestions> suggestResource(
        Iterable<T> param0, SuggestionsBuilder param1, Function<T, ResourceLocation> param2, Function<T, Message> param3
    ) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);
        filterResources(param0, var0, param2, param3x -> param1.suggest(param2.apply(param3x).toString(), param3.apply(param3x)));
        return param1.buildFuture();
    }

    static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> param0, SuggestionsBuilder param1) {
        return suggestResource(param0::iterator, param1);
    }

    static <T> CompletableFuture<Suggestions> suggestResource(
        Stream<T> param0, SuggestionsBuilder param1, Function<T, ResourceLocation> param2, Function<T, Message> param3
    ) {
        return suggestResource(param0::iterator, param1, param2, param3);
    }

    static CompletableFuture<Suggestions> suggestCoordinates(
        String param0, Collection<SharedSuggestionProvider.TextCoordinates> param1, SuggestionsBuilder param2, Predicate<String> param3
    ) {
        List<String> var0 = Lists.newArrayList();
        if (Strings.isNullOrEmpty(param0)) {
            for(SharedSuggestionProvider.TextCoordinates var1 : param1) {
                String var2 = var1.x + " " + var1.y + " " + var1.z;
                if (param3.test(var2)) {
                    var0.add(var1.x);
                    var0.add(var1.x + " " + var1.y);
                    var0.add(var2);
                }
            }
        } else {
            String[] var3 = param0.split(" ");
            if (var3.length == 1) {
                for(SharedSuggestionProvider.TextCoordinates var4 : param1) {
                    String var5 = var3[0] + " " + var4.y + " " + var4.z;
                    if (param3.test(var5)) {
                        var0.add(var3[0] + " " + var4.y);
                        var0.add(var5);
                    }
                }
            } else if (var3.length == 2) {
                for(SharedSuggestionProvider.TextCoordinates var6 : param1) {
                    String var7 = var3[0] + " " + var3[1] + " " + var6.z;
                    if (param3.test(var7)) {
                        var0.add(var7);
                    }
                }
            }
        }

        return suggest(var0, param2);
    }

    static CompletableFuture<Suggestions> suggest2DCoordinates(
        String param0, Collection<SharedSuggestionProvider.TextCoordinates> param1, SuggestionsBuilder param2, Predicate<String> param3
    ) {
        List<String> var0 = Lists.newArrayList();
        if (Strings.isNullOrEmpty(param0)) {
            for(SharedSuggestionProvider.TextCoordinates var1 : param1) {
                String var2 = var1.x + " " + var1.z;
                if (param3.test(var2)) {
                    var0.add(var1.x);
                    var0.add(var2);
                }
            }
        } else {
            String[] var3 = param0.split(" ");
            if (var3.length == 1) {
                for(SharedSuggestionProvider.TextCoordinates var4 : param1) {
                    String var5 = var3[0] + " " + var4.z;
                    if (param3.test(var5)) {
                        var0.add(var5);
                    }
                }
            }
        }

        return suggest(var0, param2);
    }

    static CompletableFuture<Suggestions> suggest(Iterable<String> param0, SuggestionsBuilder param1) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);

        for(String var1 : param0) {
            if (matchesSubStr(var0, var1.toLowerCase(Locale.ROOT))) {
                param1.suggest(var1);
            }
        }

        return param1.buildFuture();
    }

    static CompletableFuture<Suggestions> suggest(Stream<String> param0, SuggestionsBuilder param1) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);
        param0.filter(param1x -> matchesSubStr(var0, param1x.toLowerCase(Locale.ROOT))).forEach(param1::suggest);
        return param1.buildFuture();
    }

    static CompletableFuture<Suggestions> suggest(String[] param0, SuggestionsBuilder param1) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);

        for(String var1 : param0) {
            if (matchesSubStr(var0, var1.toLowerCase(Locale.ROOT))) {
                param1.suggest(var1);
            }
        }

        return param1.buildFuture();
    }

    static <T> CompletableFuture<Suggestions> suggest(Iterable<T> param0, SuggestionsBuilder param1, Function<T, String> param2, Function<T, Message> param3) {
        String var0 = param1.getRemaining().toLowerCase(Locale.ROOT);

        for(T var1 : param0) {
            String var2 = param2.apply(var1);
            if (matchesSubStr(var0, var2.toLowerCase(Locale.ROOT))) {
                param1.suggest(var2, param3.apply(var1));
            }
        }

        return param1.buildFuture();
    }

    static boolean matchesSubStr(String param0, String param1) {
        for(int var0 = 0; !param1.startsWith(param0, var0); ++var0) {
            var0 = param1.indexOf(95, var0);
            if (var0 < 0) {
                return false;
            }
        }

        return true;
    }

    public static class TextCoordinates {
        public static final SharedSuggestionProvider.TextCoordinates DEFAULT_LOCAL = new SharedSuggestionProvider.TextCoordinates("^", "^", "^");
        public static final SharedSuggestionProvider.TextCoordinates DEFAULT_GLOBAL = new SharedSuggestionProvider.TextCoordinates("~", "~", "~");
        public final String x;
        public final String y;
        public final String z;

        public TextCoordinates(String param0, String param1, String param2) {
            this.x = param0;
            this.y = param1;
            this.z = param2;
        }
    }
}
