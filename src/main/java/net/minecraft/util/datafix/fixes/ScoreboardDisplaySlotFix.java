package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import javax.annotation.Nullable;

public class ScoreboardDisplaySlotFix extends DataFix {
    private static final Map<String, String> SLOT_RENAMES = ImmutableMap.<String, String>builder()
        .put("slot_0", "list")
        .put("slot_1", "sidebar")
        .put("slot_2", "below_name")
        .put("slot_3", "sidebar.team.black")
        .put("slot_4", "sidebar.team.dark_blue")
        .put("slot_5", "sidebar.team.dark_green")
        .put("slot_6", "sidebar.team.dark_aqua")
        .put("slot_7", "sidebar.team.dark_red")
        .put("slot_8", "sidebar.team.dark_purple")
        .put("slot_9", "sidebar.team.gold")
        .put("slot_10", "sidebar.team.gray")
        .put("slot_11", "sidebar.team.dark_gray")
        .put("slot_12", "sidebar.team.blue")
        .put("slot_13", "sidebar.team.green")
        .put("slot_14", "sidebar.team.aqua")
        .put("slot_15", "sidebar.team.red")
        .put("slot_16", "sidebar.team.light_purple")
        .put("slot_17", "sidebar.team.yellow")
        .put("slot_18", "sidebar.team.white")
        .build();

    public ScoreboardDisplaySlotFix(Schema param0) {
        super(param0, false);
    }

    @Nullable
    private static String rename(String param0) {
        return SLOT_RENAMES.get(param0);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.SAVED_DATA_SCOREBOARD);
        OpticFinder<?> var1 = var0.findField("data");
        return this.fixTypeEverywhereTyped(
            "Scoreboard DisplaySlot rename",
            var0,
            param1 -> param1.updateTyped(
                    var1,
                    param0x -> param0x.update(
                            DSL.remainderFinder(),
                            param0xx -> param0xx.update(
                                    "DisplaySlots",
                                    param0xxx -> param0xxx.updateMapValues(
                                            param0xxxx -> param0xxxx.mapFirst(
                                                    param0xxxxx -> DataFixUtils.orElse(
                                                            param0xxxxx.asString()
                                                                .result()
                                                                .map(ScoreboardDisplaySlotFix::rename)
                                                                .map(param0xxxxx::createString),
                                                            param0xxxxx
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
