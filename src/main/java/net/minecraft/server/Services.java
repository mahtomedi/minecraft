package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.SignatureValidator;

public record Services(
    MinecraftSessionService sessionService, ServicesKeySet servicesKeySet, GameProfileRepository profileRepository, GameProfileCache profileCache
) {
    private static final String USERID_CACHE_FILE = "usercache.json";

    public static Services create(YggdrasilAuthenticationService param0, File param1) {
        MinecraftSessionService var0 = param0.createMinecraftSessionService();
        GameProfileRepository var1 = param0.createProfileRepository();
        GameProfileCache var2 = new GameProfileCache(var1, new File(param1, "usercache.json"));
        return new Services(var0, param0.getServicesKeySet(), var1, var2);
    }

    @Nullable
    public SignatureValidator profileKeySignatureValidator() {
        return SignatureValidator.from(this.servicesKeySet, ServicesKeyType.PROFILE_KEY);
    }
}
