package net.minecraft.client;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class User {
    private final String name;
    private final String uuid;
    private final String accessToken;
    private final Optional<String> xuid;
    private final Optional<String> clientId;
    private final User.Type type;

    public User(String param0, String param1, String param2, Optional<String> param3, Optional<String> param4, User.Type param5) {
        this.name = param0;
        this.uuid = param1;
        this.accessToken = param2;
        this.xuid = param3;
        this.clientId = param4;
        this.type = param5;
    }

    public String getSessionId() {
        return "token:" + this.accessToken + ":" + this.uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public Optional<String> getClientId() {
        return this.clientId;
    }

    public Optional<String> getXuid() {
        return this.xuid;
    }

    @Nullable
    public UUID getProfileId() {
        try {
            return UUIDTypeAdapter.fromString(this.getUuid());
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    public GameProfile getGameProfile() {
        return new GameProfile(this.getProfileId(), this.getName());
    }

    public User.Type getType() {
        return this.type;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        LEGACY("legacy"),
        MOJANG("mojang"),
        MSA("msa");

        private static final Map<String, User.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(param0 -> param0.name, Function.identity()));
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        @Nullable
        public static User.Type byName(String param0) {
            return BY_NAME.get(param0.toLowerCase(Locale.ROOT));
        }

        public String getName() {
            return this.name;
        }
    }
}
