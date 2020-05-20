package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RedstoneWireConnectionsFix extends DataFix {
    public RedstoneWireConnectionsFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        return this.fixTypeEverywhereTyped(
            "RedstoneConnectionsFix", var0.getType(References.BLOCK_STATE), param0 -> param0.update(DSL.remainderFinder(), this::updateRedstoneConnections)
        );
    }

    private <T> Dynamic<T> updateRedstoneConnections(Dynamic<T> param0) {
        boolean var0 = param0.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
        return !var0
            ? param0
            : param0.update(
                "Properties",
                param0x -> {
                    String var0x = param0x.get("east").asString("none");
                    String var1x = param0x.get("west").asString("none");
                    String var2x = param0x.get("north").asString("none");
                    String var3 = param0x.get("south").asString("none");
                    boolean var4 = isConnected(var0x) || isConnected(var1x);
                    boolean var5 = isConnected(var2x) || isConnected(var3);
                    String var6 = !isConnected(var0x) && !var5 ? "side" : var0x;
                    String var7 = !isConnected(var1x) && !var5 ? "side" : var1x;
                    String var8 = !isConnected(var2x) && !var4 ? "side" : var2x;
                    String var9 = !isConnected(var3) && !var4 ? "side" : var3;
                    return param0x.update("east", param1 -> param1.createString(var6))
                        .update("west", param1 -> param1.createString(var7))
                        .update("north", param1 -> param1.createString(var8))
                        .update("south", param1 -> param1.createString(var9));
                }
            );
    }

    private static boolean isConnected(String param0) {
        return !"none".equals(param0);
    }
}
