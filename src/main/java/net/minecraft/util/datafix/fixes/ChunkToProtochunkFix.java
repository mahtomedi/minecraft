package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChunkToProtochunkFix extends DataFix {
    private static final int NUM_SECTIONS = 16;

    public ChunkToProtochunkFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(
            this.writeFixAndRead(
                "ChunkToProtoChunkFix",
                this.getInputSchema().getType(References.CHUNK),
                this.getOutputSchema().getType(References.CHUNK),
                param0 -> param0.update("Level", ChunkToProtochunkFix::fixChunkData)
            ),
            this.writeAndRead(
                "Structure biome inject",
                this.getInputSchema().getType(References.STRUCTURE_FEATURE),
                this.getOutputSchema().getType(References.STRUCTURE_FEATURE)
            )
        );
    }

    private static <T> Dynamic<T> fixChunkData(Dynamic<T> param0) {
        boolean var0 = param0.get("TerrainPopulated").asBoolean(false)
            && (param0.get("LightPopulated").asNumber().result().isEmpty() || param0.get("LightPopulated").asBoolean(false));
        Dynamic<T> var1;
        if (var0) {
            var1 = repackTicks(repackBiomes(param0));
        } else {
            var1 = createEmptyChunk(param0);
        }

        return var1.set("Status", param0.createString(var0 ? "mobs_spawned" : "empty")).set("hasLegacyStructureData", param0.createBoolean(true));
    }

    private static <T> Dynamic<T> repackBiomes(Dynamic<T> param0) {
        return param0.update("Biomes", param1 -> DataFixUtils.orElse(param1.asByteBufferOpt().result().map(param1x -> {
                int[] var0x = new int[256];

                for(int var1x = 0; var1x < var0x.length; ++var1x) {
                    if (var1x < param1x.capacity()) {
                        var0x[var1x] = param1x.get(var1x) & 255;
                    }
                }

                return param0.createIntList(Arrays.stream(var0x));
            }), param1));
    }

    private static <T> Dynamic<T> repackTicks(Dynamic<T> param0) {
        return DataFixUtils.orElse(
            param0.get("TileTicks")
                .asStreamOpt()
                .result()
                .map(
                    param1 -> {
                        List<ShortList> var0x = IntStream.range(0, 16).mapToObj(param0x -> new ShortArrayList()).collect(Collectors.toList());
                        param1.forEach(param1x -> {
                            int var0xx = param1x.get("x").asInt(0);
                            int var1x = param1x.get("y").asInt(0);
                            int var2x = param1x.get("z").asInt(0);
                            short var3 = packOffsetCoordinates(var0xx, var1x, var2x);
                            ((ShortList)var0x.get(var1x >> 4)).add(var3);
                        });
                        return param0.remove("TileTicks")
                            .set(
                                "ToBeTicked",
                                param0.createList(
                                    var0x.stream()
                                        .map(param1x -> param0.createList(param1x.intStream().mapToObj(param1xx -> param0.createShort((short)param1xx))))
                                )
                            );
                    }
                ),
            param0
        );
    }

    private static <T> Dynamic<T> createEmptyChunk(Dynamic<T> param0) {
        Builder<Dynamic<T>, Dynamic<T>> var0 = ImmutableMap.builder();
        param0.get("xPos").result().ifPresent(param2 -> var0.put(param0.createString("xPos"), param2));
        param0.get("zPos").result().ifPresent(param2 -> var0.put(param0.createString("zPos"), param2));
        return param0.createMap(var0.build());
    }

    private static short packOffsetCoordinates(int param0, int param1, int param2) {
        return (short)(param0 & 15 | (param1 & 15) << 4 | (param2 & 15) << 8);
    }
}
