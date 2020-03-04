package com.mojang.realmsclient.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsClient {
    public static RealmsClient.Environment currentEnvironment = RealmsClient.Environment.PRODUCTION;
    private static boolean initialized;
    private static final Logger LOGGER = LogManager.getLogger();
    private final String sessionId;
    private final String username;
    private static final GuardedSerializer GSON = new GuardedSerializer();

    public static RealmsClient create() {
        Minecraft var0 = Minecraft.getInstance();
        String var1 = var0.getUser().getName();
        String var2 = var0.getUser().getSessionId();
        if (!initialized) {
            initialized = true;
            String var3 = System.getenv("realms.environment");
            if (var3 == null) {
                var3 = System.getProperty("realms.environment");
            }

            if (var3 != null) {
                if ("LOCAL".equals(var3)) {
                    switchToLocal();
                } else if ("STAGE".equals(var3)) {
                    switchToStage();
                }
            }
        }

        return new RealmsClient(var2, var1, var0.getProxy());
    }

    public static void switchToStage() {
        currentEnvironment = RealmsClient.Environment.STAGE;
    }

    public static void switchToProd() {
        currentEnvironment = RealmsClient.Environment.PRODUCTION;
    }

    public static void switchToLocal() {
        currentEnvironment = RealmsClient.Environment.LOCAL;
    }

    public RealmsClient(String param0, String param1, Proxy param2) {
        this.sessionId = param0;
        this.username = param1;
        RealmsClientConfig.setProxy(param2);
    }

    public RealmsServerList listWorlds() throws RealmsServiceException {
        String var0 = this.url("worlds");
        String var1 = this.execute(Request.get(var0));
        return RealmsServerList.parse(var1);
    }

    public RealmsServer getOwnWorld(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$ID".replace("$ID", String.valueOf(param0)));
        String var1 = this.execute(Request.get(var0));
        return RealmsServer.parse(var1);
    }

    public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
        String var0 = this.url("activities/liveplayerlist");
        String var1 = this.execute(Request.get(var0));
        return RealmsServerPlayerLists.parse(var1);
    }

    public RealmsServerAddress join(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/v1/$ID/join/pc".replace("$ID", "" + param0));
        String var1 = this.execute(Request.get(var0, 5000, 30000));
        return RealmsServerAddress.parse(var1);
    }

    public void initializeWorld(long param0, String param1, String param2) throws RealmsServiceException {
        RealmsDescriptionDto var0 = new RealmsDescriptionDto(param1, param2);
        String var1 = this.url("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(param0)));
        String var2 = GSON.toJson(var0);
        this.execute(Request.post(var1, var2, 5000, 10000));
    }

    public Boolean mcoEnabled() throws RealmsServiceException {
        String var0 = this.url("mco/available");
        String var1 = this.execute(Request.get(var0));
        return Boolean.valueOf(var1);
    }

    public Boolean stageAvailable() throws RealmsServiceException {
        String var0 = this.url("mco/stageAvailable");
        String var1 = this.execute(Request.get(var0));
        return Boolean.valueOf(var1);
    }

    public RealmsClient.CompatibleVersionResponse clientCompatible() throws RealmsServiceException {
        String var0 = this.url("mco/client/compatible");
        String var1 = this.execute(Request.get(var0));

        try {
            return RealmsClient.CompatibleVersionResponse.valueOf(var1);
        } catch (IllegalArgumentException var5) {
            throw new RealmsServiceException(500, "Could not check compatible version, got response: " + var1, -1, "");
        }
    }

    public void uninvite(long param0, String param1) throws RealmsServiceException {
        String var0 = this.url("invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(param0)).replace("$UUID", param1));
        this.execute(Request.delete(var0));
    }

    public void uninviteMyselfFrom(long param0) throws RealmsServiceException {
        String var0 = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(param0)));
        this.execute(Request.delete(var0));
    }

    public RealmsServer invite(long param0, String param1) throws RealmsServiceException {
        PlayerInfo var0 = new PlayerInfo();
        var0.setName(param1);
        String var1 = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(param0)));
        String var2 = this.execute(Request.post(var1, GSON.toJson(var0)));
        return RealmsServer.parse(var2);
    }

    public BackupList backupsFor(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(param0)));
        String var1 = this.execute(Request.get(var0));
        return BackupList.parse(var1);
    }

    public void update(long param0, String param1, String param2) throws RealmsServiceException {
        RealmsDescriptionDto var0 = new RealmsDescriptionDto(param1, param2);
        String var1 = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(param0)));
        this.execute(Request.post(var1, GSON.toJson(var0)));
    }

    public void updateSlot(long param0, int param1, RealmsWorldOptions param2) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(param0)).replace("$SLOT_ID", String.valueOf(param1)));
        String var1 = param2.toJson();
        this.execute(Request.post(var0, var1));
    }

    public boolean switchSlot(long param0, int param1) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(param0)).replace("$SLOT_ID", String.valueOf(param1)));
        String var1 = this.execute(Request.put(var0, ""));
        return Boolean.valueOf(var1);
    }

    public void restoreWorld(long param0, String param1) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(param0)), "backupId=" + param1);
        this.execute(Request.put(var0, "", 40000, 600000));
    }

    public WorldTemplatePaginatedList fetchWorldTemplates(int param0, int param1, RealmsServer.WorldType param2) throws RealmsServiceException {
        String var0 = this.url(
            "worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", param2.toString()), String.format("page=%d&pageSize=%d", param0, param1)
        );
        String var1 = this.execute(Request.get(var0));
        return WorldTemplatePaginatedList.parse(var1);
    }

    public Boolean putIntoMinigameMode(long param0, String param1) throws RealmsServiceException {
        String var0 = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", param1).replace("$WORLD_ID", String.valueOf(param0));
        String var1 = this.url("worlds" + var0);
        return Boolean.valueOf(this.execute(Request.put(var1, "")));
    }

    public Ops op(long param0, String param1) throws RealmsServiceException {
        String var0 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(param0)).replace("$PROFILE_UUID", param1);
        String var1 = this.url("ops" + var0);
        return Ops.parse(this.execute(Request.post(var1, "")));
    }

    public Ops deop(long param0, String param1) throws RealmsServiceException {
        String var0 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(param0)).replace("$PROFILE_UUID", param1);
        String var1 = this.url("ops" + var0);
        return Ops.parse(this.execute(Request.delete(var1)));
    }

    public Boolean open(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/open".replace("$WORLD_ID", String.valueOf(param0)));
        String var1 = this.execute(Request.put(var0, ""));
        return Boolean.valueOf(var1);
    }

    public Boolean close(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/close".replace("$WORLD_ID", String.valueOf(param0)));
        String var1 = this.execute(Request.put(var0, ""));
        return Boolean.valueOf(var1);
    }

    public Boolean resetWorldWithSeed(long param0, String param1, Integer param2, boolean param3) throws RealmsServiceException {
        RealmsWorldResetDto var0 = new RealmsWorldResetDto(param1, -1L, param2, param3);
        String var1 = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(param0)));
        String var2 = this.execute(Request.post(var1, GSON.toJson(var0), 30000, 80000));
        return Boolean.valueOf(var2);
    }

    public Boolean resetWorldWithTemplate(long param0, String param1) throws RealmsServiceException {
        RealmsWorldResetDto var0 = new RealmsWorldResetDto(null, Long.valueOf(param1), -1, false);
        String var1 = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(param0)));
        String var2 = this.execute(Request.post(var1, GSON.toJson(var0), 30000, 80000));
        return Boolean.valueOf(var2);
    }

    public Subscription subscriptionFor(long param0) throws RealmsServiceException {
        String var0 = this.url("subscriptions" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(param0)));
        String var1 = this.execute(Request.get(var0));
        return Subscription.parse(var1);
    }

    public int pendingInvitesCount() throws RealmsServiceException {
        String var0 = this.url("invites/count/pending");
        String var1 = this.execute(Request.get(var0));
        return Integer.parseInt(var1);
    }

    public PendingInvitesList pendingInvites() throws RealmsServiceException {
        String var0 = this.url("invites/pending");
        String var1 = this.execute(Request.get(var0));
        return PendingInvitesList.parse(var1);
    }

    public void acceptInvitation(String param0) throws RealmsServiceException {
        String var0 = this.url("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", param0));
        this.execute(Request.put(var0, ""));
    }

    public WorldDownload download(long param0, int param1) throws RealmsServiceException {
        String var0 = this.url(
            "worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(param0)).replace("$SLOT_ID", String.valueOf(param1))
        );
        String var1 = this.execute(Request.get(var0));
        return WorldDownload.parse(var1);
    }

    public UploadInfo upload(long param0, String param1) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(param0)));
        UploadInfo var1 = new UploadInfo();
        if (param1 != null) {
            var1.setToken(param1);
        }

        GsonBuilder var2 = new GsonBuilder();
        var2.excludeFieldsWithoutExposeAnnotation();
        Gson var3 = var2.create();
        String var4 = var3.toJson(var1);
        return UploadInfo.parse(this.execute(Request.put(var0, var4)));
    }

    public void rejectInvitation(String param0) throws RealmsServiceException {
        String var0 = this.url("invites" + "/reject/$INVITATION_ID".replace("$INVITATION_ID", param0));
        this.execute(Request.put(var0, ""));
    }

    public void agreeToTos() throws RealmsServiceException {
        String var0 = this.url("mco/tos/agreed");
        this.execute(Request.post(var0, ""));
    }

    public RealmsNews getNews() throws RealmsServiceException {
        String var0 = this.url("mco/v1/news");
        String var1 = this.execute(Request.get(var0, 5000, 10000));
        return RealmsNews.parse(var1);
    }

    public void sendPingResults(PingResult param0) throws RealmsServiceException {
        String var0 = this.url("regions/ping/stat");
        this.execute(Request.post(var0, GSON.toJson(param0)));
    }

    public Boolean trialAvailable() throws RealmsServiceException {
        String var0 = this.url("trial");
        String var1 = this.execute(Request.get(var0));
        return Boolean.valueOf(var1);
    }

    public void deleteWorld(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(param0)));
        this.execute(Request.delete(var0));
    }

    @Nullable
    private String url(String param0) {
        return this.url(param0, null);
    }

    @Nullable
    private String url(String param0, @Nullable String param1) {
        try {
            return new URI(currentEnvironment.protocol, currentEnvironment.baseUrl, "/" + param0, param1, null).toASCIIString();
        } catch (URISyntaxException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    private String execute(Request<?> param0) throws RealmsServiceException {
        param0.cookie("sid", this.sessionId);
        param0.cookie("user", this.username);
        param0.cookie("version", SharedConstants.getCurrentVersion().getName());

        try {
            int var0 = param0.responseCode();
            if (var0 == 503) {
                int var1 = param0.getRetryAfterHeader();
                throw new RetryCallException(var1);
            } else {
                String var2 = param0.text();
                if (var0 >= 200 && var0 < 300) {
                    return var2;
                } else if (var0 == 401) {
                    String var3 = param0.getHeader("WWW-Authenticate");
                    LOGGER.info("Could not authorize you against Realms server: " + var3);
                    throw new RealmsServiceException(var0, var3, -1, var3);
                } else if (var2 != null && var2.length() != 0) {
                    RealmsError var4 = new RealmsError(var2);
                    LOGGER.error(
                        "Realms http code: "
                            + var0
                            + " -  error code: "
                            + var4.getErrorCode()
                            + " -  message: "
                            + var4.getErrorMessage()
                            + " - raw body: "
                            + var2
                    );
                    throw new RealmsServiceException(var0, var2, var4);
                } else {
                    LOGGER.error("Realms error code: " + var0 + " message: " + var2);
                    throw new RealmsServiceException(var0, var2, var0, "");
                }
            }
        } catch (RealmsHttpException var51) {
            throw new RealmsServiceException(500, "Could not connect to Realms: " + var51.getMessage(), -1, "");
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum CompatibleVersionResponse {
        COMPATIBLE,
        OUTDATED,
        OTHER;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Environment {
        PRODUCTION("pc.realms.minecraft.net", "https"),
        STAGE("pc-stage.realms.minecraft.net", "https"),
        LOCAL("localhost:8080", "http");

        public String baseUrl;
        public String protocol;

        private Environment(String param0, String param1) {
            this.baseUrl = param0;
            this.protocol = param1;
        }
    }
}
