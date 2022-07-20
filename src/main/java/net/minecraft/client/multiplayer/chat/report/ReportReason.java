package net.minecraft.client.multiplayer.chat.report;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ReportReason {
    FALSE_REPORTING(2, "false_reporting", false),
    HATE_SPEECH(5, "hate_speech", true),
    TERRORISM_OR_VIOLENT_EXTREMISM(16, "terrorism_or_violent_extremism", true),
    CHILD_SEXUAL_EXPLOITATION_OR_ABUSE(17, "child_sexual_exploitation_or_abuse", true),
    IMMINENT_HARM(18, "imminent_harm", true),
    NON_CONSENSUAL_INTIMATE_IMAGERY(19, "non_consensual_intimate_imagery", true),
    HARASSMENT_OR_BULLYING(21, "harassment_or_bullying", true),
    DEFAMATION_IMPERSONATION_FALSE_INFORMATION(27, "defamation_impersonation_false_information", true),
    SELF_HARM_OR_SUICIDE(31, "self_harm_or_suicide", true),
    ALCOHOL_TOBACCO_DRUGS(39, "alcohol_tobacco_drugs", true);

    private final int id;
    private final String backendName;
    private final boolean reportable;
    private final Component title;
    private final Component description;

    private ReportReason(int param0, String param1, boolean param2) {
        this.id = param0;
        this.backendName = param1.toUpperCase(Locale.ROOT);
        this.reportable = param2;
        String param3 = "gui.abuseReport.reason." + param1;
        this.title = Component.translatable(param3);
        this.description = Component.translatable(param3 + ".description");
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

    public boolean reportable() {
        return this.reportable;
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
