package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RestoreTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Backup backup;
    private final long worldId;
    private final RealmsConfigureWorldScreen lastScreen;

    public RestoreTask(Backup param0, long param1, RealmsConfigureWorldScreen param2) {
        this.backup = param0;
        this.worldId = param1;
        this.lastScreen = param2;
    }

    @Override
    public void run() {
        this.setTitle(Component.translatable("mco.backup.restoring"));
        RealmsClient var0 = RealmsClient.create();
        int var1 = 0;

        while(var1 < 25) {
            try {
                if (this.aborted()) {
                    return;
                }

                var0.restoreWorld(this.worldId, this.backup.backupId);
                pause(1L);
                if (this.aborted()) {
                    return;
                }

                setScreen(this.lastScreen.getNewScreen());
                return;
            } catch (RetryCallException var4) {
                if (this.aborted()) {
                    return;
                }

                pause((long)var4.delaySeconds);
                ++var1;
            } catch (RealmsServiceException var5) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't restore backup", (Throwable)var5);
                setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
                return;
            } catch (Exception var6) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't restore backup", (Throwable)var6);
                this.error(var6);
                return;
            }
        }

    }
}
