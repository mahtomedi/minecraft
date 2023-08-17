package net.minecraft.client.multiplayer.chat.report;

import java.util.Locale;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ReportType {
    CHAT("chat"),
    SKIN("skin"),
    USERNAME("username");

    private final String backendName;

    private ReportType(String param0) {
        this.backendName = param0.toUpperCase(Locale.ROOT);
    }

    public String backendName() {
        return this.backendName;
    }
}
