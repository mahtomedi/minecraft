package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.stream.Collectors;

public class OptionsKeyLwjgl3Fix extends DataFix {
    public static final String KEY_UNKNOWN = "key.unknown";
    private static final Int2ObjectMap<String> MAP = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(0, "key.unknown");
        param0.put(11, "key.0");
        param0.put(2, "key.1");
        param0.put(3, "key.2");
        param0.put(4, "key.3");
        param0.put(5, "key.4");
        param0.put(6, "key.5");
        param0.put(7, "key.6");
        param0.put(8, "key.7");
        param0.put(9, "key.8");
        param0.put(10, "key.9");
        param0.put(30, "key.a");
        param0.put(40, "key.apostrophe");
        param0.put(48, "key.b");
        param0.put(43, "key.backslash");
        param0.put(14, "key.backspace");
        param0.put(46, "key.c");
        param0.put(58, "key.caps.lock");
        param0.put(51, "key.comma");
        param0.put(32, "key.d");
        param0.put(211, "key.delete");
        param0.put(208, "key.down");
        param0.put(18, "key.e");
        param0.put(207, "key.end");
        param0.put(28, "key.enter");
        param0.put(13, "key.equal");
        param0.put(1, "key.escape");
        param0.put(33, "key.f");
        param0.put(59, "key.f1");
        param0.put(68, "key.f10");
        param0.put(87, "key.f11");
        param0.put(88, "key.f12");
        param0.put(100, "key.f13");
        param0.put(101, "key.f14");
        param0.put(102, "key.f15");
        param0.put(103, "key.f16");
        param0.put(104, "key.f17");
        param0.put(105, "key.f18");
        param0.put(113, "key.f19");
        param0.put(60, "key.f2");
        param0.put(61, "key.f3");
        param0.put(62, "key.f4");
        param0.put(63, "key.f5");
        param0.put(64, "key.f6");
        param0.put(65, "key.f7");
        param0.put(66, "key.f8");
        param0.put(67, "key.f9");
        param0.put(34, "key.g");
        param0.put(41, "key.grave.accent");
        param0.put(35, "key.h");
        param0.put(199, "key.home");
        param0.put(23, "key.i");
        param0.put(210, "key.insert");
        param0.put(36, "key.j");
        param0.put(37, "key.k");
        param0.put(82, "key.keypad.0");
        param0.put(79, "key.keypad.1");
        param0.put(80, "key.keypad.2");
        param0.put(81, "key.keypad.3");
        param0.put(75, "key.keypad.4");
        param0.put(76, "key.keypad.5");
        param0.put(77, "key.keypad.6");
        param0.put(71, "key.keypad.7");
        param0.put(72, "key.keypad.8");
        param0.put(73, "key.keypad.9");
        param0.put(78, "key.keypad.add");
        param0.put(83, "key.keypad.decimal");
        param0.put(181, "key.keypad.divide");
        param0.put(156, "key.keypad.enter");
        param0.put(141, "key.keypad.equal");
        param0.put(55, "key.keypad.multiply");
        param0.put(74, "key.keypad.subtract");
        param0.put(38, "key.l");
        param0.put(203, "key.left");
        param0.put(56, "key.left.alt");
        param0.put(26, "key.left.bracket");
        param0.put(29, "key.left.control");
        param0.put(42, "key.left.shift");
        param0.put(219, "key.left.win");
        param0.put(50, "key.m");
        param0.put(12, "key.minus");
        param0.put(49, "key.n");
        param0.put(69, "key.num.lock");
        param0.put(24, "key.o");
        param0.put(25, "key.p");
        param0.put(209, "key.page.down");
        param0.put(201, "key.page.up");
        param0.put(197, "key.pause");
        param0.put(52, "key.period");
        param0.put(183, "key.print.screen");
        param0.put(16, "key.q");
        param0.put(19, "key.r");
        param0.put(205, "key.right");
        param0.put(184, "key.right.alt");
        param0.put(27, "key.right.bracket");
        param0.put(157, "key.right.control");
        param0.put(54, "key.right.shift");
        param0.put(220, "key.right.win");
        param0.put(31, "key.s");
        param0.put(70, "key.scroll.lock");
        param0.put(39, "key.semicolon");
        param0.put(53, "key.slash");
        param0.put(57, "key.space");
        param0.put(20, "key.t");
        param0.put(15, "key.tab");
        param0.put(22, "key.u");
        param0.put(200, "key.up");
        param0.put(47, "key.v");
        param0.put(17, "key.w");
        param0.put(45, "key.x");
        param0.put(21, "key.y");
        param0.put(44, "key.z");
    });

    public OptionsKeyLwjgl3Fix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsKeyLwjgl3Fix",
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(
                    DSL.remainderFinder(), param0x -> param0x.getMapValues().map(param1 -> param0x.createMap(param1.entrySet().stream().map(param0xxx -> {
                                if (param0xxx.getKey().asString("").startsWith("key_")) {
                                    int var0x = Integer.parseInt(param0xxx.getValue().asString(""));
                                    if (var0x < 0) {
                                        int var1 = var0x + 100;
                                        String var2;
                                        if (var1 == 0) {
                                            var2 = "key.mouse.left";
                                        } else if (var1 == 1) {
                                            var2 = "key.mouse.right";
                                        } else if (var1 == 2) {
                                            var2 = "key.mouse.middle";
                                        } else {
                                            var2 = "key.mouse." + (var1 + 1);
                                        }
        
                                        return Pair.of(param0xxx.getKey(), param0xxx.getValue().createString(var2));
                                    } else {
                                        String var6 = MAP.getOrDefault(var0x, "key.unknown");
                                        return Pair.of(param0xxx.getKey(), param0xxx.getValue().createString(var6));
                                    }
                                } else {
                                    return Pair.of(param0xxx.getKey(), param0xxx.getValue());
                                }
                            }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse(param0x)
                )
        );
    }
}
