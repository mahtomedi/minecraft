package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
    private final float experienceProgress;
    private final int totalExperience;
    private final int experienceLevel;

    public ClientboundSetExperiencePacket(float param0, int param1, int param2) {
        this.experienceProgress = param0;
        this.totalExperience = param1;
        this.experienceLevel = param2;
    }

    public ClientboundSetExperiencePacket(FriendlyByteBuf param0) {
        this.experienceProgress = param0.readFloat();
        this.experienceLevel = param0.readVarInt();
        this.totalExperience = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeFloat(this.experienceProgress);
        param0.writeVarInt(this.experienceLevel);
        param0.writeVarInt(this.totalExperience);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetExperience(this);
    }

    @OnlyIn(Dist.CLIENT)
    public float getExperienceProgress() {
        return this.experienceProgress;
    }

    @OnlyIn(Dist.CLIENT)
    public int getTotalExperience() {
        return this.totalExperience;
    }

    @OnlyIn(Dist.CLIENT)
    public int getExperienceLevel() {
        return this.experienceLevel;
    }
}
