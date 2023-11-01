package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.ClientInfo;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.RealmInfo;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.ThirdPartyServerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ReportEnvironment(String clientVersion, @Nullable ReportEnvironment.Server server) {
    public static ReportEnvironment local() {
        return create(null);
    }

    public static ReportEnvironment thirdParty(String param0) {
        return create(new ReportEnvironment.Server.ThirdParty(param0));
    }

    public static ReportEnvironment realm(RealmsServer param0) {
        return create(new ReportEnvironment.Server.Realm(param0));
    }

    public static ReportEnvironment create(@Nullable ReportEnvironment.Server param0) {
        return new ReportEnvironment(getClientVersion(), param0);
    }

    public ClientInfo clientInfo() {
        return new ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
    }

    @Nullable
    public ThirdPartyServerInfo thirdPartyServerInfo() {
        ReportEnvironment.Server var2 = this.server;
        return var2 instanceof ReportEnvironment.Server.ThirdParty var0 ? new ThirdPartyServerInfo(var0.ip) : null;
    }

    @Nullable
    public RealmInfo realmInfo() {
        ReportEnvironment.Server var2 = this.server;
        return var2 instanceof ReportEnvironment.Server.Realm var0 ? new RealmInfo(String.valueOf(var0.realmId()), var0.slotId()) : null;
    }

    private static String getClientVersion() {
        StringBuilder var0 = new StringBuilder();
        var0.append("23w44a");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            var0.append(" (modded)");
        }

        return var0.toString();
    }

    @OnlyIn(Dist.CLIENT)
    public interface Server {
        @OnlyIn(Dist.CLIENT)
        public static record Realm(long realmId, int slotId) implements ReportEnvironment.Server {
            public Realm(RealmsServer param0) {
                this(param0.id, param0.activeSlot);
            }
        }

        @OnlyIn(Dist.CLIENT)
        public static record ThirdParty(String ip) implements ReportEnvironment.Server {
        }
    }
}
