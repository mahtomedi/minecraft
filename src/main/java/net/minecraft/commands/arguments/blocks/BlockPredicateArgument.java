package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("arguments.block.tag.unknown", param0)
    );

    public static BlockPredicateArgument blockPredicate() {
        return new BlockPredicateArgument();
    }

    public BlockPredicateArgument.Result parse(StringReader param0) throws CommandSyntaxException {
        BlockStateParser var0 = new BlockStateParser(param0, true).parse(true);
        if (var0.getState() != null) {
            BlockPredicateArgument.BlockPredicate var1 = new BlockPredicateArgument.BlockPredicate(
                var0.getState(), var0.getProperties().keySet(), var0.getNbt()
            );
            return param1 -> var1;
        } else {
            ResourceLocation var2 = var0.getTag();
            return param2 -> {
                Tag<Block> var0x = param2.getBlocks().getTag(var2);
                if (var0x == null) {
                    throw ERROR_UNKNOWN_TAG.create(var2.toString());
                } else {
                    return new BlockPredicateArgument.TagPredicate(var0x, var0.getVagueProperties(), var0.getNbt());
                }
            };
        }
    }

    public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, BlockPredicateArgument.Result.class).create(param0.getSource().getServer().getTags());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        StringReader var0 = new StringReader(param1.getInput());
        var0.setCursor(param1.getStart());
        BlockStateParser var1 = new BlockStateParser(var0, true);

        try {
            var1.parse(true);
        } catch (CommandSyntaxException var6) {
        }

        return var1.fillSuggestions(param1, BlockTags.getAllTags());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static class BlockPredicate implements Predicate<BlockInWorld> {
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
                    return var2 != null && NbtUtils.compareNbt(this.nbt, var2.save(new CompoundTag()), true);
                }
            }
        }
    }

    public interface Result {
        Predicate<BlockInWorld> create(TagContainer var1) throws CommandSyntaxException;
    }

    static class TagPredicate implements Predicate<BlockInWorld> {
        private final Tag<Block> tag;
        @Nullable
        private final CompoundTag nbt;
        private final Map<String, String> vagueProperties;

        private TagPredicate(Tag<Block> param0, Map<String, String> param1, @Nullable CompoundTag param2) {
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
                    return var4 != null && NbtUtils.compareNbt(this.nbt, var4.save(new CompoundTag()), true);
                }
            }
        }
    }
}
