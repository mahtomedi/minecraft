package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1451_3 extends NamespacedSchema {
    public V1451_3(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        param0.registerSimple(var0, "minecraft:egg");
        param0.registerSimple(var0, "minecraft:ender_pearl");
        param0.registerSimple(var0, "minecraft:fireball");
        param0.register(var0, "minecraft:potion", param1 -> DSL.optionalFields("Potion", References.ITEM_STACK.in(param0)));
        param0.registerSimple(var0, "minecraft:small_fireball");
        param0.registerSimple(var0, "minecraft:snowball");
        param0.registerSimple(var0, "minecraft:wither_skull");
        param0.registerSimple(var0, "minecraft:xp_bottle");
        param0.register(var0, "minecraft:arrow", () -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(param0)));
        param0.register(var0, "minecraft:enderman", () -> DSL.optionalFields("carriedBlockState", References.BLOCK_STATE.in(param0), V100.equipment(param0)));
        param0.register(
            var0,
            "minecraft:falling_block",
            () -> DSL.optionalFields("BlockState", References.BLOCK_STATE.in(param0), "TileEntityData", References.BLOCK_ENTITY.in(param0))
        );
        param0.register(var0, "minecraft:spectral_arrow", () -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(param0)));
        param0.register(
            var0,
            "minecraft:chest_minecart",
            () -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        param0.register(var0, "minecraft:commandblock_minecart", () -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        param0.register(var0, "minecraft:furnace_minecart", () -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        param0.register(
            var0,
            "minecraft:hopper_minecart",
            () -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        param0.register(var0, "minecraft:minecart", () -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        param0.register(
            var0,
            "minecraft:spawner_minecart",
            () -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0), References.UNTAGGED_SPAWNER.in(param0))
        );
        param0.register(var0, "minecraft:tnt_minecart", () -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        return var0;
    }
}
