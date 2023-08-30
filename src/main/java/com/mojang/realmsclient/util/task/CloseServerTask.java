package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CloseServerTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.configure.world.closing");
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;

    public CloseServerTask(RealmsServer param0, RealmsConfigureWorldScreen param1) {
        this.serverData = param0;
        this.configureScreen = param1;
    }

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();

        for(int var1 = 0; var1 < 25; ++var1) {
            if (this.aborted()) {
                return;
            }

            try {
                boolean var2 = var0.close(this.serverData.id);
                if (var2) {
                    this.configureScreen.stateChanged();
                    this.serverData.state = RealmsServer.State.CLOSED;
                    setScreen(this.configureScreen);
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

                LOGGER.error("Failed to close server", (Throwable)var5);
                this.error(var5);
            }
        }

    }

    @Override
    public Component getTitle() {
        return TITLE;
    }
}
