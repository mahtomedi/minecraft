package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;

public class BeehivePoiRenameFix extends PoiTypeRename {
    public BeehivePoiRenameFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected String rename(String param0) {
        return param0.equals("minecraft:bee_hive") ? "minecraft:beehive" : param0;
    }
}
