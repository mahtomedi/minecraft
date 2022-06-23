package net.minecraft.client.multiplayer.chat.report;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ReportReason {
    HATE_SPEECH(5, "hate_speech"),
    TERRORISM_OR_VIOLENT_EXTREMISM(16, "terrorism_or_violent_extremism"),
    CHILD_SEXUAL_EXPLOITATION_OR_ABUSE(17, "child_sexual_exploitation_or_abuse"),
    IMMINENT_HARM(18, "imminent_harm"),
    NON_CONSENSUAL_INTIMATE_IMAGERY(19, "non_consensual_intimate_imagery"),
    HARASSMENT_OR_BULLYING(21, "harassment_or_bullying"),
    DEFAMATION_IMPERSONATION_FALSE_INFORMATION(27, "defamation_impersonation_false_information"),
    SELF_HARM_OR_SUICIDE(31, "self_harm_or_suicide"),
    ALCOHOL_TOBACCO_DRUGS(39, "alcohol_tobacco_drugs");

    private final int id;
    private final String backendName;
    private final Component title;
    private final Component description;

    private ReportReason(int param0, String param1) {
        this.id = param0;
        this.backendName = param1.toUpperCase(Locale.ROOT);
        String param2 = "gui.abuseReport.reason." + param1;
        this.title = Component.translatable(param2);
        this.description = Component.translatable(param2 + ".description");
    }

    public String backendName() {
        return this.backendName;
    }

    public Component title() {
        return this.title;
    }

    public Component description() {
        return this.description;
    }

    @Nullable
    public static Component getTranslationById(int param0) {
        for(ReportReason var0 : values()) {
            if (var0.id == param0) {
                return var0.title;
            }
        }

        return null;
    }
}
