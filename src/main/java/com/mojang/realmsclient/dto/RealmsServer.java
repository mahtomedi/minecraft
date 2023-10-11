package com.mojang.realmsclient.dto;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServer extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_VALUE = -1;
    public long id;
    public String remoteSubscriptionId;
    public String name;
    public String motd;
    public RealmsServer.State state;
    public String owner;
    public UUID ownerUUID = Util.NIL_UUID;
    public List<PlayerInfo> players;
    public Map<Integer, RealmsWorldOptions> slots;
    public boolean expired;
    public boolean expiredTrial;
    public int daysLeft;
    public RealmsServer.WorldType worldType;
    public int activeSlot;
    public String minigameName;
    public int minigameId;
    public String minigameImage;
    public long parentWorldId = -1L;
    @Nullable
    public String parentWorldName;
    public String activeVersion = "";
    public RealmsServer.Compatibility compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
    public RealmsServerPing serverPing = new RealmsServerPing();

    public String getDescription() {
        return this.motd;
    }

    public String getName() {
        return this.name;
    }

    public String getMinigameName() {
        return this.minigameName;
    }

    public void setName(String param0) {
        this.name = param0;
    }

    public void setDescription(String param0) {
        this.motd = param0;
    }

    public void updateServerPing(RealmsServerPlayerList param0) {
        List<String> var0 = Lists.newArrayList();
        int var1 = 0;
        MinecraftSessionService var2 = Minecraft.getInstance().getMinecraftSessionService();

        for(UUID var3 : param0.players) {
            if (!Minecraft.getInstance().isLocalPlayer(var3)) {
                try {
                    ProfileResult var4 = var2.fetchProfile(var3, false);
                    if (var4 != null) {
                        var0.add(var4.profile().getName());
                    }

                    ++var1;
                } catch (Exception var8) {
                    LOGGER.error("Could not get name for {}", var3, var8);
                }
            }
        }

        this.serverPing.nrOfPlayers = String.valueOf(var1);
        this.serverPing.playerList = Joiner.on('\n').join(var0);
    }

    public static RealmsServer parse(JsonObject param0) {
        RealmsServer var0 = new RealmsServer();

        try {
            var0.id = JsonUtils.getLongOr("id", param0, -1L);
            var0.remoteSubscriptionId = JsonUtils.getStringOr("remoteSubscriptionId", param0, null);
            var0.name = JsonUtils.getStringOr("name", param0, null);
            var0.motd = JsonUtils.getStringOr("motd", param0, null);
            var0.state = getState(JsonUtils.getStringOr("state", param0, RealmsServer.State.CLOSED.name()));
            var0.owner = JsonUtils.getStringOr("owner", param0, null);
            if (param0.get("players") != null && param0.get("players").isJsonArray()) {
                var0.players = parseInvited(param0.get("players").getAsJsonArray());
                sortInvited(var0);
            } else {
                var0.players = Lists.newArrayList();
            }

            var0.daysLeft = JsonUtils.getIntOr("daysLeft", param0, 0);
            var0.expired = JsonUtils.getBooleanOr("expired", param0, false);
            var0.expiredTrial = JsonUtils.getBooleanOr("expiredTrial", param0, false);
            var0.worldType = getWorldType(JsonUtils.getStringOr("worldType", param0, RealmsServer.WorldType.NORMAL.name()));
            var0.ownerUUID = JsonUtils.getUuidOr("ownerUUID", param0, Util.NIL_UUID);
            if (param0.get("slots") != null && param0.get("slots").isJsonArray()) {
                var0.slots = parseSlots(param0.get("slots").getAsJsonArray());
            } else {
                var0.slots = createEmptySlots();
            }

            var0.minigameName = JsonUtils.getStringOr("minigameName", param0, null);
            var0.activeSlot = JsonUtils.getIntOr("activeSlot", param0, -1);
            var0.minigameId = JsonUtils.getIntOr("minigameId", param0, -1);
            var0.minigameImage = JsonUtils.getStringOr("minigameImage", param0, null);
            var0.parentWorldId = JsonUtils.getLongOr("parentWorldId", param0, -1L);
            var0.parentWorldName = JsonUtils.getStringOr("parentWorldName", param0, null);
            var0.activeVersion = JsonUtils.getStringOr("activeVersion", param0, "");
            var0.compatibility = getCompatibility(JsonUtils.getStringOr("compatibility", param0, RealmsServer.Compatibility.UNVERIFIABLE.name()));
        } catch (Exception var3) {
            LOGGER.error("Could not parse McoServer: {}", var3.getMessage());
        }

        return var0;
    }

    private static void sortInvited(RealmsServer param0) {
        param0.players
            .sort(
                (param0x, param1) -> ComparisonChain.start()
                        .compareFalseFirst(param1.getAccepted(), param0x.getAccepted())
                        .compare(param0x.getName().toLowerCase(Locale.ROOT), param1.getName().toLowerCase(Locale.ROOT))
                        .result()
            );
    }

    private static List<PlayerInfo> parseInvited(JsonArray param0) {
        List<PlayerInfo> var0 = Lists.newArrayList();

        for(JsonElement var1 : param0) {
            try {
                JsonObject var2 = var1.getAsJsonObject();
                PlayerInfo var3 = new PlayerInfo();
                var3.setName(JsonUtils.getStringOr("name", var2, null));
                var3.setUuid(JsonUtils.getUuidOr("uuid", var2, Util.NIL_UUID));
                var3.setOperator(JsonUtils.getBooleanOr("operator", var2, false));
                var3.setAccepted(JsonUtils.getBooleanOr("accepted", var2, false));
                var3.setOnline(JsonUtils.getBooleanOr("online", var2, false));
                var0.add(var3);
            } catch (Exception var6) {
            }
        }

        return var0;
    }

    private static Map<Integer, RealmsWorldOptions> parseSlots(JsonArray param0) {
        Map<Integer, RealmsWorldOptions> var0 = Maps.newHashMap();

        for(JsonElement var1 : param0) {
            try {
                JsonObject var2 = var1.getAsJsonObject();
                JsonParser var3 = new JsonParser();
                JsonElement var4 = var3.parse(var2.get("options").getAsString());
                RealmsWorldOptions var5;
                if (var4 == null) {
                    var5 = RealmsWorldOptions.createDefaults();
                } else {
                    var5 = RealmsWorldOptions.parse(var4.getAsJsonObject());
                }

                int var7 = JsonUtils.getIntOr("slotId", var2, -1);
                var0.put(var7, var5);
            } catch (Exception var9) {
            }
        }

        for(int var8 = 1; var8 <= 3; ++var8) {
            if (!var0.containsKey(var8)) {
                var0.put(var8, RealmsWorldOptions.createEmptyDefaults());
            }
        }

        return var0;
    }

    private static Map<Integer, RealmsWorldOptions> createEmptySlots() {
        Map<Integer, RealmsWorldOptions> var0 = Maps.newHashMap();
        var0.put(1, RealmsWorldOptions.createEmptyDefaults());
        var0.put(2, RealmsWorldOptions.createEmptyDefaults());
        var0.put(3, RealmsWorldOptions.createEmptyDefaults());
        return var0;
    }

    public static RealmsServer parse(String param0) {
        try {
            return parse(new JsonParser().parse(param0).getAsJsonObject());
        } catch (Exception var2) {
            LOGGER.error("Could not parse McoServer: {}", var2.getMessage());
            return new RealmsServer();
        }
    }

    private static RealmsServer.State getState(String param0) {
        try {
            return RealmsServer.State.valueOf(param0);
        } catch (Exception var2) {
            return RealmsServer.State.CLOSED;
        }
    }

    private static RealmsServer.WorldType getWorldType(String param0) {
        try {
            return RealmsServer.WorldType.valueOf(param0);
        } catch (Exception var2) {
            return RealmsServer.WorldType.NORMAL;
        }
    }

    private static RealmsServer.Compatibility getCompatibility(@Nullable String param0) {
        try {
            return RealmsServer.Compatibility.valueOf(param0);
        } catch (Exception var2) {
            return RealmsServer.Compatibility.UNVERIFIABLE;
        }
    }

    public boolean isCompatible() {
        return this.compatibility == RealmsServer.Compatibility.COMPATIBLE;
    }

    public boolean needsUpgrade() {
        return this.compatibility == RealmsServer.Compatibility.NEEDS_UPGRADE;
    }

    public boolean needsDowngrade() {
        return this.compatibility == RealmsServer.Compatibility.NEEDS_DOWNGRADE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.motd, this.state, this.owner, this.expired);
    }

    @Override
    public boolean equals(Object param0) {
        if (param0 == null) {
            return false;
        } else if (param0 == this) {
            return true;
        } else if (param0.getClass() != this.getClass()) {
            return false;
        } else {
            RealmsServer var0 = (RealmsServer)param0;
            return new EqualsBuilder()
                .append(this.id, var0.id)
                .append(this.name, var0.name)
                .append(this.motd, var0.motd)
                .append(this.state, var0.state)
                .append(this.owner, var0.owner)
                .append(this.expired, var0.expired)
                .append(this.worldType, this.worldType)
                .isEquals();
        }
    }

    public RealmsServer clone() {
        RealmsServer var0 = new RealmsServer();
        var0.id = this.id;
        var0.remoteSubscriptionId = this.remoteSubscriptionId;
        var0.name = this.name;
        var0.motd = this.motd;
        var0.state = this.state;
        var0.owner = this.owner;
        var0.players = this.players;
        var0.slots = this.cloneSlots(this.slots);
        var0.expired = this.expired;
        var0.expiredTrial = this.expiredTrial;
        var0.daysLeft = this.daysLeft;
        var0.serverPing = new RealmsServerPing();
        var0.serverPing.nrOfPlayers = this.serverPing.nrOfPlayers;
        var0.serverPing.playerList = this.serverPing.playerList;
        var0.worldType = this.worldType;
        var0.ownerUUID = this.ownerUUID;
        var0.minigameName = this.minigameName;
        var0.activeSlot = this.activeSlot;
        var0.minigameId = this.minigameId;
        var0.minigameImage = this.minigameImage;
        var0.parentWorldName = this.parentWorldName;
        var0.parentWorldId = this.parentWorldId;
        var0.activeVersion = this.activeVersion;
        var0.compatibility = this.compatibility;
        return var0;
    }

    public Map<Integer, RealmsWorldOptions> cloneSlots(Map<Integer, RealmsWorldOptions> param0) {
        Map<Integer, RealmsWorldOptions> var0 = Maps.newHashMap();

        for(Entry<Integer, RealmsWorldOptions> var1 : param0.entrySet()) {
            var0.put(var1.getKey(), var1.getValue().clone());
        }

        return var0;
    }

    public boolean isSnapshotRealm() {
        return this.parentWorldId != -1L;
    }

    public String getWorldName(int param0) {
        return this.name + " (" + this.slots.get(param0).getSlotName(param0) + ")";
    }

    public ServerData toServerData(String param0) {
        return new ServerData(this.name, param0, ServerData.Type.REALM);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Compatibility {
        UNVERIFIABLE,
        INCOMPATIBLE,
        NEEDS_DOWNGRADE,
        NEEDS_UPGRADE,
        COMPATIBLE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class McoServerComparator implements Comparator<RealmsServer> {
        private final String refOwner;

        public McoServerComparator(String param0) {
            this.refOwner = param0;
        }

        public int compare(RealmsServer param0, RealmsServer param1) {
            return ComparisonChain.start()
                .compareTrueFirst(param0.isSnapshotRealm(), param1.isSnapshotRealm())
                .compareTrueFirst(param0.state == RealmsServer.State.UNINITIALIZED, param1.state == RealmsServer.State.UNINITIALIZED)
                .compareTrueFirst(param0.expiredTrial, param1.expiredTrial)
                .compareTrueFirst(param0.owner.equals(this.refOwner), param1.owner.equals(this.refOwner))
                .compareFalseFirst(param0.expired, param1.expired)
                .compareTrueFirst(param0.state == RealmsServer.State.OPEN, param1.state == RealmsServer.State.OPEN)
                .compare(param0.id, param1.id)
                .result();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum State {
        CLOSED,
        OPEN,
        UNINITIALIZED;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum WorldType {
        NORMAL,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;
    }
}
