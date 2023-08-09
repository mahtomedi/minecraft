package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ChunkBedBlockEntityInjecterFix extends DataFix {
    public ChunkBedBlockEntityInjecterFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.CHUNK);
        Type<?> var1 = var0.findFieldType("Level");
        Type<?> var2 = var1.findFieldType("TileEntities");
        if (!(var2 instanceof ListType)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        } else {
            ListType<?> var3 = (ListType)var2;
            return this.cap(var1, var3);
        }
    }

    private <TE> TypeRewriteRule cap(Type<?> param0, ListType<TE> param1) {
        Type<TE> var0 = param1.getElement();
        OpticFinder<?> var1 = DSL.fieldFinder("Level", param0);
        OpticFinder<List<TE>> var2 = DSL.fieldFinder("TileEntities", param1);
        int var3 = 416;
        return TypeRewriteRule.seq(
            this.fixTypeEverywhere(
                "InjectBedBlockEntityType",
                this.getInputSchema().findChoiceType(References.BLOCK_ENTITY),
                this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY),
                param0x -> param0xx -> param0xx
            ),
            this.fixTypeEverywhereTyped(
                "BedBlockEntityInjecter",
                this.getOutputSchema().getType(References.CHUNK),
                param3 -> {
                    Typed<?> var0x = param3.getTyped(var1);
                    Dynamic<?> var1x = var0x.get(DSL.remainderFinder());
                    int var2x = var1x.get("xPos").asInt(0);
                    int var3x = var1x.get("zPos").asInt(0);
                    List<TE> var4x = Lists.newArrayList(var0x.getOrCreate(var2));
        
                    for(Dynamic<?> var6x : var1x.get("Sections").asList(Function.identity())) {
                        int var7 = var6x.get("Y").asInt(0);
                        Streams.mapWithIndex(var6x.get("Blocks").asIntStream(), (param4, param5) -> {
                                if (416 == (param4 & 0xFF) << 4) {
                                    int var0xx = (int)param5;
                                    int var1xx = var0xx & 15;
                                    int var2xx = var0xx >> 8 & 15;
                                    int var3xx = var0xx >> 4 & 15;
                                    Map<Dynamic<?>, Dynamic<?>> var4xx = Maps.newHashMap();
                                    var4xx.put(var6x.createString("id"), var6x.createString("minecraft:bed"));
                                    var4xx.put(var6x.createString("x"), var6x.createInt(var1xx + (var2x << 4)));
                                    var4xx.put(var6x.createString("y"), var6x.createInt(var2xx + (var7 << 4)));
                                    var4xx.put(var6x.createString("z"), var6x.createInt(var3xx + (var3x << 4)));
                                    var4xx.put(var6x.createString("color"), var6x.createShort((short)14));
                                    return var4xx;
                                } else {
                                    return null;
                                }
                            })
                            .forEachOrdered(
                                param3x -> {
                                    if (param3x != null) {
                                        var4x.add(
                                            ((Pair)var0.read(var6x.createMap(param3x))
                                                    .result()
                                                    .orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity.")))
                                                .getFirst()
                                        );
                                    }
                
                                }
                            );
                    }
        
                    return !var4x.isEmpty() ? param3.set(var1, var0x.set(var2, var4x)) : param3;
                }
            )
        );
    }
}
