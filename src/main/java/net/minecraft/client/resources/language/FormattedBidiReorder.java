package net.minecraft.client.resources.language;

import com.google.common.collect.Lists;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.BidiRun;
import java.util.List;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.SubStringSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FormattedBidiReorder {
    public static FormattedCharSequence reorder(FormattedText param0, boolean param1) {
        SubStringSource var0 = SubStringSource.create(param0, UCharacter::getMirror, FormattedBidiReorder::shape);
        Bidi var1 = new Bidi(var0.getPlainText(), param1 ? 127 : 126);
        var1.setReorderingMode(0);
        List<FormattedCharSequence> var2 = Lists.newArrayList();
        int var3 = var1.countRuns();

        for(int var4 = 0; var4 < var3; ++var4) {
            BidiRun var5 = var1.getVisualRun(var4);
            var2.addAll(var0.substring(var5.getStart(), var5.getLength(), var5.isOddRun()));
        }

        return FormattedCharSequence.composite(var2);
    }

    private static String shape(String param0x) {
        try {
            return new ArabicShaping(8).shape(param0x);
        } catch (Exception var2) {
            return param0x;
        }
    }
}
