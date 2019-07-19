package net.minecraft.client;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
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
    private final User.Type type;

    public User(String param0, String param1, String param2, String param3) {
        this.name = param0;
        this.uuid = param1;
        this.accessToken = param2;
        this.type = User.Type.byName(param3);
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

    public GameProfile getGameProfile() {
        try {
            UUID var0 = UUIDTypeAdapter.fromString(this.getUuid());
            return new GameProfile(var0, this.getName());
        } catch (IllegalArgumentException var2) {
            return new GameProfile(null, this.getName());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        LEGACY("legacy"),
        MOJANG("mojang");

        private static final Map<String, User.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(param0 -> param0.name, Function.identity()));
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        @Nullable
        public static User.Type byName(String param0) {
            return BY_NAME.get(param0.toLowerCase(Locale.ROOT));
        }
    }
}
