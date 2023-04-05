package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class SculkSensorRemoveCooldownPhaseFix extends DataFix {
    public SculkSensorRemoveCooldownPhaseFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private static Dynamic<?> fix(Dynamic<?> param0) {
        Optional<String> var0 = param0.get("Name").asString().result();
        return !var0.equals(Optional.of("minecraft:sculk_sensor")) && !var0.equals(Optional.of("minecraft:calibrated_sculk_sensor"))
            ? param0
            : param0.update("Properties", param0x -> {
                String var0x = param0x.get("sculk_sensor_phase").asString("");
                return var0x.equals("cooldown") ? param0x.set("sculk_sensor_phase", param0x.createString("inactive")) : param0x;
            });
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "sculk_sensor_remove_cooldown_phase_fix",
            this.getInputSchema().getType(References.BLOCK_STATE),
            param0 -> param0.update(DSL.remainderFinder(), SculkSensorRemoveCooldownPhaseFix::fix)
        );
    }
}
