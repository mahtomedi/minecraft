package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ComponentRenderUtils {
    private static final FormattedText INDENT = FormattedText.of(" ");

    private static String stripColor(String param0) {
        return Minecraft.getInstance().options.chatColors ? param0 : ChatFormatting.stripFormatting(param0);
    }

    public static List<FormattedText> wrapComponents(FormattedText param0, int param1, Font param2) {
        ComponentCollector var0 = new ComponentCollector();
        param0.visit((param1x, param2x) -> {
            var0.append(FormattedText.of(stripColor(param2x), param1x));
            return Optional.empty();
        }, Style.EMPTY);
        List<FormattedText> var1 = param2.getSplitter().splitLines(var0.getResultOrEmpty(), param1, Style.EMPTY, INDENT);
        return (List<FormattedText>)(var1.isEmpty() ? Lists.newArrayList(FormattedText.EMPTY) : var1);
    }
}
