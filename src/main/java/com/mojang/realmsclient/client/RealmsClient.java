package com.mojang.realmsclient.client;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.ServerActivityList;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.util.UndashedUuid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsClient {
    public static final RealmsClient.Environment ENVIRONMENT = Optional.ofNullable(System.getenv("realms.environment"))
        .or(() -> Optional.ofNullable(System.getProperty("realms.environment")))
        .flatMap(RealmsClient.Environment::byName)
        .orElse(RealmsClient.Environment.PRODUCTION);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String sessionId;
    private final String username;
    private final Minecraft minecraft;
    private static final String WORLDS_RESOURCE_PATH = "worlds";
    private static final String INVITES_RESOURCE_PATH = "invites";
    private static final String MCO_RESOURCE_PATH = "mco";
    private static final String SUBSCRIPTION_RESOURCE = "subscriptions";
    private static final String ACTIVITIES_RESOURCE = "activities";
    private static final String OPS_RESOURCE = "ops";
    private static final String REGIONS_RESOURCE = "regions/ping/stat";
    private static final String TRIALS_RESOURCE = "trial";
    private static final String NOTIFICATIONS_RESOURCE = "notifications";
    private static final String PATH_INITIALIZE = "/$WORLD_ID/initialize";
    private static final String PATH_GET_ACTIVTIES = "/$WORLD_ID";
    private static final String PATH_GET_LIVESTATS = "/liveplayerlist";
    private static final String PATH_GET_SUBSCRIPTION = "/$WORLD_ID";
    private static final String PATH_OP = "/$WORLD_ID/$PROFILE_UUID";
    private static final String PATH_PUT_INTO_MINIGAMES_MODE = "/minigames/$MINIGAME_ID/$WORLD_ID";
    private static final String PATH_AVAILABLE = "/available";
    private static final String PATH_TEMPLATES = "/templates/$WORLD_TYPE";
    private static final String PATH_WORLD_JOIN = "/v1/$ID/join/pc";
    private static final String PATH_WORLD_GET = "/$ID";
    private static final String PATH_WORLD_INVITES = "/$WORLD_ID";
    private static final String PATH_WORLD_UNINVITE = "/$WORLD_ID/invite/$UUID";
    private static final String PATH_PENDING_INVITES_COUNT = "/count/pending";
    private static final String PATH_PENDING_INVITES = "/pending";
    private static final String PATH_ACCEPT_INVITE = "/accept/$INVITATION_ID";
    private static final String PATH_REJECT_INVITE = "/reject/$INVITATION_ID";
    private static final String PATH_UNINVITE_MYSELF = "/$WORLD_ID";
    private static final String PATH_WORLD_UPDATE = "/$WORLD_ID";
    private static final String PATH_SLOT = "/$WORLD_ID/slot/$SLOT_ID";
    private static final String PATH_WORLD_OPEN = "/$WORLD_ID/open";
    private static final String PATH_WORLD_CLOSE = "/$WORLD_ID/close";
    private static final String PATH_WORLD_RESET = "/$WORLD_ID/reset";
    private static final String PATH_DELETE_WORLD = "/$WORLD_ID";
    private static final String PATH_WORLD_BACKUPS = "/$WORLD_ID/backups";
    private static final String PATH_WORLD_DOWNLOAD = "/$WORLD_ID/slot/$SLOT_ID/download";
    private static final String PATH_WORLD_UPLOAD = "/$WORLD_ID/backups/upload";
    private static final String PATH_CLIENT_COMPATIBLE = "/client/compatible";
    private static final String PATH_TOS_AGREED = "/tos/agreed";
    private static final String PATH_NEWS = "/v1/news";
    private static final String PATH_MARK_NOTIFICATIONS_SEEN = "/seen";
    private static final String PATH_DISMISS_NOTIFICATIONS = "/dismiss";
    private static final GuardedSerializer GSON = new GuardedSerializer();

    public static RealmsClient create() {
        Minecraft var0 = Minecraft.getInstance();
        return create(var0);
    }

    public static RealmsClient create(Minecraft param0) {
        String var0 = param0.getUser().getName();
        String var1 = param0.getUser().getSessionId();
        return new RealmsClient(var1, var0, param0);
    }

    public RealmsClient(String param0, String param1, Minecraft param2) {
        this.sessionId = param0;
        this.username = param1;
        this.minecraft = param2;
        RealmsClientConfig.setProxy(param2.getProxy());
    }

    public RealmsServerList listWorlds() throws RealmsServiceException {
        String var0 = this.url("worlds");
        String var1 = this.execute(Request.get(var0));
        return RealmsServerList.parse(var1);
    }

    public List<RealmsNotification> getNotifications() throws RealmsServiceException {
        String var0 = this.url("notifications");
        String var1 = this.execute(Request.get(var0));
        return RealmsNotification.parseList(var1);
    }

    private static JsonArray uuidListToJsonArray(List<UUID> param0) {
        JsonArray var0 = new JsonArray();

        for(UUID var1 : param0) {
            if (var1 != null) {
                var0.add(var1.toString());
            }
        }

        return var0;
    }

    public void notificationsSeen(List<UUID> param0) throws RealmsServiceException {
        String var0 = this.url("notifications/seen");
        this.execute(Request.post(var0, GSON.toJson(uuidListToJsonArray(param0))));
    }

    public void notificationsDismiss(List<UUID> param0) throws RealmsServiceException {
        String var0 = this.url("notifications/dismiss");
        this.execute(Request.post(var0, GSON.toJson(uuidListToJsonArray(param0))));
    }

    public RealmsServer getOwnWorld(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$ID".replace("$ID", String.valueOf(param0)));
        String var1 = this.execute(Request.get(var0));
        return RealmsServer.parse(var1);
    }

    public ServerActivityList getActivity(long param0) throws RealmsServiceException {
        String var0 = this.url("activities" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(param0)));
        String var1 = this.execute(Request.get(var0));
        return ServerActivityList.parse(var1);
    }

    public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
        String var0 = this.url("activities/liveplayerlist");
        String var1 = this.execute(Request.get(var0));
        return RealmsServerPlayerLists.parse(var1);
    }

    public RealmsServerAddress join(long param0) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/v1/$ID/join/pc".replace("$ID", param0 + ""));
        String var1 = this.execute(Request.get(var0, 5000, 30000));
        return RealmsServerAddress.parse(var1);
    }

    public void initializeWorld(long param0, String param1, String param2) throws RealmsServiceException {
        RealmsDescriptionDto var0 = new RealmsDescriptionDto(param1, param2);
        String var1 = this.url("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(param0)));
        String var2 = GSON.toJson(var0);
        this.execute(Request.post(var1, var2, 5000, 10000));
    }

    public boolean hasParentalConsent() throws RealmsServiceException {
        String var0 = this.url("mco/available");
        String var1 = this.execute(Request.get(var0));
        return Boolean.parseBoolean(var1);
    }

    public RealmsClient.CompatibleVersionResponse clientCompatible() throws RealmsServiceException {
        String var0 = this.url("mco/client/compatible");
        String var1 = this.execute(Request.get(var0));

        try {
            return RealmsClient.CompatibleVersionResponse.valueOf(var1);
        } catch (IllegalArgumentException var5) {
            throw new RealmsServiceException(RealmsError.CustomError.unknownCompatibilityResponse(var1));
        }
    }

    public void uninvite(long param0, UUID param1) throws RealmsServiceException {
        String var0 = this.url(
            "invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(param0)).replace("$UUID", UndashedUuid.toString(param1))
        );
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
            "worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", param2.toString()), String.format(Locale.ROOT, "page=%d&pageSize=%d", param0, param1)
        );
        String var1 = this.execute(Request.get(var0));
        return WorldTemplatePaginatedList.parse(var1);
    }

    public Boolean putIntoMinigameMode(long param0, String param1) throws RealmsServiceException {
        String var0 = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", param1).replace("$WORLD_ID", String.valueOf(param0));
        String var1 = this.url("worlds" + var0);
        return Boolean.valueOf(this.execute(Request.put(var1, "")));
    }

    public Ops op(long param0, UUID param1) throws RealmsServiceException {
        String var0 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(param0)).replace("$PROFILE_UUID", UndashedUuid.toString(param1));
        String var1 = this.url("ops" + var0);
        return Ops.parse(this.execute(Request.post(var1, "")));
    }

    public Ops deop(long param0, UUID param1) throws RealmsServiceException {
        String var0 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(param0)).replace("$PROFILE_UUID", UndashedUuid.toString(param1));
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

    public Boolean resetWorldWithSeed(long param0, WorldGenerationInfo param1) throws RealmsServiceException {
        RealmsWorldResetDto var0 = new RealmsWorldResetDto(param1.seed(), -1L, param1.levelType().getDtoIndex(), param1.generateStructures());
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
        return this.pendingInvites().pendingInvites.size();
    }

    public PendingInvitesList pendingInvites() throws RealmsServiceException {
        String var0 = this.url("invites/pending");
        String var1 = this.execute(Request.get(var0));
        PendingInvitesList var2 = PendingInvitesList.parse(var1);
        var2.pendingInvites.removeIf(this::isBlocked);
        return var2;
    }

    private boolean isBlocked(PendingInvite param0) {
        return this.minecraft.getPlayerSocialManager().isBlocked(param0.worldOwnerUuid);
    }

    public void acceptInvitation(String param0) throws RealmsServiceException {
        String var0 = this.url("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", param0));
        this.execute(Request.put(var0, ""));
    }

    public WorldDownload requestDownloadInfo(long param0, int param1) throws RealmsServiceException {
        String var0 = this.url(
            "worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(param0)).replace("$SLOT_ID", String.valueOf(param1))
        );
        String var1 = this.execute(Request.get(var0));
        return WorldDownload.parse(var1);
    }

    @Nullable
    public UploadInfo requestUploadInfo(long param0, @Nullable String param1) throws RealmsServiceException {
        String var0 = this.url("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(param0)));
        return UploadInfo.parse(this.execute(Request.put(var0, UploadInfo.createRequest(param1))));
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

    private String url(String param0) {
        return this.url(param0, null);
    }

    private String url(String param0, @Nullable String param1) {
        try {
            return new URI(ENVIRONMENT.protocol, ENVIRONMENT.baseUrl, "/" + param0, param1, null).toASCIIString();
        } catch (URISyntaxException var4) {
            throw new IllegalArgumentException(param0, var4);
        }
    }

    private String execute(Request<?> param0) throws RealmsServiceException {
        param0.cookie("sid", this.sessionId);
        param0.cookie("user", this.username);
        param0.cookie("version", SharedConstants.getCurrentVersion().getName());

        try {
            int var0 = param0.responseCode();
            if (var0 != 503 && var0 != 277) {
                String var2 = param0.text();
                if (var0 >= 200 && var0 < 300) {
                    return var2;
                } else if (var0 == 401) {
                    String var3 = param0.getHeader("WWW-Authenticate");
                    LOGGER.info("Could not authorize you against Realms server: {}", var3);
                    throw new RealmsServiceException(new RealmsError.AuthenticationError(var3));
                } else {
                    RealmsError var4 = RealmsError.parse(var0, var2);
                    throw new RealmsServiceException(var4);
                }
            } else {
                int var1 = param0.getRetryAfterHeader();
                throw new RetryCallException(var1, var0);
            }
        } catch (RealmsHttpException var51) {
            throw new RealmsServiceException(RealmsError.CustomError.connectivityError(var51));
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

        public final String baseUrl;
        public final String protocol;

        private Environment(String param0, String param1) {
            this.baseUrl = param0;
            this.protocol = param1;
        }

        public static Optional<RealmsClient.Environment> byName(String param0) {
            String var1 = param0.toLowerCase(Locale.ROOT);

            return switch(var1) {
                case "production" -> Optional.of(PRODUCTION);
                case "local" -> Optional.of(LOCAL);
                case "stage", "staging" -> Optional.of(STAGE);
                default -> Optional.empty();
            };
        }
    }
}
