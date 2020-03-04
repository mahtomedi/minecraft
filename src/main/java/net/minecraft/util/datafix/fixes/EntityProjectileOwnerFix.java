package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OptionalDynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Arrays;
import java.util.function.Function;

public class EntityProjectileOwnerFix extends DataFix {
    public EntityProjectileOwnerFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        return this.fixTypeEverywhereTyped("EntityProjectileOwner", var0.getType(References.ENTITY), this::updateProjectiles);
    }

    private Typed<?> updateProjectiles(Typed<?> param0) {
        param0 = this.updateEntity(param0, "minecraft:egg", this::updateOwnerThrowable);
        param0 = this.updateEntity(param0, "minecraft:ender_pearl", this::updateOwnerThrowable);
        param0 = this.updateEntity(param0, "minecraft:experience_bottle", this::updateOwnerThrowable);
        param0 = this.updateEntity(param0, "minecraft:snowball", this::updateOwnerThrowable);
        param0 = this.updateEntity(param0, "minecraft:potion", this::updateOwnerThrowable);
        param0 = this.updateEntity(param0, "minecraft:potion", this::updateItemPotion);
        param0 = this.updateEntity(param0, "minecraft:llama_spit", this::updateOwnerLlamaSpit);
        param0 = this.updateEntity(param0, "minecraft:arrow", this::updateOwnerArrow);
        param0 = this.updateEntity(param0, "minecraft:spectral_arrow", this::updateOwnerArrow);
        return this.updateEntity(param0, "minecraft:trident", this::updateOwnerArrow);
    }

    private Dynamic<?> updateOwnerArrow(Dynamic<?> param0x) {
        long var0x = param0x.get("OwnerUUIDMost").asLong(0L);
        long var1 = param0x.get("OwnerUUIDLeast").asLong(0L);
        return this.setUUID(param0x, var0x, var1).remove("OwnerUUIDMost").remove("OwnerUUIDLeast");
    }

    private Dynamic<?> updateOwnerLlamaSpit(Dynamic<?> param0x) {
        OptionalDynamic var0x = param0x.get("Owner");
        long var1 = var0x.get("OwnerUUIDMost").asLong(0L);
        long var2 = var0x.get("OwnerUUIDLeast").asLong(0L);
        return this.setUUID(param0x, var1, var2).remove("Owner");
    }

    private Dynamic<?> updateItemPotion(Dynamic<?> param0x) {
        OptionalDynamic var0x = param0x.get("Potion");
        return param0x.set("Item", var0x.orElseEmptyMap()).remove("Potion");
    }

    private Dynamic<?> updateOwnerThrowable(Dynamic<?> param0x) {
        String var0x = "owner";
        OptionalDynamic<?> var1 = param0x.get("owner");
        long var2 = var1.get("M").asLong(0L);
        long var3 = var1.get("L").asLong(0L);
        return this.setUUID(param0x, var2, var3).remove("owner");
    }

    private Dynamic<?> setUUID(Dynamic<?> param0, long param1, long param2) {
        String var0 = "OwnerUUID";
        return param1 != 0L && param2 != 0L ? param0.set("OwnerUUID", param0.createIntList(Arrays.stream(createUUIDArray(param1, param2)))) : param0;
    }

    private static int[] createUUIDArray(long param0, long param1) {
        return new int[]{(int)(param0 >> 32), (int)param0, (int)(param1 >> 32), (int)param1};
    }

    private Typed<?> updateEntity(Typed<?> param0, String param1, Function<Dynamic<?>, Dynamic<?>> param2) {
        Type<?> var0 = this.getInputSchema().getChoiceType(References.ENTITY, param1);
        Type<?> var1 = this.getOutputSchema().getChoiceType(References.ENTITY, param1);
        return param0.updateTyped(DSL.namedChoice(param1, var0), var1, param1x -> param1x.update(DSL.remainderFinder(), param2));
    }
}
