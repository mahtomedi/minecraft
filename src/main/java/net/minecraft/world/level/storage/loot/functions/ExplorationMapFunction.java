package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
    public static final String DEFAULT_DECORATION_NAME = "mansion";
    public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
    public static final byte DEFAULT_ZOOM = 2;
    public static final int DEFAULT_SEARCH_RADIUS = 50;
    public static final boolean DEFAULT_SKIP_EXISTING = true;
    final TagKey<Structure> destination;
    final MapDecoration.Type mapDecoration;
    final byte zoom;
    final int searchRadius;
    final boolean skipKnownStructures;

    ExplorationMapFunction(LootItemCondition[] param0, TagKey<Structure> param1, MapDecoration.Type param2, byte param3, int param4, boolean param5) {
        super(param0);
        this.destination = param1;
        this.mapDecoration = param2;
        this.zoom = param3;
        this.searchRadius = param4;
        this.skipKnownStructures = param5;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.EXPLORATION_MAP;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (!param0.is(Items.MAP)) {
            return param0;
        } else {
            Vec3 var0 = param1.getParamOrNull(LootContextParams.ORIGIN);
            if (var0 != null) {
                ServerLevel var1 = param1.getLevel();
                BlockPos var2 = var1.findNearestMapStructure(this.destination, BlockPos.containing(var0), this.searchRadius, this.skipKnownStructures);
                if (var2 != null) {
                    ItemStack var3 = MapItem.create(var1, var2.getX(), var2.getZ(), this.zoom, true, true);
                    MapItem.renderBiomePreviewMap(var1, var3);
                    MapItemSavedData.addTargetDecoration(var3, var2, "+", this.mapDecoration);
                    return var3;
                }
            }

            return param0;
        }
    }

    public static ExplorationMapFunction.Builder makeExplorationMap() {
        return new ExplorationMapFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<ExplorationMapFunction.Builder> {
        private TagKey<Structure> destination = ExplorationMapFunction.DEFAULT_DESTINATION;
        private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
        private byte zoom = 2;
        private int searchRadius = 50;
        private boolean skipKnownStructures = true;

        protected ExplorationMapFunction.Builder getThis() {
            return this;
        }

        public ExplorationMapFunction.Builder setDestination(TagKey<Structure> param0) {
            this.destination = param0;
            return this;
        }

        public ExplorationMapFunction.Builder setMapDecoration(MapDecoration.Type param0) {
            this.mapDecoration = param0;
            return this;
        }

        public ExplorationMapFunction.Builder setZoom(byte param0) {
            this.zoom = param0;
            return this;
        }

        public ExplorationMapFunction.Builder setSearchRadius(int param0) {
            this.searchRadius = param0;
            return this;
        }

        public ExplorationMapFunction.Builder setSkipKnownStructures(boolean param0) {
            this.skipKnownStructures = param0;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new ExplorationMapFunction(
                this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures
            );
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ExplorationMapFunction> {
        public void serialize(JsonObject param0, ExplorationMapFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            if (!param1.destination.equals(ExplorationMapFunction.DEFAULT_DESTINATION)) {
                param0.addProperty("destination", param1.destination.location().toString());
            }

            if (param1.mapDecoration != ExplorationMapFunction.DEFAULT_DECORATION) {
                param0.add("decoration", param2.serialize(param1.mapDecoration.toString().toLowerCase(Locale.ROOT)));
            }

            if (param1.zoom != 2) {
                param0.addProperty("zoom", param1.zoom);
            }

            if (param1.searchRadius != 50) {
                param0.addProperty("search_radius", param1.searchRadius);
            }

            if (!param1.skipKnownStructures) {
                param0.addProperty("skip_existing_chunks", param1.skipKnownStructures);
            }

        }

        public ExplorationMapFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            TagKey<Structure> var0 = readStructure(param0);
            String var1 = param0.has("decoration") ? GsonHelper.getAsString(param0, "decoration") : "mansion";
            MapDecoration.Type var2 = ExplorationMapFunction.DEFAULT_DECORATION;

            try {
                var2 = MapDecoration.Type.valueOf(var1.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException var10) {
                ExplorationMapFunction.LOGGER
                    .error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", var1, ExplorationMapFunction.DEFAULT_DECORATION);
            }

            byte var4 = GsonHelper.getAsByte(param0, "zoom", (byte)2);
            int var5 = GsonHelper.getAsInt(param0, "search_radius", 50);
            boolean var6 = GsonHelper.getAsBoolean(param0, "skip_existing_chunks", true);
            return new ExplorationMapFunction(param2, var0, var2, var4, var5, var6);
        }

        private static TagKey<Structure> readStructure(JsonObject param0) {
            if (param0.has("destination")) {
                String var0 = GsonHelper.getAsString(param0, "destination");
                return TagKey.create(Registries.STRUCTURE, new ResourceLocation(var0));
            } else {
                return ExplorationMapFunction.DEFAULT_DESTINATION;
            }
        }
    }
}
