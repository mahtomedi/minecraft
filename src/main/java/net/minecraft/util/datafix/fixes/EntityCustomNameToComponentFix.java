package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityCustomNameToComponentFix extends DataFix {
    public EntityCustomNameToComponentFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<String> var0 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
        return this.fixTypeEverywhereTyped(
            "EntityCustomNameToComponentFix", this.getInputSchema().getType(References.ENTITY), param1 -> param1.update(DSL.remainderFinder(), param2 -> {
                    Optional<String> var0x = param1.getOptional(var0);
                    return var0x.isPresent() && Objects.equals(var0x.get(), "minecraft:commandblock_minecart") ? param2 : fixTagCustomName(param2);
                })
        );
    }

    public static Dynamic<?> fixTagCustomName(Dynamic<?> param0) {
        String var0 = param0.get("CustomName").asString("");
        return var0.isEmpty()
            ? param0.remove("CustomName")
            : param0.set("CustomName", param0.createString(Component.Serializer.toJson(new TextComponent(var0))));
    }
}
