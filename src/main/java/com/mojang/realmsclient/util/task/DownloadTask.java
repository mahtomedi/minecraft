package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DownloadTask extends LongRunningTask {
    private final long worldId;
    private final int slot;
    private final Screen lastScreen;
    private final String downloadName;

    public DownloadTask(long param0, int param1, String param2, Screen param3) {
        this.worldId = param0;
        this.slot = param1;
        this.lastScreen = param3;
        this.downloadName = param2;
    }

    @Override
    public void run() {
        this.setTitle(new TranslatableComponent("mco.download.preparing"));
        RealmsClient var0 = RealmsClient.create();
        int var1 = 0;

        while(var1 < 25) {
            try {
                if (this.aborted()) {
                    return;
                }

                WorldDownload var2 = var0.requestDownloadInfo(this.worldId, this.slot);
                pause(1L);
                if (this.aborted()) {
                    return;
                }

                setScreen(new RealmsDownloadLatestWorldScreen(this.lastScreen, var2, this.downloadName, param0 -> {
                }));
                return;
            } catch (RetryCallException var4) {
                if (this.aborted()) {
                    return;
                }

                pause((long)var4.delaySeconds);
                ++var1;
            } catch (RealmsServiceException var51) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't download world data");
                setScreen(new RealmsGenericErrorScreen(var51, this.lastScreen));
                return;
            } catch (Exception var6) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't download world data", (Throwable)var6);
                this.error(var6.getLocalizedMessage());
                return;
            }
        }

    }
}
