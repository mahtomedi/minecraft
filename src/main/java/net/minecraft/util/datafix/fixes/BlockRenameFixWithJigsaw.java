package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import java.util.function.Function;

public abstract class BlockRenameFixWithJigsaw extends BlockRenameFix {
    private final String name;

    public BlockRenameFixWithJigsaw(Schema param0, String param1) {
        super(param0, param1);
        this.name = param1;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TypeReference var0 = References.BLOCK_ENTITY;
        String var1 = "minecraft:jigsaw";
        OpticFinder<?> var2 = DSL.namedChoice("minecraft:jigsaw", this.getInputSchema().getChoiceType(var0, "minecraft:jigsaw"));
        TypeRewriteRule var3 = this.fixTypeEverywhereTyped(
            this.name + " for jigsaw state",
            this.getInputSchema().getType(var0),
            this.getOutputSchema().getType(var0),
            param2 -> param2.updateTyped(
                    var2,
                    this.getOutputSchema().getChoiceType(var0, "minecraft:jigsaw"),
                    param0x -> param0x.update(
                            DSL.remainderFinder(),
                            param0xx -> param0xx.update("final_state", param1x -> DataFixUtils.orElse(param1x.asString().result().map(param0xxxx -> {
                                        int var0x = param0xxxx.indexOf(91);
                                        int var1x = param0xxxx.indexOf(123);
                                        int var2x = param0xxxx.length();
                                        if (var0x > 0) {
                                            var2x = Math.min(var2x, var0x);
                                        }
            
                                        if (var1x > 0) {
                                            var2x = Math.min(var2x, var1x);
                                        }
            
                                        String var3x = param0xxxx.substring(0, var2x);
                                        String var4x = this.fixBlock(var3x);
                                        return var4x + param0xxxx.substring(var2x);
                                    }).map(param0xx::createString), param1x))
                        )
                )
        );
        return TypeRewriteRule.seq(super.makeRule(), var3);
    }

    public static DataFix create(Schema param0, String param1, final Function<String, String> param2) {
        return new BlockRenameFixWithJigsaw(param0, param1) {
            @Override
            protected String fixBlock(String param0) {
                return param2.apply(param0);
            }
        };
    }
}
