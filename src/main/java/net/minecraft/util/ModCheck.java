package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModCheck(ModCheck.Confidence confidence, String description) {
    public static ModCheck identify(String param0, Supplier<String> param1, String param2, Class<?> param3) {
        String var0 = param1.get();
        if (!param0.equals(var0)) {
            return new ModCheck(ModCheck.Confidence.DEFINITELY, param2 + " brand changed to '" + var0 + "'");
        } else {
            return param3.getSigners() == null
                ? new ModCheck(ModCheck.Confidence.VERY_LIKELY, param2 + " jar signature invalidated")
                : new ModCheck(ModCheck.Confidence.PROBABLY_NOT, param2 + " jar signature and brand is untouched");
        }
    }

    public boolean shouldReportAsModified() {
        return this.confidence.shouldReportAsModified;
    }

    public ModCheck merge(ModCheck param0) {
        return new ModCheck(ObjectUtils.max(this.confidence, param0.confidence), this.description + "; " + param0.description);
    }

    public String fullDescription() {
        return this.confidence.description + " " + this.description;
    }

    public static enum Confidence {
        PROBABLY_NOT("Probably not.", false),
        VERY_LIKELY("Very likely;", true),
        DEFINITELY("Definitely;", true);

        final String description;
        final boolean shouldReportAsModified;

        private Confidence(String param0, boolean param1) {
            this.description = param0;
            this.shouldReportAsModified = param1;
        }
    }
}
