package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class EntityPaintingItemFrameDirectionFix extends DataFix {
    private static final int[][] DIRECTIONS = new int[][]{{0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}};

    public EntityPaintingItemFrameDirectionFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private Dynamic<?> doFix(Dynamic<?> param0, boolean param1, boolean param2) {
        if ((param1 || param2) && !param0.get("Facing").asNumber().isPresent()) {
            int var0;
            if (param0.get("Direction").asNumber().isPresent()) {
                var0 = param0.get("Direction").asByte((byte)0) % DIRECTIONS.length;
                int[] var1 = DIRECTIONS[var0];
                param0 = param0.set("TileX", param0.createInt(param0.get("TileX").asInt(0) + var1[0]));
                param0 = param0.set("TileY", param0.createInt(param0.get("TileY").asInt(0) + var1[1]));
                param0 = param0.set("TileZ", param0.createInt(param0.get("TileZ").asInt(0) + var1[2]));
                param0 = param0.remove("Direction");
                if (param2 && param0.get("ItemRotation").asNumber().isPresent()) {
                    param0 = param0.set("ItemRotation", param0.createByte((byte)(param0.get("ItemRotation").asByte((byte)0) * 2)));
                }
            } else {
                var0 = param0.get("Dir").asByte((byte)0) % DIRECTIONS.length;
                param0 = param0.remove("Dir");
            }

            param0 = param0.set("Facing", param0.createByte((byte)var0));
        }

        return param0;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getChoiceType(References.ENTITY, "Painting");
        OpticFinder<?> var1 = DSL.namedChoice("Painting", var0);
        Type<?> var2 = this.getInputSchema().getChoiceType(References.ENTITY, "ItemFrame");
        OpticFinder<?> var3 = DSL.namedChoice("ItemFrame", var2);
        Type<?> var4 = this.getInputSchema().getType(References.ENTITY);
        TypeRewriteRule var5 = this.fixTypeEverywhereTyped(
            "EntityPaintingFix",
            var4,
            param2 -> param2.updateTyped(var1, var0, param0x -> param0x.update(DSL.remainderFinder(), param0xx -> this.doFix(param0xx, true, false)))
        );
        TypeRewriteRule var6 = this.fixTypeEverywhereTyped(
            "EntityItemFrameFix",
            var4,
            param2 -> param2.updateTyped(var3, var2, param0x -> param0x.update(DSL.remainderFinder(), param0xx -> this.doFix(param0xx, false, true)))
        );
        return TypeRewriteRule.seq(var5, var6);
    }
}
