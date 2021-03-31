package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.stream.LongStream;
import net.minecraft.util.Mth;

public class BitStorageAlignFix extends DataFix {
    private static final int BIT_TO_LONG_SHIFT = 6;
    private static final int SECTION_WIDTH = 16;
    private static final int SECTION_HEIGHT = 16;
    private static final int SECTION_SIZE = 4096;
    private static final int HEIGHTMAP_BITS = 9;
    private static final int HEIGHTMAP_SIZE = 256;

    public BitStorageAlignFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = var0.findFieldType("Level");
        OpticFinder<?> var2 = DSL.fieldFinder("Level", var1);
        OpticFinder<?> var3 = var2.type().findField("Sections");
        Type<?> var4 = ((ListType)var3.type()).getElement();
        OpticFinder<?> var5 = DSL.typeFinder(var4);
        Type<Pair<String, Dynamic<?>>> var6 = DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType());
        OpticFinder<List<Pair<String, Dynamic<?>>>> var7 = DSL.fieldFinder("Palette", DSL.list(var6));
        return this.fixTypeEverywhereTyped(
            "BitStorageAlignFix",
            var0,
            this.getOutputSchema().getType(References.CHUNK),
            param4 -> param4.updateTyped(var2, param3x -> this.updateHeightmaps(updateSections(var3, var5, var7, param3x)))
        );
    }

    private Typed<?> updateHeightmaps(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(),
            param0x -> param0x.update(
                    "Heightmaps", param1 -> param1.updateMapValues(param1x -> param1x.mapSecond(param1xx -> updateBitStorage(param0x, param1xx, 256, 9)))
                )
        );
    }

    private static Typed<?> updateSections(OpticFinder<?> param0, OpticFinder<?> param1, OpticFinder<List<Pair<String, Dynamic<?>>>> param2, Typed<?> param3) {
        return param3.updateTyped(
            param0,
            param2x -> param2x.updateTyped(
                    param1,
                    param1x -> {
                        int var0x = param1x.getOptional(param2).map(param0x -> Math.max(4, DataFixUtils.ceillog2(param0x.size()))).orElse(0);
                        return var0x != 0 && !Mth.isPowerOfTwo(var0x)
                            ? param1x.update(
                                DSL.remainderFinder(),
                                param1xx -> param1xx.update("BlockStates", param2xx -> updateBitStorage(param1xx, param2xx, 4096, var0x))
                            )
                            : param1x;
                    }
                )
        );
    }

    private static Dynamic<?> updateBitStorage(Dynamic<?> param0, Dynamic<?> param1, int param2, int param3) {
        long[] var0 = param1.asLongStream().toArray();
        long[] var1 = addPadding(param2, param3, var0);
        return param0.createLongList(LongStream.of(var1));
    }

    public static long[] addPadding(int param0, int param1, long[] param2) {
        int var0 = param2.length;
        if (var0 == 0) {
            return param2;
        } else {
            long var1 = (1L << param1) - 1L;
            int var2 = 64 / param1;
            int var3 = (param0 + var2 - 1) / var2;
            long[] var4 = new long[var3];
            int var5 = 0;
            int var6 = 0;
            long var7 = 0L;
            int var8 = 0;
            long var9 = param2[0];
            long var10 = var0 > 1 ? param2[1] : 0L;

            for(int var11 = 0; var11 < param0; ++var11) {
                int var12 = var11 * param1;
                int var13 = var12 >> 6;
                int var14 = (var11 + 1) * param1 - 1 >> 6;
                int var15 = var12 ^ var13 << 6;
                if (var13 != var8) {
                    var9 = var10;
                    var10 = var13 + 1 < var0 ? param2[var13 + 1] : 0L;
                    var8 = var13;
                }

                long var16;
                if (var13 == var14) {
                    var16 = var9 >>> var15 & var1;
                } else {
                    int var17 = 64 - var15;
                    var16 = (var9 >>> var15 | var10 << var17) & var1;
                }

                int var19 = var6 + param1;
                if (var19 >= 64) {
                    var4[var5++] = var7;
                    var7 = var16;
                    var6 = param1;
                } else {
                    var7 |= var16 << var6;
                    var6 = var19;
                }
            }

            if (var7 != 0L) {
                var4[var5] = var7;
            }

            return var4;
        }
    }
}
