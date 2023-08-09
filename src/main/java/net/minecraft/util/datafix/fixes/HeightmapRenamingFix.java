package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class HeightmapRenamingFix extends DataFix {
    public HeightmapRenamingFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("Level");
        return this.fixTypeEverywhereTyped(
            "HeightmapRenamingFix", var0, param1 -> param1.updateTyped(var1, param0x -> param0x.update(DSL.remainderFinder(), this::fix))
        );
    }

    private Dynamic<?> fix(Dynamic<?> param0) {
        Optional<? extends Dynamic<?>> var0 = param0.get("Heightmaps").result();
        if (var0.isEmpty()) {
            return param0;
        } else {
            Dynamic<?> var1 = var0.get();
            Optional<? extends Dynamic<?>> var2 = var1.get("LIQUID").result();
            if (var2.isPresent()) {
                var1 = var1.remove("LIQUID");
                var1 = var1.set("WORLD_SURFACE_WG", var2.get());
            }

            Optional<? extends Dynamic<?>> var3 = var1.get("SOLID").result();
            if (var3.isPresent()) {
                var1 = var1.remove("SOLID");
                var1 = var1.set("OCEAN_FLOOR_WG", var3.get());
                var1 = var1.set("OCEAN_FLOOR", var3.get());
            }

            Optional<? extends Dynamic<?>> var4 = var1.get("LIGHT").result();
            if (var4.isPresent()) {
                var1 = var1.remove("LIGHT");
                var1 = var1.set("LIGHT_BLOCKING", var4.get());
            }

            Optional<? extends Dynamic<?>> var5 = var1.get("RAIN").result();
            if (var5.isPresent()) {
                var1 = var1.remove("RAIN");
                var1 = var1.set("MOTION_BLOCKING", var5.get());
                var1 = var1.set("MOTION_BLOCKING_NO_LEAVES", var5.get());
            }

            return param0.set("Heightmaps", var1);
        }
    }
}
