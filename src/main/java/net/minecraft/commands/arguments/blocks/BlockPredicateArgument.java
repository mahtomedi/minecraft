package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
    private final HolderLookup<Block> blocks;

    public BlockPredicateArgument(CommandBuildContext param0) {
        this.blocks = param0.holderLookup(Registries.BLOCK);
    }

    public static BlockPredicateArgument blockPredicate(CommandBuildContext param0) {
        return new BlockPredicateArgument(param0);
    }

    public BlockPredicateArgument.Result parse(StringReader param0) throws CommandSyntaxException {
        return parse(this.blocks, param0);
    }

    public static BlockPredicateArgument.Result parse(HolderLookup<Block> param0, StringReader param1) throws CommandSyntaxException {
        return BlockStateParser.parseForTesting(param0, param1, true)
            .map(
                param0x -> new BlockPredicateArgument.BlockPredicate(param0x.blockState(), param0x.properties().keySet(), param0x.nbt()),
                param0x -> new BlockPredicateArgument.TagPredicate(param0x.tag(), param0x.vagueProperties(), param0x.nbt())
            );
    }

    public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, BlockPredicateArgument.Result.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return BlockStateParser.fillSuggestions(this.blocks, param1, true, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static class BlockPredicate implements BlockPredicateArgument.Result {
        private final BlockState state;
        private final Set<Property<?>> properties;
        @Nullable
        private final CompoundTag nbt;

        public BlockPredicate(BlockState param0, Set<Property<?>> param1, @Nullable CompoundTag param2) {
            this.state = param0;
            this.properties = param1;
            this.nbt = param2;
        }

        public boolean test(BlockInWorld param0) {
            BlockState var0 = param0.getState();
            if (!var0.is(this.state.getBlock())) {
                return false;
            } else {
                for(Property<?> var1 : this.properties) {
                    if (var0.getValue(var1) != this.state.getValue(var1)) {
                        return false;
                    }
                }

                if (this.nbt == null) {
                    return true;
                } else {
                    BlockEntity var2 = param0.getEntity();
                    return var2 != null && NbtUtils.compareNbt(this.nbt, var2.saveWithFullMetadata(), true);
                }
            }
        }

        @Override
        public boolean requiresNbt() {
            return this.nbt != null;
        }
    }

    public interface Result extends Predicate<BlockInWorld> {
        boolean requiresNbt();
    }

    static class TagPredicate implements BlockPredicateArgument.Result {
        private final HolderSet<Block> tag;
        @Nullable
        private final CompoundTag nbt;
        private final Map<String, String> vagueProperties;

        TagPredicate(HolderSet<Block> param0, Map<String, String> param1, @Nullable CompoundTag param2) {
            this.tag = param0;
            this.vagueProperties = param1;
            this.nbt = param2;
        }

        public boolean test(BlockInWorld param0) {
            BlockState var0 = param0.getState();
            if (!var0.is(this.tag)) {
                return false;
            } else {
                for(Entry<String, String> var1 : this.vagueProperties.entrySet()) {
                    Property<?> var2 = var0.getBlock().getStateDefinition().getProperty(var1.getKey());
                    if (var2 == null) {
                        return false;
                    }

                    Comparable<?> var3 = (Comparable)var2.getValue(var1.getValue()).orElse(null);
                    if (var3 == null) {
                        return false;
                    }

                    if (var0.getValue(var2) != var3) {
                        return false;
                    }
                }

                if (this.nbt == null) {
                    return true;
                } else {
                    BlockEntity var4 = param0.getEntity();
                    return var4 != null && NbtUtils.compareNbt(this.nbt, var4.saveWithFullMetadata(), true);
                }
            }
        }

        @Override
        public boolean requiresNbt() {
            return this.nbt != null;
        }
    }
}
