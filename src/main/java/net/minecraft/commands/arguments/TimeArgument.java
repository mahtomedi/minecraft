package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class TimeArgument implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0d", "0s", "0t", "0");
    private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = new SimpleCommandExceptionType(Component.translatable("argument.time.invalid_unit"));
    private static final Dynamic2CommandExceptionType ERROR_TICK_COUNT_TOO_LOW = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.time.tick_count_too_low", param1, param0)
    );
    private static final Object2IntMap<String> UNITS = new Object2IntOpenHashMap<>();
    final int minimum;

    private TimeArgument(int param0) {
        this.minimum = param0;
    }

    public static TimeArgument time() {
        return new TimeArgument(0);
    }

    public static TimeArgument time(int param0) {
        return new TimeArgument(param0);
    }

    public Integer parse(StringReader param0) throws CommandSyntaxException {
        float var0 = param0.readFloat();
        String var1 = param0.readUnquotedString();
        int var2 = UNITS.getOrDefault(var1, 0);
        if (var2 == 0) {
            throw ERROR_INVALID_UNIT.create();
        } else {
            int var3 = Math.round(var0 * (float)var2);
            if (var3 < this.minimum) {
                throw ERROR_TICK_COUNT_TOO_LOW.create(var3, this.minimum);
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

    public static class Info implements ArgumentTypeInfo<TimeArgument, TimeArgument.Info.Template> {
        public void serializeToNetwork(TimeArgument.Info.Template param0, FriendlyByteBuf param1) {
            param1.writeInt(param0.min);
        }

        public TimeArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf param0) {
            int var0 = param0.readInt();
            return new TimeArgument.Info.Template(var0);
        }

        public void serializeToJson(TimeArgument.Info.Template param0, JsonObject param1) {
            param1.addProperty("min", param0.min);
        }

        public TimeArgument.Info.Template unpack(TimeArgument param0) {
            return new TimeArgument.Info.Template(param0.minimum);
        }

        public final class Template implements ArgumentTypeInfo.Template<TimeArgument> {
            final int min;

            Template(int param1) {
                this.min = param1;
            }

            public TimeArgument instantiate(CommandBuildContext param0) {
                return TimeArgument.time(this.min);
            }

            @Override
            public ArgumentTypeInfo<TimeArgument, ?> type() {
                return Info.this;
            }
        }
    }
}
