package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.packs.PackResources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ResourceLoadStateTracker {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private ResourceLoadStateTracker.ReloadState reloadState;
    private int reloadCount;

    public void startReload(ResourceLoadStateTracker.ReloadReason param0, List<PackResources> param1) {
        ++this.reloadCount;
        if (this.reloadState != null && !this.reloadState.finished) {
            LOGGER.warn("Reload already ongoing, replacing");
        }

        this.reloadState = new ResourceLoadStateTracker.ReloadState(
            param0, param1.stream().map(PackResources::getName).collect(ImmutableList.toImmutableList())
        );
    }

    public void startRecovery(Throwable param0) {
        if (this.reloadState == null) {
            LOGGER.warn("Trying to signal reload recovery, but nothing was started");
            this.reloadState = new ResourceLoadStateTracker.ReloadState(ResourceLoadStateTracker.ReloadReason.UNKNOWN, ImmutableList.of());
        }

        this.reloadState.recoveryReloadInfo = new ResourceLoadStateTracker.RecoveryInfo(param0);
    }

    public void finishReload() {
        if (this.reloadState == null) {
            LOGGER.warn("Trying to finish reload, but nothing was started");
        } else {
            this.reloadState.finished = true;
        }

    }

    public void fillCrashReport(CrashReport param0) {
        CrashReportCategory var0 = param0.addCategory("Last reload");
        var0.setDetail("Reload number", this.reloadCount);
        if (this.reloadState != null) {
            this.reloadState.fillCrashInfo(var0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    static class RecoveryInfo {
        private final Throwable error;

        private RecoveryInfo(Throwable param0) {
            this.error = param0;
        }

        public void fillCrashInfo(CrashReportCategory param0) {
            param0.setDetail("Recovery", "Yes");
            param0.setDetail("Recovery reason", () -> {
                StringWriter var0 = new StringWriter();
                this.error.printStackTrace(new PrintWriter(var0));
                return var0.toString();
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum ReloadReason {
        INITIAL("initial"),
        MANUAL("manual"),
        UNKNOWN("unknown");

        private final String name;

        private ReloadReason(String param0) {
            this.name = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ReloadState {
        private final ResourceLoadStateTracker.ReloadReason reloadReason;
        private final List<String> packs;
        @Nullable
        private ResourceLoadStateTracker.RecoveryInfo recoveryReloadInfo;
        private boolean finished;

        private ReloadState(ResourceLoadStateTracker.ReloadReason param0, List<String> param1) {
            this.reloadReason = param0;
            this.packs = param1;
        }

        public void fillCrashInfo(CrashReportCategory param0) {
            param0.setDetail("Reload reason", this.reloadReason.name);
            param0.setDetail("Finished", this.finished ? "Yes" : "No");
            param0.setDetail("Packs", () -> String.join(", ", this.packs));
            if (this.recoveryReloadInfo != null) {
                this.recoveryReloadInfo.fillCrashInfo(param0);
            }

        }
    }
}
