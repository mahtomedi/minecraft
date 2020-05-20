package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Set;

public class WallPropertyFix extends DataFix {
    private static final Set<String> WALL_BLOCKS = ImmutableSet.of(
        "minecraft:andesite_wall",
        "minecraft:brick_wall",
        "minecraft:cobblestone_wall",
        "minecraft:diorite_wall",
        "minecraft:end_stone_brick_wall",
        "minecraft:granite_wall",
        "minecraft:mossy_cobblestone_wall",
        "minecraft:mossy_stone_brick_wall",
        "minecraft:nether_brick_wall",
        "minecraft:prismarine_wall",
        "minecraft:red_nether_brick_wall",
        "minecraft:red_sandstone_wall",
        "minecraft:sandstone_wall",
        "minecraft:stone_brick_wall"
    );

    public WallPropertyFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "WallPropertyFix",
            this.getInputSchema().getType(References.BLOCK_STATE),
            param0 -> param0.update(DSL.remainderFinder(), WallPropertyFix::upgradeBlockStateTag)
        );
    }

    private static String mapProperty(String param0) {
        return "true".equals(param0) ? "low" : "none";
    }

    private static <T> Dynamic<T> fixWallProperty(Dynamic<T> param0, String param1) {
        return param0.update(
            param1, param0x -> DataFixUtils.orElse(param0x.asString().result().map(WallPropertyFix::mapProperty).map(param0x::createString), param0x)
        );
    }

    private static <T> Dynamic<T> upgradeBlockStateTag(Dynamic<T> param0) {
        boolean var0 = param0.get("Name").asString().result().filter(WALL_BLOCKS::contains).isPresent();
        return !var0 ? param0 : param0.update("Properties", param0x -> {
            Dynamic<?> var0x = fixWallProperty(param0x, "east");
            var0x = fixWallProperty(var0x, "west");
            var0x = fixWallProperty(var0x, "north");
            return fixWallProperty(var0x, "south");
        });
    }
}
