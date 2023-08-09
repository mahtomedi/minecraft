package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
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

public class ExplorationMapFunction extends LootItemConditionalFunction {
    public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
    public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
    public static final byte DEFAULT_ZOOM = 2;
    public static final int DEFAULT_SEARCH_RADIUS = 50;
    public static final boolean DEFAULT_SKIP_EXISTING = true;
    public static final Codec<ExplorationMapFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        ExtraCodecs.strictOptionalField(TagKey.codec(Registries.STRUCTURE), "destination", DEFAULT_DESTINATION)
                            .forGetter(param0x -> param0x.destination),
                        MapDecoration.Type.CODEC.optionalFieldOf("decoration", DEFAULT_DECORATION).forGetter(param0x -> param0x.mapDecoration),
                        ExtraCodecs.strictOptionalField(Codec.BYTE, "zoom", (byte)2).forGetter(param0x -> param0x.zoom),
                        ExtraCodecs.strictOptionalField(Codec.INT, "search_radius", 50).forGetter(param0x -> param0x.searchRadius),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "skip_existing_chunks", true).forGetter(param0x -> param0x.skipKnownStructures)
                    )
                )
                .apply(param0, ExplorationMapFunction::new)
    );
    private final TagKey<Structure> destination;
    private final MapDecoration.Type mapDecoration;
    private final byte zoom;
    private final int searchRadius;
    private final boolean skipKnownStructures;

    ExplorationMapFunction(List<LootItemCondition> param0, TagKey<Structure> param1, MapDecoration.Type param2, byte param3, int param4, boolean param5) {
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
}
