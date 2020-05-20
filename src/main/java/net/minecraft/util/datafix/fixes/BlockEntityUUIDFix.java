package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityUUIDFix extends AbstractUUIDFix {
    public BlockEntityUUIDFix(Schema param0) {
        super(param0, References.BLOCK_ENTITY);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), param0 -> {
            param0 = this.updateNamedChoice(param0, "minecraft:conduit", this::updateConduit);
            return this.updateNamedChoice(param0, "minecraft:skull", this::updateSkull);
        });
    }

    private Dynamic<?> updateSkull(Dynamic<?> param0) {
        return param0.get("Owner")
            .get()
            .map(param0x -> replaceUUIDString(param0x, "Id", "Id").orElse(param0x))
            .map(param1 -> param0.remove("Owner").set("SkullOwner", param1))
            .result()
            .orElse(param0);
    }

    private Dynamic<?> updateConduit(Dynamic<?> param0) {
        return replaceUUIDMLTag(param0, "target_uuid", "Target").orElse(param0);
    }
}
