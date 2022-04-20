package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class TimeArgument implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0d", "0s", "0t", "0");
    private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = new SimpleCommandExceptionType(Component.translatable("argument.time.invalid_unit"));
    private static final DynamicCommandExceptionType ERROR_INVALID_TICK_COUNT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("argument.time.invalid_tick_count", param0)
    );
    private static final Object2IntMap<String> UNITS = new Object2IntOpenHashMap<>();

    public static TimeArgument time() {
        return new TimeArgument();
    }

    public Integer parse(StringReader param0) throws CommandSyntaxException {
        float var0 = param0.readFloat();
        String var1 = param0.readUnquotedString();
        int var2 = UNITS.getOrDefault(var1, 0);
        if (var2 == 0) {
            throw ERROR_INVALID_UNIT.create();
        } else {
            int var3 = Math.round(var0 * (float)var2);
            if (var3 < 0) {
                throw ERROR_INVALID_TICK_COUNT.create(var3);
            } else {
                return var3;
            }
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        StringReader var0 = new StringReader(param1.getRemaining());

        try {
            var0.readFloat();
        } catch (CommandSyntaxException var5) {
            return param1.buildFuture();
        }

        return SharedSuggestionProvider.suggest(UNITS.keySet(), param1.createOffset(param1.getStart() + var0.getCursor()));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static {
        UNITS.put("d", 24000);
        UNITS.put("s", 20);
        UNITS.put("t", 1);
        UNITS.put("", 1);
    }
}
