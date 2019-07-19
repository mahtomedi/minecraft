package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.UUID;

public class EntityStringUuidFix extends DataFix {
    public EntityStringUuidFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "EntityStringUuidFix",
            this.getInputSchema().getType(References.ENTITY),
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> {
                        Optional<String> var0x = param0x.get("UUID").asString();
                        if (var0x.isPresent()) {
                            UUID var1 = UUID.fromString((String)var0x.get());
                            return param0x.remove("UUID")
                                .set("UUIDMost", param0x.createLong(var1.getMostSignificantBits()))
                                .set("UUIDLeast", param0x.createLong(var1.getLeastSignificantBits()));
                        } else {
                            return param0x;
                        }
                    }
                )
        );
    }
}
