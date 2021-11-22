package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

public class ChunkHeightAndBiomeFix extends DataFix {
    public static final String DATAFIXER_CONTEXT_TAG = "__context";
    private static final String NAME = "ChunkHeightAndBiomeFix";
    private static final int OLD_SECTION_COUNT = 16;
    private static final int NEW_SECTION_COUNT = 24;
    private static final int NEW_MIN_SECTION_Y = -4;
    public static final int BLOCKS_PER_SECTION = 4096;
    private static final int LONGS_PER_SECTION = 64;
    private static final int HEIGHTMAP_BITS = 9;
    private static final long HEIGHTMAP_MASK = 511L;
    private static final int HEIGHTMAP_OFFSET = 64;
    private static final String[] HEIGHTMAP_TYPES = new String[]{
        "WORLD_SURFACE_WG", "WORLD_SURFACE", "WORLD_SURFACE_IGNORE_SNOW", "OCEAN_FLOOR_WG", "OCEAN_FLOOR", "MOTION_BLOCKING", "MOTION_BLOCKING_NO_LEAVES"
    };
    private static final Set<String> STATUS_IS_OR_AFTER_SURFACE = Set.of(
        "surface", "carvers", "liquid_carvers", "features", "light", "spawn", "heightmaps", "full"
    );
    private static final Set<String> STATUS_IS_OR_AFTER_NOISE = Set.of(
        "noise", "surface", "carvers", "liquid_carvers", "features", "light", "spawn", "heightmaps", "full"
    );
    private static final Set<String> BLOCKS_BEFORE_FEATURE_STATUS = Set.of(
        "minecraft:air",
        "minecraft:basalt",
        "minecraft:bedrock",
        "minecraft:blackstone",
        "minecraft:calcite",
        "minecraft:cave_air",
        "minecraft:coarse_dirt",
        "minecraft:crimson_nylium",
        "minecraft:dirt",
        "minecraft:end_stone",
        "minecraft:grass_block",
        "minecraft:gravel",
        "minecraft:ice",
        "minecraft:lava",
        "minecraft:mycelium",
        "minecraft:nether_wart_block",
        "minecraft:netherrack",
        "minecraft:orange_terracotta",
        "minecraft:packed_ice",
        "minecraft:podzol",
        "minecraft:powder_snow",
        "minecraft:red_sand",
        "minecraft:red_sandstone",
        "minecraft:sand",
        "minecraft:sandstone",
        "minecraft:snow_block",
        "minecraft:soul_sand",
        "minecraft:soul_soil",
        "minecraft:stone",
        "minecraft:terracotta",
        "minecraft:warped_nylium",
        "minecraft:warped_wart_block",
        "minecraft:water",
        "minecraft:white_terracotta"
    );
    private static final int BIOME_CONTAINER_LAYER_SIZE = 16;
    private static final int BIOME_CONTAINER_SIZE = 64;
    private static final int BIOME_CONTAINER_TOP_LAYER_OFFSET = 1008;
    public static final String DEFAULT_BIOME = "minecraft:plains";
    private static final Int2ObjectMap<String> BIOMES_BY_ID = new Int2ObjectOpenHashMap<>();

    public ChunkHeightAndBiomeFix(Schema param0) {
        super(param0, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("Level");
        OpticFinder<?> var2 = var1.type().findField("Sections");
        Schema var3 = this.getOutputSchema();
        Type<?> var4 = var3.getType(References.CHUNK);
        Type<?> var5 = var4.findField("Level").type();
        Type<?> var6 = var5.findField("Sections").type();
        return this.fixTypeEverywhereTyped(
            "ChunkHeightAndBiomeFix",
            var0,
            var4,
            param4 -> param4.updateTyped(
                    var1,
                    var5,
                    param3x -> {
                        Dynamic<?> var0x = param3x.get(DSL.remainderFinder());
                        OptionalDynamic<?> var1x = param4.get(DSL.remainderFinder()).get("__context");
                        String var2x = var1x.get("dimension").asString().result().orElse("");
                        String var3x = var1x.get("generator").asString().result().orElse("");
                        boolean var4x = "minecraft:overworld".equals(var2x);
                        MutableBoolean var5x = new MutableBoolean();
                        int var6x = var4x ? -4 : 0;
                        Dynamic<?>[] var7x = getBiomeContainers(var0x, var4x, var6x, var5x);
                        Dynamic<?> var8 = makePalettedContainer(
                            var0x.createList(Stream.of(var0x.createMap(ImmutableMap.of(var0x.createString("Name"), var0x.createString("minecraft:air")))))
                        );
                        Set<String> var9 = Sets.newHashSet();
                        MutableObject<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> var10 = new MutableObject<>(() -> null);
                        param3x = param3x.updateTyped(
                            var2,
                            var6,
                            param7 -> {
                                IntSet var0xx = new IntOpenHashSet();
                                Dynamic<?> var1xx = param7.write().result().orElseThrow(() -> new IllegalStateException("Malformed Chunk.Level.Sections"));
                                List<Dynamic<?>> var2xx = var1xx.asStream().map(param6x -> {
                                    int var0xxx = param6x.get("Y").asInt(0);
                                    Dynamic<?> var1xxx = DataFixUtils.orElse(param6x.get("Palette").result().flatMap(param2x -> {
                                        param2x.asStream().map(param0x -> param0x.get("Name").asString("minecraft:air")).forEach(var9::add);
                                        return param6x.get("BlockStates").result().map(param1x -> makeOptimizedPalettedContainer(param2x, param1x));
                                    }), var8);
                                    Dynamic<?> var2xxx = param6x;
                                    int var3xxx = var0xxx - var6x;
                                    if (var3xxx >= 0 && var3xxx < var7x.length) {
                                        var2xxx = param6x.set("biomes", var7x[var3xxx]);
                                    }
            
                                    var0xx.add(var0xxx);
                                    if (param6x.get("Y").asInt(Integer.MAX_VALUE) == 0) {
                                        var10.setValue(() -> {
                                            List<? extends Dynamic<?>> var0xxxx = var1xxx.get("palette").asList(Function.identity());
                                            long[] var1xxxx = var1xxx.get("data").asLongStream().toArray();
                                            return new ChunkProtoTickListFix.PoorMansPalettedContainer(var0xxxx, var1xxxx);
                                        });
                                    }
            
                                    return var2xxx.set("block_states", var1xxx).remove("Palette").remove("BlockStates");
                                }).collect(Collectors.toCollection(ArrayList::new));
            
                                for(int var3xx = 0; var3xx < var7x.length; ++var3xx) {
                                    int var4xx = var3xx + var6x;
                                    if (var0xx.add(var4xx)) {
                                        Dynamic<?> var5xx = var0x.createMap(Map.of(var0x.createString("Y"), var0x.createInt(var4xx)));
                                        var5xx = var5xx.set("block_states", var8);
                                        var5xx = var5xx.set("biomes", var7x[var3xx]);
                                        var2xx.add(var5xx);
                                    }
                                }
            
                                return var6.readTyped(var0x.createList(var2xx.stream()))
                                    .result()
                                    .orElseThrow(() -> new IllegalStateException("ChunkHeightAndBiomeFix failed."))
                                    .getFirst();
                            }
                        );
                        return param3x.update(DSL.remainderFinder(), param5 -> {
                            if (var4x) {
                                param5 = this.predictChunkStatusBeforeSurface(param5, var9);
                            }
        
                            return updateChunkTag(param5, var4x, var5x.booleanValue(), "minecraft:noise".equals(var3x), var10.getValue());
                        });
                    }
                )
        );
    }

    private Dynamic<?> predictChunkStatusBeforeSurface(Dynamic<?> param0, Set<String> param1) {
        return param0.update("Status", param1x -> {
            String var0 = param1x.asString("empty");
            if (STATUS_IS_OR_AFTER_SURFACE.contains(var0)) {
                return param1x;
            } else {
                param1.remove("minecraft:air");
                boolean var1x = !param1.isEmpty();
                param1.removeAll(BLOCKS_BEFORE_FEATURE_STATUS);
                boolean var2x = !param1.isEmpty();
                if (var2x) {
                    return param1x.createString("liquid_carvers");
                } else if ("noise".equals(var0) || var1x) {
                    return param1x.createString("noise");
                } else {
                    return "biomes".equals(var0) ? param1x.createString("structure_references") : param1x;
                }
            }
        });
    }

    private static Dynamic<?>[] getBiomeContainers(Dynamic<?> param0, boolean param1, int param2, MutableBoolean param3) {
        Dynamic<?>[] var0 = new Dynamic[param1 ? 24 : 16];
        int[] var1 = param0.get("Biomes").asIntStreamOpt().result().map(IntStream::toArray).orElse(null);
        if (var1 != null && var1.length == 1536) {
            param3.setValue(true);

            for(int var2 = 0; var2 < 24; ++var2) {
                var0[var2] = makeBiomeContainer(param0, param2x -> getOldBiome(var1, var2 * 64 + param2x));
            }
        } else if (var1 != null && var1.length == 1024) {
            for(int var4 = 0; var4 < 16; ++var4) {
                int var5 = var4 - param2;
                var0[var5] = makeBiomeContainer(param0, param2x -> getOldBiome(var1, var4 * 64 + param2x));
            }

            if (param1) {
                Dynamic<?> var7 = makeBiomeContainer(param0, param1x -> getOldBiome(var1, param1x % 16));
                Dynamic<?> var8 = makeBiomeContainer(param0, param1x -> getOldBiome(var1, param1x % 16 + 1008));

                for(int var9 = 0; var9 < 4; ++var9) {
                    var0[var9] = var7;
                }

                for(int var10 = 20; var10 < 24; ++var10) {
                    var0[var10] = var8;
                }
            }
        } else {
            Arrays.fill(var0, makePalettedContainer(param0.createList(Stream.of(param0.createString("minecraft:plains")))));
        }

        return var0;
    }

    private static int getOldBiome(int[] param0, int param1) {
        return param0[param1] & 0xFF;
    }

    private static Dynamic<?> updateChunkTag(
        Dynamic<?> param0, boolean param1, boolean param2, boolean param3, Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> param4
    ) {
        param0 = param0.remove("Biomes");
        if (!param1) {
            return updateCarvingMasks(param0, 16, 0);
        } else if (param2) {
            return updateCarvingMasks(param0, 24, 0);
        } else {
            param0 = updateHeightmaps(param0);
            param0 = addPaddingEntries(param0, "Lights");
            param0 = addPaddingEntries(param0, "LiquidsToBeTicked");
            param0 = addPaddingEntries(param0, "PostProcessing");
            param0 = addPaddingEntries(param0, "ToBeTicked");
            param0 = updateCarvingMasks(param0, 24, 4);
            param0 = param0.update("UpgradeData", ChunkHeightAndBiomeFix::shiftUpgradeData);
            if (!param3) {
                return param0;
            } else {
                Optional<? extends Dynamic<?>> var0 = param0.get("Status").result();
                if (var0.isPresent()) {
                    Dynamic<?> var1 = var0.get();
                    String var2 = var1.asString("");
                    if (!"empty".equals(var2)) {
                        param0 = param0.set(
                            "blending_data",
                            param0.createMap(ImmutableMap.of(param0.createString("old_noise"), param0.createBoolean(STATUS_IS_OR_AFTER_NOISE.contains(var2))))
                        );
                        ChunkProtoTickListFix.PoorMansPalettedContainer var3 = param4.get();
                        if (var3 != null) {
                            BitSet var4 = new BitSet(256);
                            boolean var5 = var2.equals("noise");

                            for(int var6 = 0; var6 < 16; ++var6) {
                                for(int var7 = 0; var7 < 16; ++var7) {
                                    Dynamic<?> var8 = var3.get(var7, 0, var6);
                                    boolean var9 = var8 != null && "minecraft:bedrock".equals(var8.get("Name").asString(""));
                                    boolean var10 = var8 != null && "minecraft:air".equals(var8.get("Name").asString(""));
                                    if (var10) {
                                        var4.set(var6 * 16 + var7);
                                    }

                                    var5 |= var9;
                                }
                            }

                            if (var5 && var4.cardinality() != var4.size()) {
                                Dynamic<?> var11 = "full".equals(var2) ? param0.createString("heightmaps") : var1;
                                param0 = param0.set(
                                    "below_zero_retrogen",
                                    param0.createMap(
                                        ImmutableMap.of(
                                            param0.createString("target_status"),
                                            var11,
                                            param0.createString("missing_bedrock"),
                                            param0.createLongList(LongStream.of(var4.toLongArray()))
                                        )
                                    )
                                );
                                param0 = param0.set("Status", param0.createString("empty"));
                            }

                            param0 = param0.set("isLightOn", param0.createBoolean(false));
                        }
                    }
                }

                return param0;
            }
        }
    }

    private static <T> Dynamic<T> shiftUpgradeData(Dynamic<T> param0x) {
        return param0x.update("Indices", param0xx -> {
            Map<Dynamic<?>, Dynamic<?>> var0x = new HashMap<>();
            param0xx.getMapValues().result().ifPresent(param1x -> param1x.forEach((param1xx, param2x) -> {
                    try {
                        param1xx.asString().result().map(Integer::parseInt).ifPresent(param3x -> {
                            int var0xx = param3x - -4;
                            var0x.put(param1xx.createString(Integer.toString(var0xx)), param2x);
                        });
                    } catch (NumberFormatException var4) {
                    }

                }));
            return param0xx.createMap(var0x);
        });
    }

    private static Dynamic<?> updateCarvingMasks(Dynamic<?> param0, int param1, int param2) {
        Dynamic<?> var0 = param0.get("CarvingMasks").orElseEmptyMap();
        var0 = var0.updateMapValues(param3 -> {
            long[] var0x = BitSet.valueOf(param3.getSecond().asByteBuffer().array()).toLongArray();
            long[] var1x = new long[64 * param1];
            System.arraycopy(var0x, 0, var1x, 64 * param2, var0x.length);
            return Pair.of(param3.getFirst(), param0.createLongList(LongStream.of(var1x)));
        });
        return param0.set("CarvingMasks", var0);
    }

    private static Dynamic<?> addPaddingEntries(Dynamic<?> param0, String param1) {
        List<Dynamic<?>> var0 = param0.get(param1).orElseEmptyList().asStream().collect(Collectors.toCollection(ArrayList::new));
        if (var0.size() == 24) {
            return param0;
        } else {
            Dynamic<?> var1 = param0.emptyList();

            for(int var2 = 0; var2 < 4; ++var2) {
                var0.add(0, var1);
                var0.add(var1);
            }

            return param0.set(param1, param0.createList(var0.stream()));
        }
    }

    private static Dynamic<?> updateHeightmaps(Dynamic<?> param0) {
        return param0.update("Heightmaps", param0x -> {
            for(String var0x : HEIGHTMAP_TYPES) {
                param0x = param0x.update(var0x, ChunkHeightAndBiomeFix::getFixedHeightmap);
            }

            return param0x;
        });
    }

    private static Dynamic<?> getFixedHeightmap(Dynamic<?> param0) {
        return param0.createLongList(param0.asLongStream().map(param0x -> {
            long var0x = 0L;

            for(int var1 = 0; var1 + 9 <= 64; var1 += 9) {
                long var2 = param0x >> var1 & 511L;
                long var3;
                if (var2 == 0L) {
                    var3 = 0L;
                } else {
                    var3 = Math.min(var2 + 64L, 511L);
                }

                var0x |= var3 << var1;
            }

            return var0x;
        }));
    }

    private static Dynamic<?> makeBiomeContainer(Dynamic<?> param0, Int2IntFunction param1) {
        Int2IntMap var0 = new Int2IntLinkedOpenHashMap();

        for(int var1 = 0; var1 < 64; ++var1) {
            int var2 = param1.applyAsInt(var1);
            if (!var0.containsKey(var2)) {
                var0.put(var2, var0.size());
            }
        }

        Dynamic<?> var3 = param0.createList(
            var0.keySet().stream().map(param1x -> param0.createString(BIOMES_BY_ID.getOrDefault(param1x.intValue(), "minecraft:plains")))
        );
        int var4 = ceillog2(var0.size());
        if (var4 == 0) {
            return makePalettedContainer(var3);
        } else {
            int var5 = 64 / var4;
            int var6 = (64 + var5 - 1) / var5;
            long[] var7 = new long[var6];
            int var8 = 0;
            int var9 = 0;

            for(int var10 = 0; var10 < 64; ++var10) {
                int var11 = param1.applyAsInt(var10);
                var7[var8] |= (long)var0.get(var11) << var9;
                var9 += var4;
                if (var9 + var4 > 64) {
                    ++var8;
                    var9 = 0;
                }
            }

            Dynamic<?> var12 = param0.createLongList(Arrays.stream(var7));
            return makePalettedContainer(var3, var12);
        }
    }

    private static Dynamic<?> makePalettedContainer(Dynamic<?> param0) {
        return param0.createMap(ImmutableMap.of(param0.createString("palette"), param0));
    }

    private static Dynamic<?> makePalettedContainer(Dynamic<?> param0, Dynamic<?> param1) {
        return param0.createMap(ImmutableMap.of(param0.createString("palette"), param0, param0.createString("data"), param1));
    }

    private static Dynamic<?> makeOptimizedPalettedContainer(Dynamic<?> param0, Dynamic<?> param1) {
        List<Dynamic<?>> var0 = param0.asStream().collect(Collectors.toCollection(ArrayList::new));
        if (var0.size() == 1) {
            return makePalettedContainer(param0);
        } else {
            param0 = padPaletteEntries(param0, param1, var0);
            return makePalettedContainer(param0, param1);
        }
    }

    private static Dynamic<?> padPaletteEntries(Dynamic<?> param0, Dynamic<?> param1, List<Dynamic<?>> param2) {
        long var0 = param1.asLongStream().count() * 64L;
        long var1 = var0 / 4096L;
        int var2 = param2.size();
        int var3 = ceillog2(var2);
        if (var1 <= (long)var3) {
            return param0;
        } else {
            Dynamic<?> var4 = param0.createMap(ImmutableMap.of(param0.createString("Name"), param0.createString("minecraft:air")));
            int var5 = (1 << (int)(var1 - 1L)) + 1;
            int var6 = var5 - var2;

            for(int var7 = 0; var7 < var6; ++var7) {
                param2.add(var4);
            }

            return param0.createList(param2.stream());
        }
    }

    public static int ceillog2(int param0) {
        return param0 == 0 ? 0 : (int)Math.ceil(Math.log((double)param0) / Math.log(2.0));
    }

    static {
        BIOMES_BY_ID.put(0, "minecraft:ocean");
        BIOMES_BY_ID.put(1, "minecraft:plains");
        BIOMES_BY_ID.put(2, "minecraft:desert");
        BIOMES_BY_ID.put(3, "minecraft:mountains");
        BIOMES_BY_ID.put(4, "minecraft:forest");
        BIOMES_BY_ID.put(5, "minecraft:taiga");
        BIOMES_BY_ID.put(6, "minecraft:swamp");
        BIOMES_BY_ID.put(7, "minecraft:river");
        BIOMES_BY_ID.put(8, "minecraft:nether_wastes");
        BIOMES_BY_ID.put(9, "minecraft:the_end");
        BIOMES_BY_ID.put(10, "minecraft:frozen_ocean");
        BIOMES_BY_ID.put(11, "minecraft:frozen_river");
        BIOMES_BY_ID.put(12, "minecraft:snowy_tundra");
        BIOMES_BY_ID.put(13, "minecraft:snowy_mountains");
        BIOMES_BY_ID.put(14, "minecraft:mushroom_fields");
        BIOMES_BY_ID.put(15, "minecraft:mushroom_field_shore");
        BIOMES_BY_ID.put(16, "minecraft:beach");
        BIOMES_BY_ID.put(17, "minecraft:desert_hills");
        BIOMES_BY_ID.put(18, "minecraft:wooded_hills");
        BIOMES_BY_ID.put(19, "minecraft:taiga_hills");
        BIOMES_BY_ID.put(20, "minecraft:mountain_edge");
        BIOMES_BY_ID.put(21, "minecraft:jungle");
        BIOMES_BY_ID.put(22, "minecraft:jungle_hills");
        BIOMES_BY_ID.put(23, "minecraft:jungle_edge");
        BIOMES_BY_ID.put(24, "minecraft:deep_ocean");
        BIOMES_BY_ID.put(25, "minecraft:stone_shore");
        BIOMES_BY_ID.put(26, "minecraft:snowy_beach");
        BIOMES_BY_ID.put(27, "minecraft:birch_forest");
        BIOMES_BY_ID.put(28, "minecraft:birch_forest_hills");
        BIOMES_BY_ID.put(29, "minecraft:dark_forest");
        BIOMES_BY_ID.put(30, "minecraft:snowy_taiga");
        BIOMES_BY_ID.put(31, "minecraft:snowy_taiga_hills");
        BIOMES_BY_ID.put(32, "minecraft:giant_tree_taiga");
        BIOMES_BY_ID.put(33, "minecraft:giant_tree_taiga_hills");
        BIOMES_BY_ID.put(34, "minecraft:wooded_mountains");
        BIOMES_BY_ID.put(35, "minecraft:savanna");
        BIOMES_BY_ID.put(36, "minecraft:savanna_plateau");
        BIOMES_BY_ID.put(37, "minecraft:badlands");
        BIOMES_BY_ID.put(38, "minecraft:wooded_badlands_plateau");
        BIOMES_BY_ID.put(39, "minecraft:badlands_plateau");
        BIOMES_BY_ID.put(40, "minecraft:small_end_islands");
        BIOMES_BY_ID.put(41, "minecraft:end_midlands");
        BIOMES_BY_ID.put(42, "minecraft:end_highlands");
        BIOMES_BY_ID.put(43, "minecraft:end_barrens");
        BIOMES_BY_ID.put(44, "minecraft:warm_ocean");
        BIOMES_BY_ID.put(45, "minecraft:lukewarm_ocean");
        BIOMES_BY_ID.put(46, "minecraft:cold_ocean");
        BIOMES_BY_ID.put(47, "minecraft:deep_warm_ocean");
        BIOMES_BY_ID.put(48, "minecraft:deep_lukewarm_ocean");
        BIOMES_BY_ID.put(49, "minecraft:deep_cold_ocean");
        BIOMES_BY_ID.put(50, "minecraft:deep_frozen_ocean");
        BIOMES_BY_ID.put(127, "minecraft:the_void");
        BIOMES_BY_ID.put(129, "minecraft:sunflower_plains");
        BIOMES_BY_ID.put(130, "minecraft:desert_lakes");
        BIOMES_BY_ID.put(131, "minecraft:gravelly_mountains");
        BIOMES_BY_ID.put(132, "minecraft:flower_forest");
        BIOMES_BY_ID.put(133, "minecraft:taiga_mountains");
        BIOMES_BY_ID.put(134, "minecraft:swamp_hills");
        BIOMES_BY_ID.put(140, "minecraft:ice_spikes");
        BIOMES_BY_ID.put(149, "minecraft:modified_jungle");
        BIOMES_BY_ID.put(151, "minecraft:modified_jungle_edge");
        BIOMES_BY_ID.put(155, "minecraft:tall_birch_forest");
        BIOMES_BY_ID.put(156, "minecraft:tall_birch_hills");
        BIOMES_BY_ID.put(157, "minecraft:dark_forest_hills");
        BIOMES_BY_ID.put(158, "minecraft:snowy_taiga_mountains");
        BIOMES_BY_ID.put(160, "minecraft:giant_spruce_taiga");
        BIOMES_BY_ID.put(161, "minecraft:giant_spruce_taiga_hills");
        BIOMES_BY_ID.put(162, "minecraft:modified_gravelly_mountains");
        BIOMES_BY_ID.put(163, "minecraft:shattered_savanna");
        BIOMES_BY_ID.put(164, "minecraft:shattered_savanna_plateau");
        BIOMES_BY_ID.put(165, "minecraft:eroded_badlands");
        BIOMES_BY_ID.put(166, "minecraft:modified_wooded_badlands_plateau");
        BIOMES_BY_ID.put(167, "minecraft:modified_badlands_plateau");
        BIOMES_BY_ID.put(168, "minecraft:bamboo_jungle");
        BIOMES_BY_ID.put(169, "minecraft:bamboo_jungle_hills");
        BIOMES_BY_ID.put(170, "minecraft:soul_sand_valley");
        BIOMES_BY_ID.put(171, "minecraft:crimson_forest");
        BIOMES_BY_ID.put(172, "minecraft:warped_forest");
        BIOMES_BY_ID.put(173, "minecraft:basalt_deltas");
        BIOMES_BY_ID.put(174, "minecraft:dripstone_caves");
        BIOMES_BY_ID.put(175, "minecraft:lush_caves");
        BIOMES_BY_ID.put(177, "minecraft:meadow");
        BIOMES_BY_ID.put(178, "minecraft:grove");
        BIOMES_BY_ID.put(179, "minecraft:snowy_slopes");
        BIOMES_BY_ID.put(180, "minecraft:snowcapped_peaks");
        BIOMES_BY_ID.put(181, "minecraft:lofty_peaks");
        BIOMES_BY_ID.put(182, "minecraft:stony_peaks");
    }
}
