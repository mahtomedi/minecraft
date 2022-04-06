package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ComponentRenderUtils {
    private static final FormattedCharSequence INDENT = FormattedCharSequence.codepoint(32, Style.EMPTY);

    private static String stripColor(String param0) {
        return Minecraft.getInstance().options.chatColors().get() ? param0 : ChatFormatting.stripFormatting(param0);
    }

    public static List<FormattedCharSequence> wrapComponents(FormattedText param0, int param1, Font param2) {
        ComponentCollector var0 = new ComponentCollector();
        param0.visit((param1x, param2x) -> {
            var0.append(FormattedText.of(stripColor(param2x), param1x));
            return Optional.empty();
        }, Style.EMPTY);
        List<FormattedCharSequence> var1 = Lists.newArrayList();
        param2.getSplitter().splitLines(var0.getResultOrEmpty(), param1, Style.EMPTY, (param1x, param2x) -> {
            FormattedCharSequence var0x = Language.getInstance().getVisualOrder(param1x);
            var1.add(param2x ? FormattedCharSequence.composite(INDENT, var0x) : var0x);
        });
        return (List<FormattedCharSequence>)(var1.isEmpty() ? Lists.newArrayList(FormattedCharSequence.EMPTY) : var1);
    }
}
