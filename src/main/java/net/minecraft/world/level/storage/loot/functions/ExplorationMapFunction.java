package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<?> DEFAULT_FEATURE = StructureFeature.BURIED_TREASURE;
    public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
    private final StructureFeature<?> destination;
    private final MapDecoration.Type mapDecoration;
    private final byte zoom;
    private final int searchRadius;
    private final boolean skipKnownStructures;

    private ExplorationMapFunction(LootItemCondition[] param0, StructureFeature<?> param1, MapDecoration.Type param2, byte param3, int param4, boolean param5) {
        super(param0);
        this.destination = param1;
        this.mapDecoration = param2;
        this.zoom = param3;
        this.searchRadius = param4;
        this.skipKnownStructures = param5;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_POS);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.getItem() != Items.MAP) {
            return param0;
        } else {
            BlockPos var0 = param1.getParamOrNull(LootContextParams.BLOCK_POS);
            if (var0 != null) {
                ServerLevel var1 = param1.getLevel();
                BlockPos var2 = var1.findNearestMapFeature(this.destination, var0, this.searchRadius, this.skipKnownStructures);
                if (var2 != null) {
                    ItemStack var3 = MapItem.create(var1, var2.getX(), var2.getZ(), this.zoom, true, true);
                    MapItem.renderBiomePreviewMap(var1, var3);
                    MapItemSavedData.addTargetDecoration(var3, var2, "+", this.mapDecoration);
                    var3.setHoverName(new TranslatableComponent("filled_map." + this.destination.getFeatureName().toLowerCase(Locale.ROOT)));
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
        private StructureFeature<?> destination = ExplorationMapFunction.DEFAULT_FEATURE;
        private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
        private byte zoom = 2;
        private int searchRadius = 50;
        private boolean skipKnownStructures = true;

        protected ExplorationMapFunction.Builder getThis() {
            return this;
        }

        public ExplorationMapFunction.Builder setDestination(StructureFeature<?> param0) {
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
        protected Serializer() {
            super(new ResourceLocation("exploration_map"), ExplorationMapFunction.class);
        }

        public void serialize(JsonObject param0, ExplorationMapFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            if (!param1.destination.equals(ExplorationMapFunction.DEFAULT_FEATURE)) {
                param0.add("destination", param2.serialize(param1.destination.getFeatureName()));
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
            StructureFeature<?> var0 = readStructure(param0);
            String var1 = param0.has("decoration") ? GsonHelper.getAsString(param0, "decoration") : "mansion";
            MapDecoration.Type var2 = ExplorationMapFunction.DEFAULT_DECORATION;

            try {
                var2 = MapDecoration.Type.valueOf(var1.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException var10) {
                ExplorationMapFunction.LOGGER
                    .error("Error while parsing loot table decoration entry. Found {}. Defaulting to " + ExplorationMapFunction.DEFAULT_DECORATION, var1);
            }

            byte var4 = GsonHelper.getAsByte(param0, "zoom", (byte)2);
            int var5 = GsonHelper.getAsInt(param0, "search_radius", 50);
            boolean var6 = GsonHelper.getAsBoolean(param0, "skip_existing_chunks", true);
            return new ExplorationMapFunction(param2, var0, var2, var4, var5, var6);
        }

        private static StructureFeature<?> readStructure(JsonObject param0) {
            if (param0.has("destination")) {
                String var0 = GsonHelper.getAsString(param0, "destination");
                StructureFeature<?> var1 = StructureFeature.STRUCTURES_REGISTRY.get(var0.toLowerCase(Locale.ROOT));
                if (var1 != null) {
                    return var1;
                }
            }

            return ExplorationMapFunction.DEFAULT_FEATURE;
        }
    }
}
