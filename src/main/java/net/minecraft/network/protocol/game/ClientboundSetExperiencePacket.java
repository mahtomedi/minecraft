package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
    private float experienceProgress;
    private int totalExperience;
    private int experienceLevel;

    public ClientboundSetExperiencePacket() {
    }

    public ClientboundSetExperiencePacket(float param0, int param1, int param2) {
        this.experienceProgress = param0;
        this.totalExperience = param1;
        this.experienceLevel = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.experienceProgress = param0.readFloat();
        this.experienceLevel = param0.readVarInt();
        this.totalExperience = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
