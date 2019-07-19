package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix extends DataFix {
    private static final Splitter SPLITTER = Splitter.on(';').limit(5);
    private static final Splitter LAYER_SPLITTER = Splitter.on(',');
    private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on('x').limit(2);
    private static final Splitter AMOUNT_SPLITTER = Splitter.on('*').limit(2);
    private static final Splitter BLOCK_SPLITTER = Splitter.on(':').limit(3);

    public LevelFlatGeneratorInfoFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), param0 -> param0.update(DSL.remainderFinder(), this::fix)
        );
    }

    private Dynamic<?> fix(Dynamic<?> param0) {
        return param0.get("generatorName").asString("").equalsIgnoreCase("flat")
            ? param0.update("generatorOptions", param0x -> DataFixUtils.orElse(param0x.asString().map(this::fixString).map(param0x::createString), param0x))
            : param0;
    }

    @VisibleForTesting
    String fixString(String param0) {
        if (param0.isEmpty()) {
            return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
        } else {
            Iterator<String> var0 = SPLITTER.split(param0).iterator();
            String var1 = var0.next();
            int var2;
            String var3;
            if (var0.hasNext()) {
                var2 = NumberUtils.toInt(var1, 0);
                var3 = var0.next();
            } else {
                var2 = 0;
                var3 = var1;
            }

            if (var2 >= 0 && var2 <= 3) {
                StringBuilder var6 = new StringBuilder();
                Splitter var7 = var2 < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
                var6.append(StreamSupport.stream(LAYER_SPLITTER.split(var3).spliterator(), false).map(param2 -> {
                    List<String> var0x = var7.splitToList(param2);
                    int var1x;
                    String var2x;
                    if (var0x.size() == 2) {
                        var1x = NumberUtils.toInt(var0x.get(0));
                        var2x = var0x.get(1);
                    } else {
                        var1x = 1;
                        var2x = var0x.get(0);
                    }

                    List<String> var5x = BLOCK_SPLITTER.splitToList(var2x);
                    int var6x = var5x.get(0).equals("minecraft") ? 1 : 0;
                    String var7x = var5x.get(var6x);
                    int var8 = var2 == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + var7x) : NumberUtils.toInt(var7x, 0);
                    int var9 = var6x + 1;
                    int var10 = var5x.size() > var9 ? NumberUtils.toInt(var5x.get(var9), 0) : 0;
                    return (var1x == 1 ? "" : var1x + "*") + BlockStateData.getTag(var8 << 4 | var10).get("Name").asString("");
                }).collect(Collectors.joining(",")));

                while(var0.hasNext()) {
                    var6.append(';').append(var0.next());
                }

                return var6.toString();
            } else {
                return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
            }
        }
    }
}
