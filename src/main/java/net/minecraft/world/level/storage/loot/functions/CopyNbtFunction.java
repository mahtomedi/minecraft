package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;

public class CopyNbtFunction extends LootItemConditionalFunction {
    public static final Codec<CopyNbtFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        NbtProviders.CODEC.fieldOf("source").forGetter(param0x -> param0x.source),
                        CopyNbtFunction.CopyOperation.CODEC.listOf().fieldOf("ops").forGetter(param0x -> param0x.operations)
                    )
                )
                .apply(param0, CopyNbtFunction::new)
    );
    private final NbtProvider source;
    private final List<CopyNbtFunction.CopyOperation> operations;

    CopyNbtFunction(List<LootItemCondition> param0, NbtProvider param1, List<CopyNbtFunction.CopyOperation> param2) {
        super(param0);
        this.source = param1;
        this.operations = List.copyOf(param2);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_NBT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.source.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Tag var0 = this.source.get(param1);
        if (var0 != null) {
            this.operations.forEach(param2 -> param2.apply(param0::getOrCreateTag, var0));
        }

        return param0;
    }

    public static CopyNbtFunction.Builder copyData(NbtProvider param0) {
        return new CopyNbtFunction.Builder(param0);
    }

    public static CopyNbtFunction.Builder copyData(LootContext.EntityTarget param0) {
        return new CopyNbtFunction.Builder(ContextNbtProvider.forContextEntity(param0));
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyNbtFunction.Builder> {
        private final NbtProvider source;
        private final List<CopyNbtFunction.CopyOperation> ops = Lists.newArrayList();

        Builder(NbtProvider param0) {
            this.source = param0;
        }

        public CopyNbtFunction.Builder copy(String param0, String param1, CopyNbtFunction.MergeStrategy param2) {
            try {
                this.ops.add(new CopyNbtFunction.CopyOperation(CopyNbtFunction.Path.of(param0), CopyNbtFunction.Path.of(param1), param2));
                return this;
            } catch (CommandSyntaxException var5) {
                throw new IllegalArgumentException(var5);
            }
        }

        public CopyNbtFunction.Builder copy(String param0, String param1) {
            return this.copy(param0, param1, CopyNbtFunction.MergeStrategy.REPLACE);
        }

        protected CopyNbtFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyNbtFunction(this.getConditions(), this.source, this.ops);
        }
    }

    static record CopyOperation(CopyNbtFunction.Path sourcePath, CopyNbtFunction.Path targetPath, CopyNbtFunction.MergeStrategy op) {
        public static final Codec<CopyNbtFunction.CopyOperation> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        CopyNbtFunction.Path.CODEC.fieldOf("source").forGetter(CopyNbtFunction.CopyOperation::sourcePath),
                        CopyNbtFunction.Path.CODEC.fieldOf("target").forGetter(CopyNbtFunction.CopyOperation::targetPath),
                        CopyNbtFunction.MergeStrategy.CODEC.fieldOf("op").forGetter(CopyNbtFunction.CopyOperation::op)
                    )
                    .apply(param0, CopyNbtFunction.CopyOperation::new)
        );

        public void apply(Supplier<Tag> param0, Tag param1) {
            try {
                List<Tag> var0 = this.sourcePath.path().get(param1);
                if (!var0.isEmpty()) {
                    this.op.merge(param0.get(), this.targetPath.path(), var0);
                }
            } catch (CommandSyntaxException var4) {
            }

        }
    }

    public static enum MergeStrategy implements StringRepresentable {
        REPLACE("replace") {
            @Override
            public void merge(Tag param0, NbtPathArgument.NbtPath param1, List<Tag> param2) throws CommandSyntaxException {
                param1.set(param0, Iterables.getLast(param2));
            }
        },
        APPEND("append") {
            @Override
            public void merge(Tag param0, NbtPathArgument.NbtPath param1, List<Tag> param2) throws CommandSyntaxException {
                List<Tag> var0 = param1.getOrCreate(param0, ListTag::new);
                var0.forEach(param1x -> {
                    if (param1x instanceof ListTag) {
                        param2.forEach(param1xx -> ((ListTag)param1x).add(param1xx.copy()));
                    }

                });
            }
        },
        MERGE("merge") {
            @Override
            public void merge(Tag param0, NbtPathArgument.NbtPath param1, List<Tag> param2) throws CommandSyntaxException {
                List<Tag> var0 = param1.getOrCreate(param0, CompoundTag::new);
                var0.forEach(param1x -> {
                    if (param1x instanceof CompoundTag) {
                        param2.forEach(param1xx -> {
                            if (param1xx instanceof CompoundTag) {
                                ((CompoundTag)param1x).merge((CompoundTag)param1xx);
                            }

                        });
                    }

                });
            }
        };

        public static final Codec<CopyNbtFunction.MergeStrategy> CODEC = StringRepresentable.fromEnum(CopyNbtFunction.MergeStrategy::values);
        private final String name;

        public abstract void merge(Tag var1, NbtPathArgument.NbtPath var2, List<Tag> var3) throws CommandSyntaxException;

        MergeStrategy(String param0) {
            this.name = param0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    static record Path(String string, NbtPathArgument.NbtPath path) {
        public static final Codec<CopyNbtFunction.Path> CODEC = Codec.STRING.comapFlatMap(param0 -> {
            try {
                return DataResult.success(of(param0));
            } catch (CommandSyntaxException var2) {
                return DataResult.error(() -> "Failed to parse path " + param0 + ": " + var2.getMessage());
            }
        }, CopyNbtFunction.Path::string);

        public static CopyNbtFunction.Path of(String param0) throws CommandSyntaxException {
            NbtPathArgument.NbtPath var0 = new NbtPathArgument().parse(new StringReader(param0));
            return new CopyNbtFunction.Path(param0, var0);
        }
    }
}
