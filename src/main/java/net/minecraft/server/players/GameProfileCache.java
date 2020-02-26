package net.minecraft.server.players;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.IOUtils;

public class GameProfileCache {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static boolean usesAuthentication;
    private final Map<String, GameProfileCache.GameProfileInfo> profilesByName = Maps.newHashMap();
    private final Map<UUID, GameProfileCache.GameProfileInfo> profilesByUUID = Maps.newHashMap();
    private final Deque<GameProfile> profileMRUList = Lists.newLinkedList();
    private final GameProfileRepository profileRepository;
    protected final Gson gson;
    private final File file;
    private static final TypeToken<List<GameProfileCache.GameProfileInfo>> GAMEPROFILE_ENTRY_TYPE = new TypeToken<List<GameProfileCache.GameProfileInfo>>() {
    };

    public GameProfileCache(GameProfileRepository param0, File param1) {
        this.profileRepository = param0;
        this.file = param1;
        GsonBuilder var0 = new GsonBuilder();
        var0.registerTypeHierarchyAdapter(GameProfileCache.GameProfileInfo.class, new GameProfileCache.Serializer());
        this.gson = var0.create();
        this.load();
    }

    private static GameProfile lookupGameProfile(GameProfileRepository param0, String param1) {
        final GameProfile[] var0 = new GameProfile[1];
        ProfileLookupCallback var1 = new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile param0) {
                var0[0] = param0;
            }

            @Override
            public void onProfileLookupFailed(GameProfile param0, Exception param1) {
                var0[0] = null;
            }
        };
        param0.findProfilesByNames(new String[]{param1}, Agent.MINECRAFT, var1);
        if (!usesAuthentication() && var0[0] == null) {
            UUID var2 = Player.createPlayerUUID(new GameProfile(null, param1));
            GameProfile var3 = new GameProfile(var2, param1);
            var1.onProfileLookupSucceeded(var3);
        }

        return var0[0];
    }

    public static void setUsesAuthentication(boolean param0) {
        usesAuthentication = param0;
    }

    private static boolean usesAuthentication() {
        return usesAuthentication;
    }

    public void add(GameProfile param0) {
        this.add(param0, null);
    }

    private void add(GameProfile param0, Date param1) {
        UUID var0 = param0.getId();
        if (param1 == null) {
            Calendar var1 = Calendar.getInstance();
            var1.setTime(new Date());
            var1.add(2, 1);
            param1 = var1.getTime();
        }

        GameProfileCache.GameProfileInfo var2 = new GameProfileCache.GameProfileInfo(param0, param1);
        if (this.profilesByUUID.containsKey(var0)) {
            GameProfileCache.GameProfileInfo var3 = this.profilesByUUID.get(var0);
            this.profilesByName.remove(var3.getProfile().getName().toLowerCase(Locale.ROOT));
            this.profileMRUList.remove(param0);
        }

        this.profilesByName.put(param0.getName().toLowerCase(Locale.ROOT), var2);
        this.profilesByUUID.put(var0, var2);
        this.profileMRUList.addFirst(param0);
        this.save();
    }

    @Nullable
    public GameProfile get(String param0) {
        String var0 = param0.toLowerCase(Locale.ROOT);
        GameProfileCache.GameProfileInfo var1 = this.profilesByName.get(var0);
        if (var1 != null && new Date().getTime() >= var1.expirationDate.getTime()) {
            this.profilesByUUID.remove(var1.getProfile().getId());
            this.profilesByName.remove(var1.getProfile().getName().toLowerCase(Locale.ROOT));
            this.profileMRUList.remove(var1.getProfile());
            var1 = null;
        }

        if (var1 != null) {
            GameProfile var2 = var1.getProfile();
            this.profileMRUList.remove(var2);
            this.profileMRUList.addFirst(var2);
        } else {
            GameProfile var3 = lookupGameProfile(this.profileRepository, var0);
            if (var3 != null) {
                this.add(var3);
                var1 = this.profilesByName.get(var0);
            }
        }

        this.save();
        return var1 == null ? null : var1.getProfile();
    }

    @Nullable
    public GameProfile get(UUID param0) {
        GameProfileCache.GameProfileInfo var0 = this.profilesByUUID.get(param0);
        return var0 == null ? null : var0.getProfile();
    }

    private GameProfileCache.GameProfileInfo getProfileInfo(UUID param0) {
        GameProfileCache.GameProfileInfo var0 = this.profilesByUUID.get(param0);
        if (var0 != null) {
            GameProfile var1 = var0.getProfile();
            this.profileMRUList.remove(var1);
            this.profileMRUList.addFirst(var1);
        }

        return var0;
    }

    public void load() {
        BufferedReader var0 = null;

        try {
            var0 = Files.newReader(this.file, StandardCharsets.UTF_8);
            List<GameProfileCache.GameProfileInfo> var1 = GsonHelper.fromJson(this.gson, var0, GAMEPROFILE_ENTRY_TYPE);
            this.profilesByName.clear();
            this.profilesByUUID.clear();
            this.profileMRUList.clear();
            if (var1 != null) {
                for(GameProfileCache.GameProfileInfo var2 : Lists.reverse(var1)) {
                    if (var2 != null) {
                        this.add(var2.getProfile(), var2.getExpirationDate());
                    }
                }
            }
        } catch (FileNotFoundException var9) {
        } catch (JsonParseException var10) {
        } finally {
            IOUtils.closeQuietly((Reader)var0);
        }

    }

    public void save() {
        String var0 = this.gson.toJson(this.getTopMRUProfiles(1000));
        BufferedWriter var1 = null;

        try {
            var1 = Files.newWriter(this.file, StandardCharsets.UTF_8);
            var1.write(var0);
            return;
        } catch (FileNotFoundException var8) {
        } catch (IOException var9) {
            return;
        } finally {
            IOUtils.closeQuietly((Writer)var1);
        }

    }

    private List<GameProfileCache.GameProfileInfo> getTopMRUProfiles(int param0) {
        List<GameProfileCache.GameProfileInfo> var0 = Lists.newArrayList();

        for(GameProfile var2 : Lists.newArrayList(Iterators.limit(this.profileMRUList.iterator(), param0))) {
            GameProfileCache.GameProfileInfo var3 = this.getProfileInfo(var2.getId());
            if (var3 != null) {
                var0.add(var3);
            }
        }

        return var0;
    }

    class GameProfileInfo {
        private final GameProfile profile;
        private final Date expirationDate;

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
    }

    class Serializer implements JsonDeserializer<GameProfileCache.GameProfileInfo>, JsonSerializer<GameProfileCache.GameProfileInfo> {
        private Serializer() {
        }

        public JsonElement serialize(GameProfileCache.GameProfileInfo param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            var0.addProperty("name", param0.getProfile().getName());
            UUID var1 = param0.getProfile().getId();
            var0.addProperty("uuid", var1 == null ? "" : var1.toString());
            var0.addProperty("expiresOn", GameProfileCache.DATE_FORMAT.format(param0.getExpirationDate()));
            return var0;
        }

        public GameProfileCache.GameProfileInfo deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
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
                            var6 = GameProfileCache.DATE_FORMAT.parse(var3.getAsString());
                        } catch (ParseException var14) {
                            var6 = null;
                        }
                    }

                    if (var5 != null && var4 != null) {
                        UUID var8;
                        try {
                            var8 = UUID.fromString(var4);
                        } catch (Throwable var13) {
                            return null;
                        }

                        return GameProfileCache.this.new GameProfileInfo(new GameProfile(var8, var5), var6);
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
    }
}
