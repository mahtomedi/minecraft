package net.minecraft.server.players;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;

public class GameProfileCache {
    private static boolean usesAuthentication;
    private final Map<String, GameProfileCache.GameProfileInfo> profilesByName = Maps.newConcurrentMap();
    private final Map<UUID, GameProfileCache.GameProfileInfo> profilesByUUID = Maps.newConcurrentMap();
    private final GameProfileRepository profileRepository;
    private final Gson gson = new GsonBuilder().create();
    private final File file;
    private final AtomicLong operationCount = new AtomicLong();

    public GameProfileCache(GameProfileRepository param0, File param1) {
        this.profileRepository = param0;
        this.file = param1;
        Lists.reverse(this.load()).forEach(this::safeAdd);
    }

    private void safeAdd(GameProfileCache.GameProfileInfo param0x) {
        GameProfile var0 = param0x.getProfile();
        param0x.setLastAccess(this.getNextOperation());
        String var1 = var0.getName();
        if (var1 != null) {
            this.profilesByName.put(var1.toLowerCase(Locale.ROOT), param0x);
        }

        UUID var2 = var0.getId();
        if (var2 != null) {
            this.profilesByUUID.put(var2, param0x);
        }

    }

    @Nullable
    private static GameProfile lookupGameProfile(GameProfileRepository param0, String param1) {
        final AtomicReference<GameProfile> var0 = new AtomicReference<>();
        ProfileLookupCallback var1 = new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile param0) {
                var0.set(param0);
            }

            @Override
            public void onProfileLookupFailed(GameProfile param0, Exception param1) {
                var0.set(null);
            }
        };
        param0.findProfilesByNames(new String[]{param1}, Agent.MINECRAFT, var1);
        GameProfile var2 = var0.get();
        if (!usesAuthentication() && var2 == null) {
            UUID var3 = Player.createPlayerUUID(new GameProfile(null, param1));
            var2 = new GameProfile(var3, param1);
        }

        return var2;
    }

    public static void setUsesAuthentication(boolean param0) {
        usesAuthentication = param0;
    }

    private static boolean usesAuthentication() {
        return usesAuthentication;
    }

    public void add(GameProfile param0) {
        Calendar var0 = Calendar.getInstance();
        var0.setTime(new Date());
        var0.add(2, 1);
        Date var1 = var0.getTime();
        GameProfileCache.GameProfileInfo var2 = new GameProfileCache.GameProfileInfo(param0, var1);
        this.safeAdd(var2);
        this.save();
    }

    private long getNextOperation() {
        return this.operationCount.incrementAndGet();
    }

    @Nullable
    public GameProfile get(String param0) {
        String var0 = param0.toLowerCase(Locale.ROOT);
        GameProfileCache.GameProfileInfo var1 = this.profilesByName.get(var0);
        boolean var2 = false;
        if (var1 != null && new Date().getTime() >= var1.expirationDate.getTime()) {
            this.profilesByUUID.remove(var1.getProfile().getId());
            this.profilesByName.remove(var1.getProfile().getName().toLowerCase(Locale.ROOT));
            var2 = true;
            var1 = null;
        }

        GameProfile var3;
        if (var1 != null) {
            var1.setLastAccess(this.getNextOperation());
            var3 = var1.getProfile();
        } else {
            var3 = lookupGameProfile(this.profileRepository, var0);
            if (var3 != null) {
                this.add(var3);
                var2 = false;
            }
        }

        if (var2) {
            this.save();
        }

        return var3;
    }

    @Nullable
    public GameProfile get(UUID param0) {
        GameProfileCache.GameProfileInfo var0 = this.profilesByUUID.get(param0);
        if (var0 == null) {
            return null;
        } else {
            var0.setLastAccess(this.getNextOperation());
            return var0.getProfile();
        }
    }

    private static DateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    }

    public List<GameProfileCache.GameProfileInfo> load() {
        List<GameProfileCache.GameProfileInfo> var0 = Lists.newArrayList();

        try (Reader var1 = Files.newReader(this.file, StandardCharsets.UTF_8)) {
            JsonArray var2 = this.gson.fromJson(var1, JsonArray.class);
            DateFormat var3 = createDateFormat();
            var2.forEach(param2 -> {
                GameProfileCache.GameProfileInfo var0x = readGameProfile(param2, var3);
                if (var0x != null) {
                    var0.add(var0x);
                }

            });
        } catch (JsonParseException | IOException var16) {
        }

        return var0;
    }

    public void save() {
        JsonArray var0 = new JsonArray();
        DateFormat var1 = createDateFormat();
        this.getTopMRUProfiles(1000).forEach(param2 -> var0.add(writeGameProfile(param2, var1)));
        String var2 = this.gson.toJson((JsonElement)var0);

        try (Writer var3 = Files.newWriter(this.file, StandardCharsets.UTF_8)) {
            var3.write(var2);
        } catch (IOException var17) {
        }

    }

    private Stream<GameProfileCache.GameProfileInfo> getTopMRUProfiles(int param0) {
        return ImmutableList.copyOf(this.profilesByUUID.values())
            .stream()
            .sorted(Comparator.comparing(GameProfileCache.GameProfileInfo::getLastAccess).reversed())
            .limit((long)param0);
    }

    private static JsonElement writeGameProfile(GameProfileCache.GameProfileInfo param0, DateFormat param1) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("name", param0.getProfile().getName());
        UUID var1 = param0.getProfile().getId();
        var0.addProperty("uuid", var1 == null ? "" : var1.toString());
        var0.addProperty("expiresOn", param1.format(param0.getExpirationDate()));
        return var0;
    }

    @Nullable
    private static GameProfileCache.GameProfileInfo readGameProfile(JsonElement param0, DateFormat param1) {
        if (param0.isJsonObject()) {
            JsonObject var0 = param0.getAsJsonObject();
            JsonElement var1 = var0.get("name");
            JsonElement var2 = var0.get("uuid");
            JsonElement var3 = var0.get("expiresOn");
            if (var1 != null && var2 != null) {
                String var4 = var2.getAsString();
                String var5 = var1.getAsString();
                Date var6 = null;
                if (var3 != null) {
                    try {
                        var6 = param1.parse(var3.getAsString());
                    } catch (ParseException var12) {
                    }
                }

                if (var5 != null && var4 != null && var6 != null) {
                    UUID var7;
                    try {
                        var7 = UUID.fromString(var4);
                    } catch (Throwable var11) {
                        return null;
                    }

                    return new GameProfileCache.GameProfileInfo(new GameProfile(var7, var5), var6);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static class GameProfileInfo {
        private final GameProfile profile;
        private final Date expirationDate;
        private volatile long lastAccess;

        private GameProfileInfo(GameProfile param0, Date param1) {
            this.profile = param0;
            this.expirationDate = param1;
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }

        public void setLastAccess(long param0) {
            this.lastAccess = param0;
        }

        public long getLastAccess() {
            return this.lastAccess;
        }
    }
}
