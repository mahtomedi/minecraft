package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SwitchMinigameTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long worldId;
    private final WorldTemplate worldTemplate;
    private final RealmsConfigureWorldScreen lastScreen;

    public SwitchMinigameTask(long param0, WorldTemplate param1, RealmsConfigureWorldScreen param2) {
        this.worldId = param0;
        this.worldTemplate = param1;
        this.lastScreen = param2;
    }

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();
        this.setTitle(Component.translatable("mco.minigame.world.starting.screen.title"));

        for(int var1 = 0; var1 < 25; ++var1) {
            try {
                if (this.aborted()) {
                    return;
                }

                if (var0.putIntoMinigameMode(this.worldId, this.worldTemplate.id)) {
                    setScreen(this.lastScreen);
                    break;
                }
            } catch (RetryCallException var4) {
                if (this.aborted()) {
                    return;
                }

                pause((long)var4.delaySeconds);
            } catch (Exception var5) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't start mini game!");
                this.error(var5);
            }
        }

    }
}
