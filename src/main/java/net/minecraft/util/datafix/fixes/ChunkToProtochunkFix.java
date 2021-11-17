package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChunkToProtochunkFix extends DataFix {
    private static final int NUM_SECTIONS = 16;

    public ChunkToProtochunkFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = this.getOutputSchema().getType(References.CHUNK);
        Type<?> var2 = var0.findFieldType("Level");
        Type<?> var3 = var1.findFieldType("Level");
        Type<?> var4 = var2.findFieldType("TileTicks");
        OpticFinder<?> var5 = DSL.fieldFinder("Level", var2);
        OpticFinder<?> var6 = DSL.fieldFinder("TileTicks", var4);
        return TypeRewriteRule.seq(
            this.fixTypeEverywhereTyped(
                "ChunkToProtoChunkFix",
                var0,
                this.getOutputSchema().getType(References.CHUNK),
                param3 -> param3.updateTyped(
                        var5,
                        var3,
                        param2x -> {
                            Optional<? extends Stream<? extends Dynamic<?>>> var0x = param2x.getOptionalTyped(var6)
                                .flatMap(param0x -> param0x.write().result())
                                .flatMap(param0x -> param0x.asStreamOpt().result());
                            Dynamic<?> var1x = param2x.get(DSL.remainderFinder());
                            boolean var2x = var1x.get("TerrainPopulated").asBoolean(false)
                                && (!var1x.get("LightPopulated").asNumber().result().isPresent() || var1x.get("LightPopulated").asBoolean(false));
                            var1x = var1x.set("Status", var1x.createString(var2x ? "mobs_spawned" : "empty"));
                            var1x = var1x.set("hasLegacyStructureData", var1x.createBoolean(true));
                            Dynamic<?> var9;
                            if (var2x) {
                                Optional<ByteBuffer> var3x = var1x.get("Biomes").asByteBufferOpt().result();
                                if (var3x.isPresent()) {
                                    ByteBuffer var7x = var3x.get();
                                    int[] var5x = new int[256];
            
                                    for(int var6x = 0; var6x < var5x.length; ++var6x) {
                                        if (var6x < var7x.capacity()) {
                                            var5x[var6x] = var7x.get(var6x) & 255;
                                        }
                                    }
            
                                    var1x = var1x.set("Biomes", var1x.createIntList(Arrays.stream(var5x)));
                                }
            
                                Dynamic<?> var7 = var1x;
                                List<ShortList> var8 = IntStream.range(0, 16).mapToObj(param0x -> new ShortArrayList()).collect(Collectors.toList());
                                if (var0x.isPresent()) {
                                    var0x.get().forEach(param1x -> {
                                        int var0xx = param1x.get("x").asInt(0);
                                        int var1xx = param1x.get("y").asInt(0);
                                        int var2xx = param1x.get("z").asInt(0);
                                        short var3xx = packOffsetCoordinates(var0xx, var1xx, var2xx);
                                        var8.get(var1xx >> 4).add(var3xx);
                                    });
                                    var1x = var1x.set(
                                        "ToBeTicked", var1x.createList(var8.stream().map(param1x -> var7.createList(param1x.stream().map(var7::createShort))))
                                    );
                                }
            
                                var9 = DataFixUtils.orElse(param2x.set(DSL.remainderFinder(), var1x).write().result(), var1x);
                            } else {
                                var9 = var1x;
                            }
            
                            return var3.readTyped(var9).result().orElseThrow(() -> new IllegalStateException("Could not read the new chunk")).getFirst();
                        }
                    )
            ),
            this.writeAndRead(
                "Structure biome inject",
                this.getInputSchema().getType(References.STRUCTURE_FEATURE),
                this.getOutputSchema().getType(References.STRUCTURE_FEATURE)
            )
        );
    }

    private static short packOffsetCoordinates(int param0, int param1, int param2) {
        return (short)(param0 & 15 | (param1 & 15) << 4 | (param2 & 15) << 8);
    }
}
