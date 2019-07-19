package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class EntityRedundantChanceTagsFix extends DataFix {
    public EntityRedundantChanceTagsFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "EntityRedundantChanceTagsFix",
            this.getInputSchema().getType(References.ENTITY),
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> {
                        Dynamic<?> var0x = param0x;
                        if (Objects.equals(
                            param0x.get("HandDropChances"), Optional.of(param0x.createList(Stream.generate(() -> var0x.createFloat(0.0F)).limit(2L)))
                        )) {
                            param0x = param0x.remove("HandDropChances");
                        }
        
                        if (Objects.equals(
                            param0x.get("ArmorDropChances"), Optional.of(param0x.createList(Stream.generate(() -> var0x.createFloat(0.0F)).limit(4L)))
                        )) {
                            param0x = param0x.remove("ArmorDropChances");
                        }
        
                        return param0x;
                    }
                )
        );
    }
}
