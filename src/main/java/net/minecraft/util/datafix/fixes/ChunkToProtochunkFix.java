package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChunkToProtochunkFix extends DataFix {
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
                                .map(Typed::write)
                                .flatMap(Dynamic::asStreamOpt);
                            Dynamic<?> var1x = param2x.get(DSL.remainderFinder());
                            boolean var2x = var1x.get("TerrainPopulated").asBoolean(false)
                                && (!var1x.get("LightPopulated").asNumber().isPresent() || var1x.get("LightPopulated").asBoolean(false));
                            var1x = var1x.set("Status", var1x.createString(var2x ? "mobs_spawned" : "empty"));
                            var1x = var1x.set("hasLegacyStructureData", var1x.createBoolean(true));
                            Dynamic<?> var9;
                            if (var2x) {
                                Optional<ByteBuffer> var3x = var1x.get("Biomes").asByteBufferOpt();
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
                                List<Dynamic<?>> var8 = IntStream.range(0, 16)
                                    .mapToObj(param1x -> var7.createList(Stream.empty()))
                                    .collect(Collectors.toList());
                                if (var0x.isPresent()) {
                                    var0x.get().forEach(param2xx -> {
                                        int var0xx = param2xx.get("x").asInt(0);
                                        int var1xx = param2xx.get("y").asInt(0);
                                        int var2xx = param2xx.get("z").asInt(0);
                                        short var3xx = packOffsetCoordinates(var0xx, var1xx, var2xx);
                                        var8.set(var1xx >> 4, var8.get(var1xx >> 4).merge(var7.createShort(var3xx)));
                                    });
                                    var1x = var1x.set("ToBeTicked", var1x.createList(var8.stream()));
                                }
            
                                var9 = param2x.set(DSL.remainderFinder(), var1x).write();
                            } else {
                                var9 = var1x;
                            }
            
                            return var3.readTyped(var9).getSecond().orElseThrow(() -> new IllegalStateException("Could not read the new chunk"));
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
