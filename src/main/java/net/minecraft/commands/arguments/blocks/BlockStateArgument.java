package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;

public class BlockStateArgument implements ArgumentType<BlockInput> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");

    public static BlockStateArgument block() {
        return new BlockStateArgument();
    }

    public BlockInput parse(StringReader param0) throws CommandSyntaxException {
        BlockStateParser var0 = new BlockStateParser(param0, false).parse(true);
        return new BlockInput(var0.getState(), var0.getProperties().keySet(), var0.getNbt());
    }

    public static BlockInput getBlock(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, BlockInput.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        StringReader var0 = new StringReader(param1.getInput());
        var0.setCursor(param1.getStart());
        BlockStateParser var1 = new BlockStateParser(var0, false);

        try {
            var1.parse(true);
        } catch (CommandSyntaxException var6) {
        }

        return var1.fillSuggestions(param1, Registry.BLOCK);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
