package net.minecraft.server.packs.repository;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum PackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Component description;
    private final Component confirmation;

    private PackCompatibility(String param0) {
        this.description = new TranslatableComponent("pack.incompatible." + param0);
        this.confirmation = new TranslatableComponent("pack.incompatible.confirm." + param0);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static PackCompatibility forFormat(int param0) {
        if (param0 < SharedConstants.getCurrentVersion().getPackVersion()) {
            return TOO_OLD;
        } else {
            return param0 > SharedConstants.getCurrentVersion().getPackVersion() ? TOO_NEW : COMPATIBLE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDescription() {
        return this.description;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getConfirmation() {
        return this.confirmation;
    }
}
