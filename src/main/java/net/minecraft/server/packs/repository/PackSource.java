package net.minecraft.server.packs.repository;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface PackSource {
    UnaryOperator<Component> NO_DECORATION = UnaryOperator.identity();
    PackSource DEFAULT = create(NO_DECORATION, true);
    PackSource BUILT_IN = create(decorateWithSource("pack.source.builtin"), true);
    PackSource FEATURE = create(decorateWithSource("pack.source.feature"), false);
    PackSource WORLD = create(decorateWithSource("pack.source.world"), true);
    PackSource SERVER = create(decorateWithSource("pack.source.server"), true);

    Component decorate(Component var1);

    boolean shouldAddAutomatically();

    static PackSource create(final UnaryOperator<Component> param0, final boolean param1) {
        return new PackSource() {
            @Override
            public Component decorate(Component param0x) {
                return param0.apply(param0);
            }

            @Override
            public boolean shouldAddAutomatically() {
                return param1;
            }
        };
    }

    private static UnaryOperator<Component> decorateWithSource(String param0) {
        Component var0 = Component.translatable(param0);
        return param1 -> Component.translatable("pack.nameAndSource", param1, var0).withStyle(ChatFormatting.GRAY);
    }
}
