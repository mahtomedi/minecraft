package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNbtFunction extends LootItemConditionalFunction {
    private final CopyNbtFunction.DataSource source;
    private final List<CopyNbtFunction.CopyOperation> operations;
    private static final Function<Entity, Tag> ENTITY_GETTER = NbtPredicate::getEntityTagToCompare;
    private static final Function<BlockEntity, Tag> BLOCK_ENTITY_GETTER = param0 -> param0.save(new CompoundTag());

    private CopyNbtFunction(LootItemCondition[] param0, CopyNbtFunction.DataSource param1, List<CopyNbtFunction.CopyOperation> param2) {
        super(param0);
        this.source = param1;
        this.operations = ImmutableList.copyOf(param2);
    }

    private static NbtPathArgument.NbtPath compileNbtPath(String param0) {
        try {
            return new NbtPathArgument().parse(new StringReader(param0));
        } catch (CommandSyntaxException var2) {
            throw new IllegalArgumentException("Failed to parse path " + param0, var2);
        }
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Tag var0 = this.source.getter.apply(param1);
        if (var0 != null) {
            this.operations.forEach(param2 -> param2.apply(param0::getOrCreateTag, var0));
        }

        return param0;
    }

    public static CopyNbtFunction.Builder copyData(CopyNbtFunction.DataSource param0) {
        return new CopyNbtFunction.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyNbtFunction.Builder> {
        private final CopyNbtFunction.DataSource source;
        private final List<CopyNbtFunction.CopyOperation> ops = Lists.newArrayList();

        private Builder(CopyNbtFunction.DataSource param0) {
            this.source = param0;
        }

        public CopyNbtFunction.Builder copy(String param0, String param1, CopyNbtFunction.MergeStrategy param2) {
            this.ops.add(new CopyNbtFunction.CopyOperation(param0, param1, param2));
            return this;
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

    static class CopyOperation {
        private final String sourcePathText;
        private final NbtPathArgument.NbtPath sourcePath;
        private final String targetPathText;
        private final NbtPathArgument.NbtPath targetPath;
        private final CopyNbtFunction.MergeStrategy op;

        private CopyOperation(String param0, String param1, CopyNbtFunction.MergeStrategy param2) {
            this.sourcePathText = param0;
            this.sourcePath = CopyNbtFunction.compileNbtPath(param0);
            this.targetPathText = param1;
            this.targetPath = CopyNbtFunction.compileNbtPath(param1);
            this.op = param2;
        }

        public void apply(Supplier<Tag> param0, Tag param1) {
            try {
                List<Tag> var0 = this.sourcePath.get(param1);
                if (!var0.isEmpty()) {
                    this.op.merge(param0.get(), this.targetPath, var0);
                }
            } catch (CommandSyntaxException var4) {
            }

        }

        public JsonObject toJson() {
            JsonObject var0 = new JsonObject();
            var0.addProperty("source", this.sourcePathText);
            var0.addProperty("target", this.targetPathText);
            var0.addProperty("op", this.op.name);
            return var0;
        }

        public static CopyNbtFunction.CopyOperation fromJson(JsonObject param0) {
            String var0 = GsonHelper.getAsString(param0, "source");
            String var1 = GsonHelper.getAsString(param0, "target");
            CopyNbtFunction.MergeStrategy var2 = CopyNbtFunction.MergeStrategy.getByName(GsonHelper.getAsString(param0, "op"));
            return new CopyNbtFunction.CopyOperation(var0, var1, var2);
        }
    }

    public static enum DataSource {
        THIS("this", LootContextParams.THIS_ENTITY, CopyNbtFunction.ENTITY_GETTER),
        KILLER("killer", LootContextParams.KILLER_ENTITY, CopyNbtFunction.ENTITY_GETTER),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER, CopyNbtFunction.ENTITY_GETTER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY, CopyNbtFunction.BLOCK_ENTITY_GETTER);

        public final String name;
        public final LootContextParam<?> param;
        public final Function<LootContext, Tag> getter;

        private <T> DataSource(String param0, LootContextParam<T> param1, Function<? super T, Tag> param2) {
            this.name = param0;
            this.param = param1;
            this.getter = param2x -> {
                T var0 = param2x.getParamOrNull(param1);
                return var0 != null ? param2.apply(var0) : null;
            };
        }

        public static CopyNbtFunction.DataSource getByName(String param0) {
            for(CopyNbtFunction.DataSource var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            throw new IllegalArgumentException("Invalid tag source " + param0);
        }
    }

    public static enum MergeStrategy {
        REPLACE("replace") {
            @Override
            public void merge(Tag param0, NbtPathArgument.NbtPath param1, List<Tag> param2) throws CommandSyntaxException {
                param1.set(param0, Iterables.getLast(param2)::copy);
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

        private final String name;

        public abstract void merge(Tag var1, NbtPathArgument.NbtPath var2, List<Tag> var3) throws CommandSyntaxException;

        private MergeStrategy(String param0) {
            this.name = param0;
        }

        public static CopyNbtFunction.MergeStrategy getByName(String param0) {
            for(CopyNbtFunction.MergeStrategy var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            throw new IllegalArgumentException("Invalid merge strategy" + param0);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNbtFunction> {
        public Serializer() {
            super(new ResourceLocation("copy_nbt"), CopyNbtFunction.class);
        }

        public void serialize(JsonObject param0, CopyNbtFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("source", param1.source.name);
            JsonArray var0 = new JsonArray();
            param1.operations.stream().map(CopyNbtFunction.CopyOperation::toJson).forEach(var0::add);
            param0.add("ops", var0);
        }

        public CopyNbtFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            CopyNbtFunction.DataSource var0 = CopyNbtFunction.DataSource.getByName(GsonHelper.getAsString(param0, "source"));
            List<CopyNbtFunction.CopyOperation> var1 = Lists.newArrayList();

            for(JsonElement var3 : GsonHelper.getAsJsonArray(param0, "ops")) {
                JsonObject var4 = GsonHelper.convertToJsonObject(var3, "op");
                var1.add(CopyNbtFunction.CopyOperation.fromJson(var4));
            }

            return new CopyNbtFunction(param2, var0, var1);
        }
    }
}
