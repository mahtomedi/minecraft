package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveCriteriaArgument implements ArgumentType<ObjectiveCriteria> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar.baz", "minecraft:foo");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.criteria.invalid", param0)
    );

    private ObjectiveCriteriaArgument() {
    }

    public static ObjectiveCriteriaArgument criteria() {
        return new ObjectiveCriteriaArgument();
    }

    public static ObjectiveCriteria getCriteria(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, ObjectiveCriteria.class);
    }

    public ObjectiveCriteria parse(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();

        while(param0.canRead() && param0.peek() != ' ') {
            param0.skip();
        }

        String var1 = param0.getString().substring(var0, param0.getCursor());
        return ObjectiveCriteria.byName(var1).orElseThrow(() -> {
            param0.setCursor(var0);
            return ERROR_INVALID_VALUE.create(var1);
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        List<String> var0 = Lists.newArrayList(ObjectiveCriteria.getCustomCriteriaNames());

        for(StatType<?> var1 : Registry.STAT_TYPE) {
            for(Object var2 : var1.getRegistry()) {
                String var3 = this.getName(var1, var2);
                var0.add(var3);
            }
        }

        return SharedSuggestionProvider.suggest(var0, param1);
    }

    public <T> String getName(StatType<T> param0, Object param1) {
        return Stat.buildName(param0, (T)param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
