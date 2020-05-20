package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;

public class JigsawRotationFix extends DataFix {
    private static final Map<String, String> renames = ImmutableMap.<String, String>builder()
        .put("down", "down_south")
        .put("up", "up_north")
        .put("north", "north_up")
        .put("south", "south_up")
        .put("west", "west_up")
        .put("east", "east_up")
        .build();

    public JigsawRotationFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private static Dynamic<?> fix(Dynamic<?> param0) {
        Optional<String> var0 = param0.get("Name").asString().result();
        return var0.equals(Optional.of("minecraft:jigsaw")) ? param0.update("Properties", param0x -> {
            String var0x = param0x.get("facing").asString("north");
            return param0x.remove("facing").set("orientation", param0x.createString(renames.getOrDefault(var0x, var0x)));
        }) : param0;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "jigsaw_rotation_fix",
            this.getInputSchema().getType(References.BLOCK_STATE),
            param0 -> param0.update(DSL.remainderFinder(), JigsawRotationFix::fix)
        );
    }
}
