package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Report {
    protected final UUID reportId;
    protected final Instant createdAt;
    protected final UUID reportedProfileId;
    protected String comments = "";
    @Nullable
    protected ReportReason reason;

    public Report(UUID param0, Instant param1, UUID param2) {
        this.reportId = param0;
        this.createdAt = param1;
        this.reportedProfileId = param2;
    }

    public boolean isReportedPlayer(UUID param0) {
        return param0.equals(this.reportedProfileId);
    }

    public abstract Report copy();

    public abstract Screen createScreen(Screen var1, ReportingContext var2);

    @OnlyIn(Dist.CLIENT)
    public abstract static class Builder<R extends Report> {
        protected final R report;
        protected final AbuseReportLimits limits;

        protected Builder(R param0, AbuseReportLimits param1) {
            this.report = param0;
            this.limits = param1;
        }

        public R report() {
            return this.report;
        }

        public UUID reportedProfileId() {
            return this.report.reportedProfileId;
        }

        public String comments() {
            return this.report.comments;
        }

        public void setComments(String param0) {
            this.report.comments = param0;
        }

        @Nullable
        public ReportReason reason() {
            return this.report.reason;
        }

        public void setReason(ReportReason param0) {
            this.report.reason = param0;
        }

        public abstract boolean hasContent();

        @Nullable
        public abstract Report.CannotBuildReason checkBuildable();

        public abstract Either<Report.Result, Report.CannotBuildReason> build(ReportingContext var1);
    }

    @OnlyIn(Dist.CLIENT)
    public static record CannotBuildReason(Component message) {
        public static final Report.CannotBuildReason NO_REASON = new Report.CannotBuildReason(Component.translatable("gui.abuseReport.send.no_reason"));
        public static final Report.CannotBuildReason NO_REPORTED_MESSAGES = new Report.CannotBuildReason(
            Component.translatable("gui.chatReport.send.no_reported_messages")
        );
        public static final Report.CannotBuildReason TOO_MANY_MESSAGES = new Report.CannotBuildReason(
            Component.translatable("gui.chatReport.send.too_many_messages")
        );
        public static final Report.CannotBuildReason COMMENT_TOO_LONG = new Report.CannotBuildReason(
            Component.translatable("gui.abuseReport.send.comment_too_long")
        );

        public Tooltip tooltip() {
            return Tooltip.create(this.message);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Result(UUID id, ReportType reportType, AbuseReport report) {
    }
}
