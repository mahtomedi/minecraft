package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RestoreTask extends LongRunningTask {
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
        this.setTitle(I18n.get("mco.backup.restoring"));
        RealmsClient var0 = RealmsClient.create();
        int var1 = 0;

        while(var1 < 25) {
            try {
                if (this.aborted()) {
                    return;
                }

                var0.restoreWorld(this.worldId, this.backup.backupId);
                pause(1);
                if (this.aborted()) {
                    return;
                }

                setScreen(this.lastScreen.getNewScreen());
                return;
            } catch (RetryCallException var4) {
                if (this.aborted()) {
                    return;
                }

                pause(var4.delaySeconds);
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
                this.error(var6.getLocalizedMessage());
                return;
            }
        }

    }
}
