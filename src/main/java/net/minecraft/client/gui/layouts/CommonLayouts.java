package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonLayouts {
    private static final int LABEL_SPACING = 4;

    private CommonLayouts() {
    }

    public static Layout labeledElement(Font param0, LayoutElement param1, Component param2) {
        return labeledElement(param0, param1, param2, param0x -> {
        });
    }

    public static Layout labeledElement(Font param0, LayoutElement param1, Component param2, Consumer<LayoutSettings> param3) {
        LinearLayout var0 = LinearLayout.vertical().spacing(4);
        var0.addChild(new StringWidget(param2, param0));
        var0.addChild(param1, param3);
        return var0;
    }
}
