package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1451_7 extends NamespacedSchema {
    public V1451_7(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        param0.registerType(
            false,
            References.STRUCTURE_FEATURE,
            () -> DSL.optionalFields(
                    "Children",
                    DSL.list(
                        DSL.optionalFields(
                            "CA",
                            References.BLOCK_STATE.in(param0),
                            "CB",
                            References.BLOCK_STATE.in(param0),
                            "CC",
                            References.BLOCK_STATE.in(param0),
                            "CD",
                            References.BLOCK_STATE.in(param0)
                        )
                    )
                )
        );
    }
}
