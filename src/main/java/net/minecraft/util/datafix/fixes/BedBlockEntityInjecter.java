package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class BedBlockEntityInjecter extends DataFix {
    public BedBlockEntityInjecter(Schema param0, boolean param1) {
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
                    List<? extends Dynamic<?>> var5x = var1x.get("Sections").asList(Function.identity());
        
                    for(int var6x = 0; var6x < var5x.size(); ++var6x) {
                        Dynamic<?> var7 = (Dynamic)var5x.get(var6x);
                        int var8 = var7.get("Y").asInt(0);
                        Stream<Integer> var9 = var7.get("Blocks").asStream().map(param0x -> param0x.asInt(0));
                        int var10 = 0;
        
                        for(int var11 : var9::iterator) {
                            if (416 == (var11 & 0xFF) << 4) {
                                int var12 = var10 & 15;
                                int var13 = var10 >> 8 & 15;
                                int var14 = var10 >> 4 & 15;
                                Map<Dynamic<?>, Dynamic<?>> var15 = Maps.newHashMap();
                                var15.put(var7.createString("id"), var7.createString("minecraft:bed"));
                                var15.put(var7.createString("x"), var7.createInt(var12 + (var2x << 4)));
                                var15.put(var7.createString("y"), var7.createInt(var13 + (var8 << 4)));
                                var15.put(var7.createString("z"), var7.createInt(var14 + (var3x << 4)));
                                var15.put(var7.createString("color"), var7.createShort((short)14));
                                var4x.add(
                                    var0.read(var7.createMap(var15))
                                        .getSecond()
                                        .orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity."))
                                );
                            }
        
                            ++var10;
                        }
                    }
        
                    return !var4x.isEmpty() ? param3.set(var1, var0x.set(var2, var4x)) : param3;
                }
            )
        );
    }
}
