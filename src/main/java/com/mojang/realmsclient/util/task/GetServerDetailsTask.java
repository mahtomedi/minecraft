package com.mojang.realmsclient.util.task;

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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GetServerDetailsTask extends LongRunningTask {
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
        this.setTitle(I18n.get("mco.connect.connecting"));
        RealmsClient var0 = RealmsClient.create();
        boolean var1 = false;
        boolean var2 = false;
        int var3 = 5;
        RealmsServerAddress var4 = null;
        boolean var5 = false;
        boolean var6 = false;

        for(int var7 = 0; var7 < 40 && !this.aborted(); ++var7) {
            try {
                var4 = var0.join(this.server.id);
                var1 = true;
            } catch (RetryCallException var111) {
                var3 = var111.delaySeconds;
            } catch (RealmsServiceException var121) {
                if (var121.errorCode == 6002) {
                    var5 = true;
                } else if (var121.errorCode == 6006) {
                    var6 = true;
                } else {
                    var2 = true;
                    this.error(var121.toString());
                    LOGGER.error("Couldn't connect to world", (Throwable)var121);
                }
                break;
            } catch (Exception var131) {
                var2 = true;
                LOGGER.error("Couldn't connect to world", (Throwable)var131);
                this.error(var131.getLocalizedMessage());
                break;
            }

            if (var1) {
                break;
            }

            this.sleep(var3);
        }

        if (var5) {
            setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
        } else if (var6) {
            if (this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid())) {
                setScreen(
                    new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME)
                );
            } else {
                setScreen(
                    new RealmsGenericErrorScreen(
                        new TranslatableComponent("mco.brokenworld.nonowner.title"),
                        new TranslatableComponent("mco.brokenworld.nonowner.error"),
                        this.lastScreen
                    )
                );
            }
        } else if (!this.aborted() && !var2) {
            if (var1) {
                RealmsServerAddress var11 = var4;
                if (var11.resourcePackUrl != null && var11.resourcePackHash != null) {
                    Component var12 = new TranslatableComponent("mco.configure.world.resourcepack.question.line1");
                    Component var13 = new TranslatableComponent("mco.configure.world.resourcepack.question.line2");
                    setScreen(
                        new RealmsLongConfirmationScreen(
                            param1 -> {
                                try {
                                    if (param1) {
                                        Function<Throwable, Void> var0x = param0x -> {
                                            Minecraft.getInstance().getClientPackSource().clearServerPack();
                                            LOGGER.error(param0x);
                                            setScreen(new RealmsGenericErrorScreen(new TextComponent("Failed to download resource pack!"), this.lastScreen));
                                            return null;
                                        };
        
                                        try {
                                            Minecraft.getInstance()
                                                .getClientPackSource()
                                                .downloadAndSelectResourcePack(var11.resourcePackUrl, var11.resourcePackHash)
                                                .thenRun(
                                                    () -> this.setScreen(
                                                            new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, var11))
                                                        )
                                                )
                                                .exceptionally(var0x);
                                        } catch (Exception var8x) {
                                            var0x.apply(var8x);
                                        }
                                    } else {
                                        setScreen(this.lastScreen);
                                    }
                                } finally {
                                    if (this.connectLock != null && this.connectLock.isHeldByCurrentThread()) {
                                        this.connectLock.unlock();
                                    }
        
                                }
        
                            },
                            RealmsLongConfirmationScreen.Type.Info,
                            var12,
                            var13,
                            true
                        )
                    );
                } else {
                    this.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, var11)));
                }
            } else {
                this.error(new TranslatableComponent("mco.errorMessage.connectionFailure"));
            }
        }

    }

    private void sleep(int param0) {
        try {
            Thread.sleep((long)(param0 * 1000));
        } catch (InterruptedException var3) {
            LOGGER.warn(var3.getLocalizedMessage());
        }

    }
}
