package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResettingWorldTask extends LongRunningTask {
    private final String seed;
    private final WorldTemplate worldTemplate;
    private final int levelType;
    private final boolean generateStructures;
    private final long serverId;
    private String title = I18n.get("mco.reset.world.resetting.screen.title");
    private final Runnable callback;

    public ResettingWorldTask(
        @Nullable String param0, @Nullable WorldTemplate param1, int param2, boolean param3, long param4, @Nullable String param5, Runnable param6
    ) {
        this.seed = param0;
        this.worldTemplate = param1;
        this.levelType = param2;
        this.generateStructures = param3;
        this.serverId = param4;
        if (param5 != null) {
            this.title = param5;
        }

        this.callback = param6;
    }

    @Override
    public void run() {
        RealmsClient var0 = RealmsClient.create();
        this.setTitle(this.title);
        int var1 = 0;

        while(var1 < 25) {
            try {
                if (this.aborted()) {
                    return;
                }

                if (this.worldTemplate != null) {
                    var0.resetWorldWithTemplate(this.serverId, this.worldTemplate.id);
                } else {
                    var0.resetWorldWithSeed(this.serverId, this.seed, this.levelType, this.generateStructures);
                }

                if (this.aborted()) {
                    return;
                }

                this.callback.run();
                return;
            } catch (RetryCallException var4) {
                if (this.aborted()) {
                    return;
                }

                pause(var4.delaySeconds);
                ++var1;
            } catch (Exception var5) {
                if (this.aborted()) {
                    return;
                }

                LOGGER.error("Couldn't reset world");
                this.error(var5.toString());
                return;
            }
        }

    }
}
