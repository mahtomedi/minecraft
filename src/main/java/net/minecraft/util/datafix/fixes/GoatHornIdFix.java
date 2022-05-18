package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class GoatHornIdFix extends ItemStackTagFix {
    private static final String[] INSTRUMENTS = new String[]{
        "minecraft:ponder_goat_horn",
        "minecraft:sing_goat_horn",
        "minecraft:seek_goat_horn",
        "minecraft:feel_goat_horn",
        "minecraft:admire_goat_horn",
        "minecraft:call_goat_horn",
        "minecraft:yearn_goat_horn",
        "minecraft:dream_goat_horn"
    };

    public GoatHornIdFix(Schema param0) {
        super(param0, "GoatHornIdFix", param0x -> param0x.equals("minecraft:goat_horn"));
    }

    @Override
    protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> param0) {
        int var0 = param0.get("SoundVariant").asInt(0);
        String var1 = INSTRUMENTS[var0 >= 0 && var0 < INSTRUMENTS.length ? var0 : 0];
        return param0.remove("SoundVariant").set("instrument", param0.createString(var1));
    }
}
