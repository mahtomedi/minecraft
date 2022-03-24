package net.minecraft.world.entity.monster.warden;

import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum AngerLevel {
    CALM(0, SoundEvents.WARDEN_AMBIENT),
    AGITATED(40, SoundEvents.WARDEN_AGITATED),
    ANGRY(80, SoundEvents.WARDEN_ANGRY);

    private static final AngerLevel[] SORTED_LEVELS = Util.make(
        values(), param0 -> Arrays.sort(param0, (param0x, param1) -> Integer.compare(param1.minimumAnger, param0x.minimumAnger))
    );
    private final int minimumAnger;
    private final SoundEvent ambientSound;

    private AngerLevel(int param0, SoundEvent param1) {
        this.minimumAnger = param0;
        this.ambientSound = param1;
    }

    public int getMinimumAnger() {
        return this.minimumAnger;
    }

    public SoundEvent getAmbientSound() {
        return this.ambientSound;
    }

    public static AngerLevel byAnger(int param0) {
        for(AngerLevel var0 : SORTED_LEVELS) {
            if (param0 >= var0.minimumAnger) {
                return var0;
            }
        }

        return CALM;
    }
}
