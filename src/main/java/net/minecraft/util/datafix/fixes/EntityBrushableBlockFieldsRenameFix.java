package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EntityBrushableBlockFieldsRenameFix extends NamedEntityFix {
    public EntityBrushableBlockFieldsRenameFix(Schema param0) {
        super(param0, false, "EntityBrushableBlockFieldsRenameFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return this.renameField(this.renameField(param0, "loot_table", "LootTable"), "loot_table_seed", "LootTableSeed");
    }

    private Dynamic<?> renameField(Dynamic<?> param0, String param1, String param2) {
        Optional<? extends Dynamic<?>> var0 = param0.get(param1).result();
        Optional<? extends Dynamic<?>> var1 = var0.map(param3 -> param0.remove(param1).set(param2, param3));
        return DataFixUtils.orElse(var1, param0);
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
