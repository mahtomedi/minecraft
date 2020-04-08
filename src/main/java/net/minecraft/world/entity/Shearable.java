package net.minecraft.world.entity;

import net.minecraft.sounds.SoundSource;

public interface Shearable {
    void shear(SoundSource var1);

    boolean readyForShearing();
}
