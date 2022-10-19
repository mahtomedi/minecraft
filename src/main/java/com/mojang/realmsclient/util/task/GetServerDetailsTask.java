package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GetServerDetailsTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RealmsServer server;
    private final Screen lastScreen;
    private final RealmsMainScreen mainScreen;
    private final ReentrantLock connectLock;

    public GetServerDetailsTask(RealmsMainScreen param0, Screen param1, RealmsServer param2, ReentrantLock param3) {
        this.lastScreen = param1;
        this.mainScreen = param0;
        this.server = param2;
        this.connectLock = param3;
    }

    @Override
    public void run() {
        this.setTitle(Component.translatable("mco.connect.connecting"));

        RealmsServerAddress var0;
        try {
            var0 = this.fetchServerAddress();
        } catch (CancellationException var4) {
            LOGGER.info("User aborted connecting to realms");
            return;
        } catch (RealmsServiceException var5) {
            switch(var5.realmsErrorCodeOrDefault(-1)) {
                case 6002:
                    setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
                    return;
                case 6006:
                    boolean var3 = this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid());
                    setScreen(
                        (Screen)(var3
                            ? new RealmsBrokenWorldScreen(
                                this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME
                            )
                            : new RealmsGenericErrorScreen(
                                Component.translatable("mco.brokenworld.nonowner.title"),
                                Component.translatable("mco.brokenworld.nonowner.error"),
                                this.lastScreen
                            ))
                    );
                    return;
                default:
                    this.error(var5.toString());
                    LOGGER.error("Couldn't connect to world", (Throwable)var5);
                    return;
            }
        } catch (TimeoutException var61) {
            this.error(Component.translatable("mco.errorMessage.connectionFailure"));
            return;
        } catch (Exception var71) {
            LOGGER.error("Couldn't connect to world", (Throwable)var71);
            this.error(var71.getLocalizedMessage());
            return;
        }

        boolean var7 = var0.resourcePackUrl != null && var0.resourcePackHash != null;
        Screen var8 = (Screen)(var7 ? this.resourcePackDownloadConfirmationScreen(var0, this::connectScreen) : this.connectScreen(var0));
        setScreen(var8);
    }

    private RealmsServerAddress fetchServerAddress() throws RealmsServiceException, TimeoutException, CancellationException {
        RealmsClient var0 = RealmsClient.create();

        for(int var1 = 0; var1 < 40; ++var1) {
            if (this.aborted()) {
                throw new CancellationException();
            }

            try {
                return var0.join(this.server.id);
            } catch (RetryCallException var4) {
                pause((long)var4.delaySeconds);
            }
        }

        throw new TimeoutException();
    }

    public RealmsLongRunningMcoTaskScreen connectScreen(RealmsServerAddress param0) {
        return new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, param0));
    }

    private RealmsLongConfirmationScreen resourcePackDownloadConfirmationScreen(RealmsServerAddress param0, Function<RealmsServerAddress, Screen> param1) {
        BooleanConsumer var0 = param2 -> {
            try {
                if (param2) {
                    this.scheduleResourcePackDownload(param0).thenRun(() -> setScreen(param1.apply(param0))).exceptionally(param1x -> {
                        Minecraft.getInstance().getDownloadedPackSource().clearServerPack();
                        LOGGER.error("Failed to download resource pack from {}", param0, param1x);
                        setScreen(new RealmsGenericErrorScreen(Component.literal("Failed to download resource pack!"), this.lastScreen));
                        return null;
                    });
                    return;
                }

                setScreen(this.lastScreen);
            } finally {
                if (this.connectLock.isHeldByCurrentThread()) {
                    this.connectLock.unlock();
                }

            }

        };
        return new RealmsLongConfirmationScreen(
            var0,
            RealmsLongConfirmationScreen.Type.Info,
            Component.translatable("mco.configure.world.resourcepack.question.line1"),
            Component.translatable("mco.configure.world.resourcepack.question.line2"),
            true
        );
    }

    private CompletableFuture<?> scheduleResourcePackDownload(RealmsServerAddress param0) {
        try {
            return Minecraft.getInstance()
                .getDownloadedPackSource()
                .downloadAndSelectResourcePack(new URL(param0.resourcePackUrl), param0.resourcePackHash, false);
        } catch (Exception var4) {
            CompletableFuture<Void> var1 = new CompletableFuture<>();
            var1.completeExceptionally(var4);
            return var1;
        }
    }
}
