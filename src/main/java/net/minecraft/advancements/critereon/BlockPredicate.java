package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
    private final Block block;
    private final StatePropertiesPredicate properties;
    private final NbtPredicate nbt;

    public BlockPredicate(@Nullable Tag<Block> param0, @Nullable Block param1, StatePropertiesPredicate param2, NbtPredicate param3) {
        this.tag = param0;
        this.block = param1;
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
            } else if (this.block != null && !var0.is(this.block)) {
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
            Block var2 = null;
            if (var0.has("block")) {
                ResourceLocation var3 = new ResourceLocation(GsonHelper.getAsString(var0, "block"));
                var2 = Registry.BLOCK.get(var3);
            }

            Tag<Block> var4 = null;
            if (var0.has("tag")) {
                ResourceLocation var5 = new ResourceLocation(GsonHelper.getAsString(var0, "tag"));
                var4 = SerializationTags.getInstance().getBlocks().getTag(var5);
                if (var4 == null) {
                    throw new JsonSyntaxException("Unknown block tag '" + var5 + "'");
                }
            }

            StatePropertiesPredicate var6 = StatePropertiesPredicate.fromJson(var0.get("state"));
            return new BlockPredicate(var4, var2, var6, var1);
        } else {
            return ANY;
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (this.block != null) {
                var0.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }

            if (this.tag != null) {
                var0.addProperty("tag", SerializationTags.getInstance().getBlocks().getIdOrThrow(this.tag).toString());
            }

            var0.add("nbt", this.nbt.serializeToJson());
            var0.add("state", this.properties.serializeToJson());
            return var0;
        }
    }

    public static class Builder {
        @Nullable
        private Block block;
        @Nullable
        private Tag<Block> blocks;
        private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;

        private Builder() {
        }

        public static BlockPredicate.Builder block() {
            return new BlockPredicate.Builder();
        }

        public BlockPredicate.Builder of(Block param0) {
            this.block = param0;
            return this;
        }

        public BlockPredicate.Builder of(Tag<Block> param0) {
            this.blocks = param0;
            return this;
        }

        public BlockPredicate.Builder setProperties(StatePropertiesPredicate param0) {
            this.properties = param0;
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.blocks, this.block, this.properties, this.nbt);
        }
    }
}
