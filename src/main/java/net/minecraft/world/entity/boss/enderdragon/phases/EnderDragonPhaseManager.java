package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.slf4j.Logger;

public class EnderDragonPhaseManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final EnderDragon dragon;
    private final DragonPhaseInstance[] phases = new DragonPhaseInstance[EnderDragonPhase.getCount()];
    @Nullable
    private DragonPhaseInstance currentPhase;

    public EnderDragonPhaseManager(EnderDragon param0) {
        this.dragon = param0;
        this.setPhase(EnderDragonPhase.HOVERING);
    }

    public void setPhase(EnderDragonPhase<?> param0) {
        if (this.currentPhase == null || param0 != this.currentPhase.getPhase()) {
            if (this.currentPhase != null) {
                this.currentPhase.end();
            }

            this.currentPhase = this.getPhase(param0);
            if (!this.dragon.level.isClientSide) {
                this.dragon.getEntityData().set(EnderDragon.DATA_PHASE, param0.getId());
            }

            LOGGER.debug("Dragon is now in phase {} on the {}", param0, this.dragon.level.isClientSide ? "client" : "server");
            this.currentPhase.begin();
        }
    }

    public DragonPhaseInstance getCurrentPhase() {
        return this.currentPhase;
    }

    public <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> param0) {
        int var0 = param0.getId();
        if (this.phases[var0] == null) {
            this.phases[var0] = param0.createInstance(this.dragon);
        }

        return (T)this.phases[var0];
    }
}
