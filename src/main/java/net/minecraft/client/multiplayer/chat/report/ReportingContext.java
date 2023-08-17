package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ReportingContext {
    private static final int LOG_CAPACITY = 1024;
    private final AbuseReportSender sender;
    private final ReportEnvironment environment;
    private final ChatLog chatLog;
    @Nullable
    private Report draftReport;

    public ReportingContext(AbuseReportSender param0, ReportEnvironment param1, ChatLog param2) {
        this.sender = param0;
        this.environment = param1;
        this.chatLog = param2;
    }

    public static ReportingContext create(ReportEnvironment param0, UserApiService param1) {
        ChatLog var0 = new ChatLog(1024);
        AbuseReportSender var1 = AbuseReportSender.create(param0, param1);
        return new ReportingContext(var1, param0, var0);
    }

    public void draftReportHandled(Minecraft param0, Screen param1, Runnable param2, boolean param3) {
        if (this.draftReport != null) {
            Report var0 = this.draftReport.copy();
            param0.setScreen(
                new ConfirmScreen(
                    param4 -> {
                        this.setReportDraft(null);
                        if (param4) {
                            param0.setScreen(var0.createScreen(param1, this));
                        } else {
                            param2.run();
                        }
        
                    },
                    Component.translatable(param3 ? "gui.abuseReport.draft.quittotitle.title" : "gui.abuseReport.draft.title"),
                    Component.translatable(param3 ? "gui.abuseReport.draft.quittotitle.content" : "gui.abuseReport.draft.content"),
                    Component.translatable("gui.abuseReport.draft.edit"),
                    Component.translatable("gui.abuseReport.draft.discard")
                )
            );
        } else {
            param2.run();
        }

    }

    public AbuseReportSender sender() {
        return this.sender;
    }

    public ChatLog chatLog() {
        return this.chatLog;
    }

    public boolean matches(ReportEnvironment param0) {
        return Objects.equals(this.environment, param0);
    }

    public void setReportDraft(@Nullable Report param0) {
        this.draftReport = param0;
    }

    public boolean hasDraftReport() {
        return this.draftReport != null;
    }

    public boolean hasDraftReportFor(UUID param0) {
        return this.hasDraftReport() && this.draftReport.isReportedPlayer(param0);
    }
}
