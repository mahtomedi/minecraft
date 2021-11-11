package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class ChunkBiomeFix extends DataFix {
    public ChunkBiomeFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("Level");
        return this.fixTypeEverywhereTyped(
            "Leaves fix", var0, param1 -> param1.updateTyped(var1, param0x -> param0x.update(DSL.remainderFinder(), param0xx -> {
                        Optional<IntStream> var0x = param0xx.get("Biomes").asIntStreamOpt().result();
                        if (var0x.isEmpty()) {
                            return param0xx;
                        } else {
                            int[] var1x = var0x.get().toArray();
                            if (var1x.length != 256) {
                                return param0xx;
                            } else {
                                int[] var2x = new int[1024];
    
                                for(int var3 = 0; var3 < 4; ++var3) {
                                    for(int var4 = 0; var4 < 4; ++var4) {
                                        int var5 = (var4 << 2) + 2;
                                        int var6 = (var3 << 2) + 2;
                                        int var7 = var6 << 4 | var5;
                                        var2x[var3 << 2 | var4] = var1x[var7];
                                    }
                                }
    
                                for(int var8 = 1; var8 < 64; ++var8) {
                                    System.arraycopy(var2x, 0, var2x, var8 * 16, 16);
                                }
    
                                return param0xx.set("Biomes", param0xx.createIntList(Arrays.stream(var2x)));
                            }
                        }
                    }))
        );
    }
}
