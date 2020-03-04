package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OpenServerTask extends LongRunningTask {
    private final RealmsServer serverData;
    private final Screen returnScreen;
    private final boolean join;
    private final RealmsMainScreen mainScreen;

    public OpenServerTask(RealmsServer param0, Screen param1, RealmsMainScreen param2, boolean param3) {
        this.serverData = param0;
        this.returnScreen = param1;
        this.join = param3;
        this.mainScreen = param2;
    }

    @Override
    public void run() {
        this.setTitle(I18n.get("mco.configure.world.opening"));
        RealmsClient var0 = RealmsClient.create();

        for(int var1 = 0; var1 < 25; ++var1) {
            if (this.aborted()) {
                return;
            }

            try {
                boolean var2 = var0.open(this.serverData.id);
                if (var2) {
                    if (this.returnScreen instanceof RealmsConfigureWorldScreen) {
                        ((RealmsConfigureWorldScreen)this.returnScreen).stateChanged();
                    }

                    this.serverData.state = RealmsServer.State.OPEN;
                    if (this.join) {
                        this.mainScreen.play(this.serverData, this.returnScreen);
                    } else {
                        setScreen(this.returnScreen);
                    }
                    break;
                }
            } catch (RetryCallException var41) {
                if (this.aborted()) {
                    return;
                }

                pause(var41.delaySeconds);
            } catch (Exception var5) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Failed to open server", (Throwable)var5);
                this.error("Failed to open the server");
            }
        }

    }
}
