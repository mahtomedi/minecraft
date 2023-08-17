package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.datafixers.util.Unit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface AbuseReportSender {
    static AbuseReportSender create(ReportEnvironment param0, UserApiService param1) {
        return new AbuseReportSender.Services(param0, param1);
    }

    CompletableFuture<Unit> send(UUID var1, ReportType var2, AbuseReport var3);

    boolean isEnabled();

    default AbuseReportLimits reportLimits() {
        return AbuseReportLimits.DEFAULTS;
    }

    @OnlyIn(Dist.CLIENT)
    public static class SendException extends ThrowingComponent {
        public SendException(Component param0, Throwable param1) {
            super(param0, param1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Services(ReportEnvironment environment, UserApiService userApiService) implements AbuseReportSender {
        private static final Component SERVICE_UNAVAILABLE_TEXT = Component.translatable("gui.abuseReport.send.service_unavailable");
        private static final Component HTTP_ERROR_TEXT = Component.translatable("gui.abuseReport.send.http_error");
        private static final Component JSON_ERROR_TEXT = Component.translatable("gui.abuseReport.send.json_error");

        @Override
        public CompletableFuture<Unit> send(UUID param0, ReportType param1, AbuseReport param2) {
            return CompletableFuture.supplyAsync(
                () -> {
                    AbuseReportRequest var0 = new AbuseReportRequest(
                        1,
                        param0,
                        param2,
                        this.environment.clientInfo(),
                        this.environment.thirdPartyServerInfo(),
                        this.environment.realmInfo(),
                        param1.backendName()
                    );
    
                    try {
                        this.userApiService.reportAbuse(var0);
                        return Unit.INSTANCE;
                    } catch (MinecraftClientHttpException var7) {
                        Component var2 = this.getHttpErrorDescription(var7);
                        throw new CompletionException(new AbuseReportSender.SendException(var2, var7));
                    } catch (MinecraftClientException var8) {
                        Component var4 = this.getErrorDescription(var8);
                        throw new CompletionException(new AbuseReportSender.SendException(var4, var8));
                    }
                },
                Util.ioPool()
            );
        }

        @Override
        public boolean isEnabled() {
            return this.userApiService.canSendReports();
        }

        private Component getHttpErrorDescription(MinecraftClientHttpException param0) {
            return Component.translatable("gui.abuseReport.send.error_message", param0.getMessage());
        }

        private Component getErrorDescription(MinecraftClientException param0) {
            return switch(param0.getType()) {
                case SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE_TEXT;
                case HTTP_ERROR -> HTTP_ERROR_TEXT;
                case JSON_ERROR -> JSON_ERROR_TEXT;
            };
        }

        @Override
        public AbuseReportLimits reportLimits() {
            return this.userApiService.getAbuseReportLimits();
        }
    }
}
