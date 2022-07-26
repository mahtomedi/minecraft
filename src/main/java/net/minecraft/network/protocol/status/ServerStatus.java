package net.minecraft.network.protocol.status;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Type;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public class ServerStatus {
    public static final int FAVICON_WIDTH = 64;
    public static final int FAVICON_HEIGHT = 64;
    @Nullable
    private Component description;
    @Nullable
    private ServerStatus.Players players;
    @Nullable
    private ServerStatus.Version version;
    @Nullable
    private String favicon;
    private boolean enforcesSecureChat;

    @Nullable
    public Component getDescription() {
        return this.description;
    }

    public void setDescription(Component param0) {
        this.description = param0;
    }

    @Nullable
    public ServerStatus.Players getPlayers() {
        return this.players;
    }

    public void setPlayers(ServerStatus.Players param0) {
        this.players = param0;
    }

    @Nullable
    public ServerStatus.Version getVersion() {
        return this.version;
    }

    public void setVersion(ServerStatus.Version param0) {
        this.version = param0;
    }

    public void setFavicon(String param0) {
        this.favicon = param0;
    }

    @Nullable
    public String getFavicon() {
        return this.favicon;
    }

    public void setEnforcesSecureChat(boolean param0) {
        this.enforcesSecureChat = param0;
    }

    public boolean enforcesSecureChat() {
        return this.enforcesSecureChat;
    }

    public static class Players {
        private final int maxPlayers;
        private final int numPlayers;
        @Nullable
        private GameProfile[] sample;

        public Players(int param0, int param1) {
            this.maxPlayers = param0;
            this.numPlayers = param1;
        }

        public int getMaxPlayers() {
            return this.maxPlayers;
        }

        public int getNumPlayers() {
            return this.numPlayers;
        }

        @Nullable
        public GameProfile[] getSample() {
            return this.sample;
        }

        public void setSample(GameProfile[] param0) {
            this.sample = param0;
        }

        public static class Serializer implements JsonDeserializer<ServerStatus.Players>, JsonSerializer<ServerStatus.Players> {
            public ServerStatus.Players deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
                JsonObject var0 = GsonHelper.convertToJsonObject(param0, "players");
                ServerStatus.Players var1 = new ServerStatus.Players(GsonHelper.getAsInt(var0, "max"), GsonHelper.getAsInt(var0, "online"));
                if (GsonHelper.isArrayNode(var0, "sample")) {
                    JsonArray var2 = GsonHelper.getAsJsonArray(var0, "sample");
                    if (var2.size() > 0) {
                        GameProfile[] var3 = new GameProfile[var2.size()];

                        for(int var4 = 0; var4 < var3.length; ++var4) {
                            JsonObject var5 = GsonHelper.convertToJsonObject(var2.get(var4), "player[" + var4 + "]");
                            String var6 = GsonHelper.getAsString(var5, "id");
                            var3[var4] = new GameProfile(UUID.fromString(var6), GsonHelper.getAsString(var5, "name"));
                        }

                        var1.setSample(var3);
                    }
                }

                return var1;
            }

            public JsonElement serialize(ServerStatus.Players param0, Type param1, JsonSerializationContext param2) {
                JsonObject var0 = new JsonObject();
                var0.addProperty("max", param0.getMaxPlayers());
                var0.addProperty("online", param0.getNumPlayers());
                GameProfile[] var1 = param0.getSample();
                if (var1 != null && var1.length > 0) {
                    JsonArray var2 = new JsonArray();

                    for(int var3 = 0; var3 < var1.length; ++var3) {
                        JsonObject var4 = new JsonObject();
                        UUID var5 = var1[var3].getId();
                        var4.addProperty("id", var5 == null ? "" : var5.toString());
                        var4.addProperty("name", var1[var3].getName());
                        var2.add(var4);
                    }

                    var0.add("sample", var2);
                }

                return var0;
            }
        }
    }

    public static class Serializer implements JsonDeserializer<ServerStatus>, JsonSerializer<ServerStatus> {
        public ServerStatus deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "status");
            ServerStatus var1 = new ServerStatus();
            if (var0.has("description")) {
                var1.setDescription(param2.deserialize(var0.get("description"), Component.class));
            }

            if (var0.has("players")) {
                var1.setPlayers(param2.deserialize(var0.get("players"), ServerStatus.Players.class));
            }

            if (var0.has("version")) {
                var1.setVersion(param2.deserialize(var0.get("version"), ServerStatus.Version.class));
            }

            if (var0.has("favicon")) {
                var1.setFavicon(GsonHelper.getAsString(var0, "favicon"));
            }

            if (var0.has("enforcesSecureChat")) {
                var1.setEnforcesSecureChat(GsonHelper.getAsBoolean(var0, "enforcesSecureChat"));
            }

            return var1;
        }

        public JsonElement serialize(ServerStatus param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            var0.addProperty("enforcesSecureChat", param0.enforcesSecureChat());
            if (param0.getDescription() != null) {
                var0.add("description", param2.serialize(param0.getDescription()));
            }

            if (param0.getPlayers() != null) {
                var0.add("players", param2.serialize(param0.getPlayers()));
            }

            if (param0.getVersion() != null) {
                var0.add("version", param2.serialize(param0.getVersion()));
            }

            if (param0.getFavicon() != null) {
                var0.addProperty("favicon", param0.getFavicon());
            }

            return var0;
        }
    }

    public static class Version {
        private final String name;
        private final int protocol;

        public Version(String param0, int param1) {
            this.name = param0;
            this.protocol = param1;
        }

        public String getName() {
            return this.name;
        }

        public int getProtocol() {
            return this.protocol;
        }

        public static class Serializer implements JsonDeserializer<ServerStatus.Version>, JsonSerializer<ServerStatus.Version> {
            public ServerStatus.Version deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
                JsonObject var0 = GsonHelper.convertToJsonObject(param0, "version");
                return new ServerStatus.Version(GsonHelper.getAsString(var0, "name"), GsonHelper.getAsInt(var0, "protocol"));
            }

            public JsonElement serialize(ServerStatus.Version param0, Type param1, JsonSerializationContext param2) {
                JsonObject var0 = new JsonObject();
                var0.addProperty("name", param0.getName());
                var0.addProperty("protocol", param0.getProtocol());
                return var0;
            }
        }
    }
}
