package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;

public class ItemShulkerBoxColorFix extends DataFix {
    public static final String[] NAMES_BY_COLOR = new String[]{
        "minecraft:white_shulker_box",
        "minecraft:orange_shulker_box",
        "minecraft:magenta_shulker_box",
        "minecraft:light_blue_shulker_box",
        "minecraft:yellow_shulker_box",
        "minecraft:lime_shulker_box",
        "minecraft:pink_shulker_box",
        "minecraft:gray_shulker_box",
        "minecraft:silver_shulker_box",
        "minecraft:cyan_shulker_box",
        "minecraft:purple_shulker_box",
        "minecraft:blue_shulker_box",
        "minecraft:brown_shulker_box",
        "minecraft:green_shulker_box",
        "minecraft:red_shulker_box",
        "minecraft:black_shulker_box"
    };

    public ItemShulkerBoxColorFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        OpticFinder<?> var3 = var2.type().findField("BlockEntityTag");
        return this.fixTypeEverywhereTyped(
            "ItemShulkerBoxColorFix",
            var0,
            param3 -> {
                Optional<Pair<String, String>> var0x = param3.getOptional(var1);
                if (var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:shulker_box")) {
                    Optional<? extends Typed<?>> var1x = param3.getOptionalTyped(var2);
                    if (var1x.isPresent()) {
                        Typed<?> var2x = var1x.get();
                        Optional<? extends Typed<?>> var3x = var2x.getOptionalTyped(var3);
                        if (var3x.isPresent()) {
                            Typed<?> var4x = var3x.get();
                            Dynamic<?> var5 = var4x.get(DSL.remainderFinder());
                            int var6 = var5.get("Color").asInt(0);
                            var5.remove("Color");
                            return param3.set(var2, var2x.set(var3, var4x.set(DSL.remainderFinder(), var5)))
                                .set(var1, Pair.of(References.ITEM_NAME.typeName(), NAMES_BY_COLOR[var6 % 16]));
                        }
                    }
                }
    
                return param3;
            }
        );
    }
}
