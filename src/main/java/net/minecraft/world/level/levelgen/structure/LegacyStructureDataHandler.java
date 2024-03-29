package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class LegacyStructureDataHandler {
    private static final Map<String, String> CURRENT_TO_LEGACY_MAP = Util.make(Maps.newHashMap(), param0 -> {
        param0.put("Village", "Village");
        param0.put("Mineshaft", "Mineshaft");
        param0.put("Mansion", "Mansion");
        param0.put("Igloo", "Temple");
        param0.put("Desert_Pyramid", "Temple");
        param0.put("Jungle_Pyramid", "Temple");
        param0.put("Swamp_Hut", "Temple");
        param0.put("Stronghold", "Stronghold");
        param0.put("Monument", "Monument");
        param0.put("Fortress", "Fortress");
        param0.put("EndCity", "EndCity");
    });
    private static final Map<String, String> LEGACY_TO_CURRENT_MAP = Util.make(Maps.newHashMap(), param0 -> {
        param0.put("Iglu", "Igloo");
        param0.put("TeDP", "Desert_Pyramid");
        param0.put("TeJP", "Jungle_Pyramid");
        param0.put("TeSH", "Swamp_Hut");
    });
    private static final Set<String> OLD_STRUCTURE_REGISTRY_KEYS = Set.of(
        "pillager_outpost",
        "mineshaft",
        "mansion",
        "jungle_pyramid",
        "desert_pyramid",
        "igloo",
        "ruined_portal",
        "shipwreck",
        "swamp_hut",
        "stronghold",
        "monument",
        "ocean_ruin",
        "fortress",
        "endcity",
        "buried_treasure",
        "village",
        "nether_fossil",
        "bastion_remnant"
    );
    private final boolean hasLegacyData;
    private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
    private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
    private final List<String> legacyKeys;
    private final List<String> currentKeys;

    public LegacyStructureDataHandler(@Nullable DimensionDataStorage param0, List<String> param1, List<String> param2) {
        this.legacyKeys = param1;
        this.currentKeys = param2;
        this.populateCaches(param0);
        boolean var0 = false;

        for(String var1 : this.currentKeys) {
            var0 |= this.dataMap.get(var1) != null;
        }

        this.hasLegacyData = var0;
    }

    public void removeIndex(long param0) {
        for(String var0 : this.legacyKeys) {
            StructureFeatureIndexSavedData var1 = this.indexMap.get(var0);
            if (var1 != null && var1.hasUnhandledIndex(param0)) {
                var1.removeIndex(param0);
                var1.setDirty();
            }
        }

    }

    public CompoundTag updateFromLegacy(CompoundTag param0) {
        CompoundTag var0 = param0.getCompound("Level");
        ChunkPos var1 = new ChunkPos(var0.getInt("xPos"), var0.getInt("zPos"));
        if (this.isUnhandledStructureStart(var1.x, var1.z)) {
            param0 = this.updateStructureStart(param0, var1);
        }

        CompoundTag var2 = var0.getCompound("Structures");
        CompoundTag var3 = var2.getCompound("References");

        for(String var4 : this.currentKeys) {
            boolean var5 = OLD_STRUCTURE_REGISTRY_KEYS.contains(var4.toLowerCase(Locale.ROOT));
            if (!var3.contains(var4, 12) && var5) {
                int var6 = 8;
                LongList var7 = new LongArrayList();

                for(int var8 = var1.x - 8; var8 <= var1.x + 8; ++var8) {
                    for(int var9 = var1.z - 8; var9 <= var1.z + 8; ++var9) {
                        if (this.hasLegacyStart(var8, var9, var4)) {
                            var7.add(ChunkPos.asLong(var8, var9));
                        }
                    }
                }

                var3.putLongArray(var4, (List<Long>)var7);
            }
        }

        var2.put("References", var3);
        var0.put("Structures", var2);
        param0.put("Level", var0);
        return param0;
    }

    private boolean hasLegacyStart(int param0, int param1, String param2) {
        if (!this.hasLegacyData) {
            return false;
        } else {
            return this.dataMap.get(param2) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(param2)).hasStartIndex(ChunkPos.asLong(param0, param1));
        }
    }

    private boolean isUnhandledStructureStart(int param0, int param1) {
        if (!this.hasLegacyData) {
            return false;
        } else {
            for(String var0 : this.currentKeys) {
                if (this.dataMap.get(var0) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(var0)).hasUnhandledIndex(ChunkPos.asLong(param0, param1))) {
                    return true;
                }
            }

            return false;
        }
    }

    private CompoundTag updateStructureStart(CompoundTag param0, ChunkPos param1) {
        CompoundTag var0 = param0.getCompound("Level");
        CompoundTag var1 = var0.getCompound("Structures");
        CompoundTag var2 = var1.getCompound("Starts");

        for(String var3 : this.currentKeys) {
            Long2ObjectMap<CompoundTag> var4 = this.dataMap.get(var3);
            if (var4 != null) {
                long var5 = param1.toLong();
                if (this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(var3)).hasUnhandledIndex(var5)) {
                    CompoundTag var6 = var4.get(var5);
                    if (var6 != null) {
                        var2.put(var3, var6);
                    }
                }
            }
        }

        var1.put("Starts", var2);
        var0.put("Structures", var1);
        param0.put("Level", var0);
        return param0;
    }

    private void populateCaches(@Nullable DimensionDataStorage param0) {
        if (param0 != null) {
            for(String var0 : this.legacyKeys) {
                CompoundTag var1 = new CompoundTag();

                try {
                    var1 = param0.readTagFromDisk(var0, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES, 1493).getCompound("data").getCompound("Features");
                    if (var1.isEmpty()) {
                        continue;
                    }
                } catch (IOException var131) {
                }

                for(String var2 : var1.getAllKeys()) {
                    CompoundTag var3 = var1.getCompound(var2);
                    long var4 = ChunkPos.asLong(var3.getInt("ChunkX"), var3.getInt("ChunkZ"));
                    ListTag var5 = var3.getList("Children", 10);
                    if (!var5.isEmpty()) {
                        String var6 = var5.getCompound(0).getString("id");
                        String var7 = LEGACY_TO_CURRENT_MAP.get(var6);
                        if (var7 != null) {
                            var3.putString("id", var7);
                        }
                    }

                    String var8 = var3.getString("id");
                    this.dataMap.computeIfAbsent(var8, param0x -> new Long2ObjectOpenHashMap()).put(var4, var3);
                }

                String var9 = var0 + "_index";
                StructureFeatureIndexSavedData var10 = param0.computeIfAbsent(StructureFeatureIndexSavedData.factory(), var9);
                if (!var10.getAll().isEmpty()) {
                    this.indexMap.put(var0, var10);
                } else {
                    StructureFeatureIndexSavedData var11 = new StructureFeatureIndexSavedData();
                    this.indexMap.put(var0, var11);

                    for(String var12 : var1.getAllKeys()) {
                        CompoundTag var13 = var1.getCompound(var12);
                        var11.addIndex(ChunkPos.asLong(var13.getInt("ChunkX"), var13.getInt("ChunkZ")));
                    }

                    var11.setDirty();
                }
            }

        }
    }

    public static LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> param0, @Nullable DimensionDataStorage param1) {
        if (param0 == Level.OVERWORLD) {
            return new LegacyStructureDataHandler(
                param1,
                ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"),
                ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument")
            );
        } else if (param0 == Level.NETHER) {
            List<String> var0 = ImmutableList.of("Fortress");
            return new LegacyStructureDataHandler(param1, var0, var0);
        } else if (param0 == Level.END) {
            List<String> var1 = ImmutableList.of("EndCity");
            return new LegacyStructureDataHandler(param1, var1, var1);
        } else {
            throw new RuntimeException(String.format(Locale.ROOT, "Unknown dimension type : %s", param0));
        }
    }
}
