package net.minecraft.realms;

import java.time.Duration;
import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NarrationHelper {
    private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));

    public static void now(String param0) {
        NarratorChatListener var0 = NarratorChatListener.INSTANCE;
        var0.clear();
        var0.handle(ChatType.SYSTEM, new TextComponent(fixNarrationNewlines(param0)), Util.NIL_UUID);
    }

    private static String fixNarrationNewlines(String param0) {
        return param0.replace("\\n", System.lineSeparator());
    }

    public static void now(String... param0) {
        now(Arrays.asList(param0));
    }

    public static void now(Iterable<String> param0) {
        now(join(param0));
    }

    public static String join(Iterable<String> param0) {
        return String.join(System.lineSeparator(), param0);
    }

    public static void repeatedly(String param0) {
        REPEATED_NARRATOR.narrate(fixNarrationNewlines(param0));
    }
}
