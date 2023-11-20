package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTickTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GetServerDetailsTask extends LongRunningTask {
    private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
    private static final UUID REALMS_PACK_UUID = UUID.fromString("08c3b151-90fb-4c09-b6cf-0548364671bb");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.connect.connecting");
    private final RealmsServer server;
    private final Screen lastScreen;

    public GetServerDetailsTask(Screen param0, RealmsServer param1) {
        this.lastScreen = param0;
        this.server = param1;
    }

    @Override
    public void run() {
        RealmsServerAddress var0;
        try {
            var0 = this.fetchServerAddress();
        } catch (CancellationException var4) {
            LOGGER.info("User aborted connecting to realms");
            return;
        } catch (RealmsServiceException var5) {
            switch(var5.realmsError.errorCode()) {
                case 6002:
                    setScreen(new RealmsTermsScreen(this.lastScreen, this.server));
                    return;
                case 6006:
                    boolean var3 = Minecraft.getInstance().isLocalPlayer(this.server.ownerUUID);
                    setScreen(
                        (Screen)(var3
                            ? new RealmsBrokenWorldScreen(this.lastScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME)
                            : new RealmsGenericErrorScreen(
                                Component.translatable("mco.brokenworld.nonowner.title"),
                                Component.translatable("mco.brokenworld.nonowner.error"),
                                this.lastScreen
                            ))
                    );
                    return;
                default:
                    this.error(var5);
                    LOGGER.error("Couldn't connect to world", (Throwable)var5);
                    return;
            }
        } catch (TimeoutException var61) {
            this.error(Component.translatable("mco.errorMessage.connectionFailure"));
            return;
        } catch (Exception var71) {
            LOGGER.error("Couldn't connect to world", (Throwable)var71);
            this.error(var71);
            return;
        }

        boolean var7 = var0.resourcePackUrl != null && var0.resourcePackHash != null;
        Screen var8 = (Screen)(var7 ? this.resourcePackDownloadConfirmationScreen(var0, this::connectScreen) : this.connectScreen(var0));
        setScreen(var8);
    }

    @Override
    public Component getTitle() {
        return TITLE;
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
        return new RealmsLongRunningMcoTickTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, param0));
    }

    private RealmsLongConfirmationScreen resourcePackDownloadConfirmationScreen(RealmsServerAddress param0, Function<RealmsServerAddress, Screen> param1) {
        BooleanConsumer var0 = param2 -> {
            if (!param2) {
                setScreen(this.lastScreen);
            } else {
                setScreen(new GenericDirtMessageScreen(APPLYING_PACK_TEXT));
                this.scheduleResourcePackDownload(param0).thenRun(() -> setScreen(param1.apply(param0))).exceptionally(param1x -> {
                    Minecraft.getInstance().getDownloadedPackSource().cleanupAfterDisconnect();
                    LOGGER.error("Failed to download resource pack from {}", param0, param1x);
                    setScreen(new RealmsGenericErrorScreen(Component.translatable("mco.download.resourcePack.fail"), this.lastScreen));
                    return null;
                });
            }
        };
        return new RealmsLongConfirmationScreen(
            var0,
            RealmsLongConfirmationScreen.Type.INFO,
            Component.translatable("mco.configure.world.resourcepack.question.line1"),
            Component.translatable("mco.configure.world.resourcepack.question.line2"),
            true
        );
    }

    private CompletableFuture<?> scheduleResourcePackDownload(RealmsServerAddress param0) {
        try {
            DownloadedPackSource var0 = Minecraft.getInstance().getDownloadedPackSource();
            CompletableFuture<Void> var1 = var0.waitForPackFeedback(REALMS_PACK_UUID);
            var0.allowServerPacks();
            var0.pushPack(REALMS_PACK_UUID, new URL(param0.resourcePackUrl), param0.resourcePackHash);
            return var1;
        } catch (Exception var4) {
            CompletableFuture<Void> var3 = new CompletableFuture<>();
            var3.completeExceptionally(var4);
            return var3;
        }
    }
}
