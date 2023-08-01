package com.mojang.realmsclient;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsAvailability {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static CompletableFuture<RealmsAvailability.Result> future;

    public static CompletableFuture<RealmsAvailability.Result> get() {
        if (future == null || shouldRefresh(future)) {
            future = check();
        }

        return future;
    }

    private static boolean shouldRefresh(CompletableFuture<RealmsAvailability.Result> param0) {
        RealmsAvailability.Result var0 = param0.getNow(null);
        return var0 != null && var0.exception() != null;
    }

    private static CompletableFuture<RealmsAvailability.Result> check() {
        return CompletableFuture.supplyAsync(
            () -> {
                RealmsClient var0 = RealmsClient.create();
    
                try {
                    if (var0.clientCompatible() != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                        return new RealmsAvailability.Result(RealmsAvailability.Type.INCOMPATIBLE_CLIENT);
                    } else {
                        return !var0.hasParentalConsent()
                            ? new RealmsAvailability.Result(RealmsAvailability.Type.NEEDS_PARENTAL_CONSENT)
                            : new RealmsAvailability.Result(RealmsAvailability.Type.SUCCESS);
                    }
                } catch (RealmsServiceException var2) {
                    LOGGER.error("Couldn't connect to realms", (Throwable)var2);
                    return var2.realmsError.errorCode() == 401
                        ? new RealmsAvailability.Result(RealmsAvailability.Type.AUTHENTICATION_ERROR)
                        : new RealmsAvailability.Result(var2);
                }
            },
            Util.ioPool()
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static record Result(RealmsAvailability.Type type, @Nullable RealmsServiceException exception) {
        public Result(RealmsAvailability.Type param0) {
            this(param0, null);
        }

        public Result(RealmsServiceException param0) {
            this(RealmsAvailability.Type.UNEXPECTED_ERROR, param0);
        }

        @Nullable
        public Screen createErrorScreen(Screen param0) {
            return (Screen)(switch(this.type) {
                case SUCCESS -> null;
                case INCOMPATIBLE_CLIENT -> new RealmsClientOutdatedScreen(param0);
                case NEEDS_PARENTAL_CONSENT -> new RealmsParentalConsentScreen(param0);
                case AUTHENTICATION_ERROR -> new RealmsGenericErrorScreen(
                Component.translatable("mco.error.invalid.session.title"), Component.translatable("mco.error.invalid.session.message"), param0
            );
                case UNEXPECTED_ERROR -> new RealmsGenericErrorScreen(Objects.requireNonNull(this.exception), param0);
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        SUCCESS,
        INCOMPATIBLE_CLIENT,
        NEEDS_PARENTAL_CONSENT,
        AUTHENTICATION_ERROR,
        UNEXPECTED_ERROR;
    }
}
