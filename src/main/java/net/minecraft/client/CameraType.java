package net.minecraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum CameraType {
    FIRST_PERSON(true, false),
    THIRD_PERSON_BACK(false, false),
    THIRD_PERSON_FRONT(false, true);

    private static final CameraType[] VALUES = values();
    private final boolean firstPerson;
    private final boolean mirrored;

    private CameraType(boolean param0, boolean param1) {
        this.firstPerson = param0;
        this.mirrored = param1;
    }

    public boolean isFirstPerson() {
        return this.firstPerson;
    }

    public boolean isMirrored() {
        return this.mirrored;
    }

    public CameraType cycle() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}
