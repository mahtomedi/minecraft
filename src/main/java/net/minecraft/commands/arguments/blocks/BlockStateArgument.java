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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class BlockStateArgument implements ArgumentType<BlockInput> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");
    private final HolderLookup<Block> blocks;

    public BlockStateArgument(CommandBuildContext param0) {
        this.blocks = param0.holderLookup(Registries.BLOCK);
    }

    public static BlockStateArgument block(CommandBuildContext param0) {
        return new BlockStateArgument(param0);
    }

    public BlockInput parse(StringReader param0) throws CommandSyntaxException {
        BlockStateParser.BlockResult var0 = BlockStateParser.parseForBlock(this.blocks, param0, true);
        return new BlockInput(var0.blockState(), var0.properties().keySet(), var0.nbt());
    }

    public static BlockInput getBlock(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, BlockInput.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return BlockStateParser.fillSuggestions(this.blocks, param1, false, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
