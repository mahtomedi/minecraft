package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicate {
    public static final BlockPredicate ANY = new BlockPredicate(null, null, StatePropertiesPredicate.ANY, NbtPredicate.ANY);
    @Nullable
    private final Tag<Block> tag;
    @Nullable
    private final Set<Block> blocks;
    private final StatePropertiesPredicate properties;
    private final NbtPredicate nbt;

    public BlockPredicate(@Nullable Tag<Block> param0, @Nullable Set<Block> param1, StatePropertiesPredicate param2, NbtPredicate param3) {
        this.tag = param0;
        this.blocks = param1;
        this.properties = param2;
        this.nbt = param3;
    }

    public boolean matches(ServerLevel param0, BlockPos param1) {
        if (this == ANY) {
            return true;
        } else if (!param0.isLoaded(param1)) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param1);
            if (this.tag != null && !var0.is(this.tag)) {
                return false;
            } else if (this.blocks != null && !this.blocks.contains(var0.getBlock())) {
                return false;
            } else if (!this.properties.matches(var0)) {
                return false;
            } else {
                if (this.nbt != NbtPredicate.ANY) {
                    BlockEntity var1 = param0.getBlockEntity(param1);
                    if (var1 == null || !this.nbt.matches(var1.save(new CompoundTag()))) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public static BlockPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "block");
            NbtPredicate var1 = NbtPredicate.fromJson(var0.get("nbt"));
            Set<Block> var2 = null;
            JsonArray var3 = GsonHelper.getAsJsonArray(var0, "blocks", null);
            if (var3 != null) {
                ImmutableSet.Builder<Block> var4 = ImmutableSet.builder();

                for(JsonElement var5 : var3) {
                    ResourceLocation var6 = new ResourceLocation(GsonHelper.convertToString(var5, "block"));
                    var4.add(Registry.BLOCK.getOptional(var6).orElseThrow(() -> new JsonSyntaxException("Unknown block id '" + var6 + "'")));
                }

                var2 = var4.build();
            }

            Tag<Block> var7 = null;
            if (var0.has("tag")) {
                ResourceLocation var8 = new ResourceLocation(GsonHelper.getAsString(var0, "tag"));
                var7 = SerializationTags.getInstance()
                    .getTagOrThrow(Registry.BLOCK_REGISTRY, var8, param0x -> new JsonSyntaxException("Unknown block tag '" + param0x + "'"));
            }

            StatePropertiesPredicate var9 = StatePropertiesPredicate.fromJson(var0.get("state"));
            return new BlockPredicate(var7, var2, var9, var1);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (this.blocks != null) {
                JsonArray var1 = new JsonArray();

                for(Block var2 : this.blocks) {
                    var1.add(Registry.BLOCK.getKey(var2).toString());
                }

                var0.add("blocks", var1);
            }

            if (this.tag != null) {
                var0.addProperty(
                    "tag",
                    SerializationTags.getInstance()
                        .getIdOrThrow(Registry.BLOCK_REGISTRY, this.tag, () -> new IllegalStateException("Unknown block tag"))
                        .toString()
                );
            }

            var0.add("nbt", this.nbt.serializeToJson());
            var0.add("state", this.properties.serializeToJson());
            return var0;
        }
    }

    public static class Builder {
        @Nullable
        private Set<Block> blocks;
        @Nullable
        private Tag<Block> tag;
        private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;

        private Builder() {
        }

        public static BlockPredicate.Builder block() {
            return new BlockPredicate.Builder();
        }

        public BlockPredicate.Builder of(Block... param0) {
            this.blocks = ImmutableSet.copyOf(param0);
            return this;
        }

        public BlockPredicate.Builder of(Iterable<Block> param0) {
            this.blocks = ImmutableSet.copyOf(param0);
            return this;
        }

        public BlockPredicate.Builder of(Tag<Block> param0) {
            this.tag = param0;
            return this;
        }

        public BlockPredicate.Builder hasNbt(CompoundTag param0) {
            this.nbt = new NbtPredicate(param0);
            return this;
        }

        public BlockPredicate.Builder setProperties(StatePropertiesPredicate param0) {
            this.properties = param0;
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.tag, this.blocks, this.properties, this.nbt);
        }
    }
}
