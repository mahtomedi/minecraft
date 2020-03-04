package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SwitchMinigameTask extends LongRunningTask {
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
        String var1 = I18n.get("mco.minigame.world.starting.screen.title");
        this.setTitle(var1);

        for(int var2 = 0; var2 < 25; ++var2) {
            try {
                if (this.aborted()) {
                    return;
                }

                if (var0.putIntoMinigameMode(this.worldId, this.worldTemplate.id)) {
                    setScreen(this.lastScreen);
                    break;
                }
            } catch (RetryCallException var5) {
                if (this.aborted()) {
                    return;
                }

                pause(var5.delaySeconds);
            } catch (Exception var6) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't start mini game!");
                this.error(var6.toString());
            }
        }

    }
}
