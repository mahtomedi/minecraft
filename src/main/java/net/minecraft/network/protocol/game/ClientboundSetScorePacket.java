package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetScorePacket implements Packet<ClientGamePacketListener> {
    private String owner = "";
    @Nullable
    private String objectiveName;
    private int score;
    private ServerScoreboard.Method method;

    public ClientboundSetScorePacket() {
    }

    public ClientboundSetScorePacket(ServerScoreboard.Method param0, @Nullable String param1, String param2, int param3) {
        if (param0 != ServerScoreboard.Method.REMOVE && param1 == null) {
            throw new IllegalArgumentException("Need an objective name");
        } else {
            this.owner = param2;
            this.objectiveName = param1;
            this.score = param3;
            this.method = param0;
        }
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.owner = param0.readUtf(40);
        this.method = param0.readEnum(ServerScoreboard.Method.class);
        String var0 = param0.readUtf(16);
        this.objectiveName = Objects.equals(var0, "") ? null : var0;
        if (this.method != ServerScoreboard.Method.REMOVE) {
            this.score = param0.readVarInt();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.owner);
        param0.writeEnum(this.method);
        param0.writeUtf(this.objectiveName == null ? "" : this.objectiveName);
        if (this.method != ServerScoreboard.Method.REMOVE) {
            param0.writeVarInt(this.score);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetScore(this);
    }

    @OnlyIn(Dist.CLIENT)
    public String getOwner() {
        return this.owner;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public String getObjectiveName() {
        return this.objectiveName;
    }

    @OnlyIn(Dist.CLIENT)
    public int getScore() {
        return this.score;
    }

    @OnlyIn(Dist.CLIENT)
    public ServerScoreboard.Method getMethod() {
        return this.method;
    }
}
