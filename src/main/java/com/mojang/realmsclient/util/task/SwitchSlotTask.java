package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RetryCallException;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SwitchSlotTask extends LongRunningTask {
    private final long worldId;
    private final int slot;
    private final Runnable callback;

    public SwitchSlotTask(long param0, int param1, Runnable param2) {
        this.worldId = param0;
        this.slot = param1;
        this.callback = param2;
    }

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();
        String var1 = I18n.get("mco.minigame.world.slot.screen.title");
        this.setTitle(var1);

        for(int var2 = 0; var2 < 25; ++var2) {
            try {
                if (this.aborted()) {
                    return;
                }

                if (var0.switchSlot(this.worldId, this.slot)) {
                    this.callback.run();
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

                LOGGER.error("Couldn't switch world!");
                this.error(var6.toString());
            }
        }

    }
}
