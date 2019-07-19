package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.util.RealmsTasks;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsResourcePackScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final RealmsServerAddress serverAddress;
    private final ReentrantLock connectLock;

    public RealmsResourcePackScreen(RealmsScreen param0, RealmsServerAddress param1, ReentrantLock param2) {
        this.lastScreen = param0;
        this.serverAddress = param1;
        this.connectLock = param2;
    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        try {
            if (!param0) {
                Realms.setScreen(this.lastScreen);
            } else {
                try {
                    Realms.downloadResourcePack(this.serverAddress.resourcePackUrl, this.serverAddress.resourcePackHash)
                        .thenRun(
                            () -> {
                                RealmsLongRunningMcoTaskScreen var0x = new RealmsLongRunningMcoTaskScreen(
                                    this.lastScreen, new RealmsTasks.RealmsConnectTask(this.lastScreen, this.serverAddress)
                                );
                                var0x.start();
                                Realms.setScreen(var0x);
                            }
                        )
                        .exceptionally(param0x -> {
                            Realms.clearResourcePack();
                            LOGGER.error(param0x);
                            Realms.setScreen(new RealmsGenericErrorScreen("Failed to download resource pack!", this.lastScreen));
                            return null;
                        });
                } catch (Exception var7) {
                    Realms.clearResourcePack();
                    LOGGER.error(var7);
                    Realms.setScreen(new RealmsGenericErrorScreen("Failed to download resource pack!", this.lastScreen));
                }
            }
        } finally {
            if (this.connectLock != null && this.connectLock.isHeldByCurrentThread()) {
                this.connectLock.unlock();
            }

        }

    }
}
