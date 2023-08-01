package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class OpenServerTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RealmsServer serverData;
    private final Screen returnScreen;
    private final boolean join;
    private final RealmsMainScreen mainScreen;
    private final Minecraft minecraft;

    public OpenServerTask(RealmsServer param0, Screen param1, RealmsMainScreen param2, boolean param3, Minecraft param4) {
        this.serverData = param0;
        this.returnScreen = param1;
        this.join = param3;
        this.mainScreen = param2;
        this.minecraft = param4;
    }

    @Override
    public void run() {
        this.setTitle(Component.translatable("mco.configure.world.opening"));
        RealmsClient var0 = RealmsClient.create();

        for(int var1 = 0; var1 < 25; ++var1) {
            if (this.aborted()) {
                return;
            }

            try {
                boolean var2 = var0.open(this.serverData.id);
                if (var2) {
                    this.minecraft.execute(() -> {
                        if (this.returnScreen instanceof RealmsConfigureWorldScreen) {
                            ((RealmsConfigureWorldScreen)this.returnScreen).stateChanged();
                        }

                        this.serverData.state = RealmsServer.State.OPEN;
                        if (this.join) {
                            this.mainScreen.play(this.serverData, this.returnScreen);
                        } else {
                            this.minecraft.setScreen(this.returnScreen);
                        }

                    });
                    break;
                }
            } catch (RetryCallException var41) {
                if (this.aborted()) {
                    return;
                }

                pause((long)var41.delaySeconds);
            } catch (Exception var5) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Failed to open server", (Throwable)var5);
                this.error(var5);
            }
        }

    }
}
