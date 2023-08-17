package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.NameReportScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class NameReport extends Report {
    private final String reportedName;

    NameReport(UUID param0, Instant param1, UUID param2, String param3) {
        super(param0, param1, param2);
        this.reportedName = param3;
    }

    public String getReportedName() {
        return this.reportedName;
    }

    public NameReport copy() {
        NameReport var0 = new NameReport(this.reportId, this.createdAt, this.reportedProfileId, this.reportedName);
        var0.comments = this.comments;
        return var0;
    }

    @Override
    public Screen createScreen(Screen param0, ReportingContext param1) {
        return new NameReportScreen(param0, param1, this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder extends Report.Builder<NameReport> {
        public Builder(NameReport param0, AbuseReportLimits param1) {
            super(param0, param1);
        }

        public Builder(UUID param0, String param1, AbuseReportLimits param2) {
            super(new NameReport(UUID.randomUUID(), Instant.now(), param0, param1), param2);
        }

        @Override
        public boolean hasContent() {
            return StringUtils.isNotEmpty(this.comments());
        }

        @Nullable
        @Override
        public Report.CannotBuildReason checkBuildable() {
            return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? Report.CannotBuildReason.COMMENT_TOO_LONG : null;
        }

        @Override
        public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext param0) {
            Report.CannotBuildReason var0 = this.checkBuildable();
            if (var0 != null) {
                return Either.right(var0);
            } else {
                ReportedEntity var1 = new ReportedEntity(this.report.reportedProfileId);
                AbuseReport var2 = AbuseReport.name(this.report.comments, var1, this.report.createdAt);
                return Either.left(new Report.Result(this.report.reportId, ReportType.USERNAME, var2));
            }
        }
    }
}
