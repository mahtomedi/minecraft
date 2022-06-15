package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.RollingMemoryChatLog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ReportingContext(AbuseReportSender sender, ReportEnvironment environment, ChatLog chatLog) {
    private static final int LOG_CAPACITY = 1024;

    public static ReportingContext create(ReportEnvironment param0, UserApiService param1) {
        RollingMemoryChatLog var0 = new RollingMemoryChatLog(1024);
        AbuseReportSender var1 = AbuseReportSender.create(param0, param1);
        return new ReportingContext(var1, param0, var0);
    }

    public boolean matches(ReportEnvironment param0) {
        return Objects.equals(this.environment, param0);
    }
}
