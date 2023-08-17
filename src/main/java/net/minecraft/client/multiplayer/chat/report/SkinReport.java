package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.SkinReportScreen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class SkinReport extends Report {
    final Supplier<PlayerSkin> skinGetter;

    SkinReport(UUID param0, Instant param1, UUID param2, Supplier<PlayerSkin> param3) {
        super(param0, param1, param2);
        this.skinGetter = param3;
    }

    public Supplier<PlayerSkin> getSkinGetter() {
        return this.skinGetter;
    }

    public SkinReport copy() {
        SkinReport var0 = new SkinReport(this.reportId, this.createdAt, this.reportedProfileId, this.skinGetter);
        var0.comments = this.comments;
        var0.reason = this.reason;
        return var0;
    }

    @Override
    public Screen createScreen(Screen param0, ReportingContext param1) {
        return new SkinReportScreen(param0, param1, this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder extends Report.Builder<SkinReport> {
        public Builder(SkinReport param0, AbuseReportLimits param1) {
            super(param0, param1);
        }

        public Builder(UUID param0, Supplier<PlayerSkin> param1, AbuseReportLimits param2) {
            super(new SkinReport(UUID.randomUUID(), Instant.now(), param0, param1), param2);
        }

        @Override
        public boolean hasContent() {
            return StringUtils.isNotEmpty(this.comments()) || this.reason() != null;
        }

        @Nullable
        @Override
        public Report.CannotBuildReason checkBuildable() {
            if (this.report.reason == null) {
                return Report.CannotBuildReason.NO_REASON;
            } else {
                return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? Report.CannotBuildReason.COMMENT_TOO_LONG : null;
            }
        }

        @Override
        public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext param0) {
            Report.CannotBuildReason var0 = this.checkBuildable();
            if (var0 != null) {
                return Either.right(var0);
            } else {
                String var1 = Objects.requireNonNull(this.report.reason).backendName();
                ReportedEntity var2 = new ReportedEntity(this.report.reportedProfileId);
                PlayerSkin var3 = this.report.skinGetter.get();
                String var4 = var3.textureUrl();
                AbuseReport var5 = AbuseReport.skin(this.report.comments, var1, var4, var2, this.report.createdAt);
                return Either.left(new Report.Result(this.report.reportId, ReportType.SKIN, var5));
            }
        }
    }
}
