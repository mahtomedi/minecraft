package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemBannerColorFix extends DataFix {
    public ItemBannerColorFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        OpticFinder<?> var3 = var2.type().findField("BlockEntityTag");
        return this.fixTypeEverywhereTyped(
            "ItemBannerColorFix",
            var0,
            param3 -> {
                Optional<Pair<String, String>> var0x = param3.getOptional(var1);
                if (var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:banner")) {
                    Dynamic<?> var1x = param3.get(DSL.remainderFinder());
                    Optional<? extends Typed<?>> var2x = param3.getOptionalTyped(var2);
                    if (var2x.isPresent()) {
                        Typed<?> var3x = var2x.get();
                        Optional<? extends Typed<?>> var4x = var3x.getOptionalTyped(var3);
                        if (var4x.isPresent()) {
                            Typed<?> var5 = (Typed)var4x.get();
                            Dynamic<?> var6 = var3x.get(DSL.remainderFinder());
                            Dynamic<?> var7 = var5.getOrCreate(DSL.remainderFinder());
                            if (var7.get("Base").asNumber().result().isPresent()) {
                                var1x = var1x.set("Damage", var1x.createShort((short)(var7.get("Base").asInt(0) & 15)));
                                Optional<? extends Dynamic<?>> var8 = var6.get("display").result();
                                if (var8.isPresent()) {
                                    Dynamic<?> var9 = var8.get();
                                    Dynamic<?> var10 = var9.createMap(
                                        ImmutableMap.of(var9.createString("Lore"), var9.createList(Stream.of(var9.createString("(+NBT"))))
                                    );
                                    if (Objects.equals(var9, var10)) {
                                        return param3.set(DSL.remainderFinder(), var1x);
                                    }
                                }
    
                                var7.remove("Base");
                                return param3.set(DSL.remainderFinder(), var1x).set(var2, var3x.set(var3, var5.set(DSL.remainderFinder(), var7)));
                            }
                        }
                    }
    
                    return param3.set(DSL.remainderFinder(), var1x);
                } else {
                    return param3;
                }
            }
        );
    }
}
