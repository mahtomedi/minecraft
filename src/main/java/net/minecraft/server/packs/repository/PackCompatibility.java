package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum PackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Component description;
    private final Component confirmation;

    private PackCompatibility(String param0) {
        this.description = new TranslatableComponent("pack.incompatible." + param0).withStyle(ChatFormatting.GRAY);
        this.confirmation = new TranslatableComponent("pack.incompatible.confirm." + param0);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static PackCompatibility forFormat(int param0, PackType param1) {
        int var0 = param1.getVersion(SharedConstants.getCurrentVersion());
        if (param0 < var0) {
            return TOO_OLD;
        } else {
            return param0 > var0 ? TOO_NEW : COMPATIBLE;
        }
    }

    public static PackCompatibility forMetadata(PackMetadataSection param0, PackType param1) {
        return forFormat(param0.getPackFormat(), param1);
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
