package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetPlayerTeamPacket implements Packet<ClientGamePacketListener> {
    private String name = "";
    private Component displayName = TextComponent.EMPTY;
    private Component playerPrefix = TextComponent.EMPTY;
    private Component playerSuffix = TextComponent.EMPTY;
    private String nametagVisibility = Team.Visibility.ALWAYS.name;
    private String collisionRule = Team.CollisionRule.ALWAYS.name;
    private ChatFormatting color = ChatFormatting.RESET;
    private final Collection<String> players = Lists.newArrayList();
    private int method;
    private int options;

    public ClientboundSetPlayerTeamPacket() {
    }

    public ClientboundSetPlayerTeamPacket(PlayerTeam param0, int param1) {
        this.name = param0.getName();
        this.method = param1;
        if (param1 == 0 || param1 == 2) {
            this.displayName = param0.getDisplayName();
            this.options = param0.packOptions();
            this.nametagVisibility = param0.getNameTagVisibility().name;
            this.collisionRule = param0.getCollisionRule().name;
            this.color = param0.getColor();
            this.playerPrefix = param0.getPlayerPrefix();
            this.playerSuffix = param0.getPlayerSuffix();
        }

        if (param1 == 0) {
            this.players.addAll(param0.getPlayers());
        }

    }

    public ClientboundSetPlayerTeamPacket(PlayerTeam param0, Collection<String> param1, int param2) {
        if (param2 != 3 && param2 != 4) {
            throw new IllegalArgumentException("Method must be join or leave for player constructor");
        } else if (param1 != null && !param1.isEmpty()) {
            this.method = param2;
            this.name = param0.getName();
            this.players.addAll(param1);
        } else {
            throw new IllegalArgumentException("Players cannot be null/empty");
        }
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.name = param0.readUtf(16);
        this.method = param0.readByte();
        if (this.method == 0 || this.method == 2) {
            this.displayName = param0.readComponent();
            this.options = param0.readByte();
            this.nametagVisibility = param0.readUtf(40);
            this.collisionRule = param0.readUtf(40);
            this.color = param0.readEnum(ChatFormatting.class);
            this.playerPrefix = param0.readComponent();
            this.playerSuffix = param0.readComponent();
        }

        if (this.method == 0 || this.method == 3 || this.method == 4) {
            int var0 = param0.readVarInt();

            for(int var1 = 0; var1 < var0; ++var1) {
                this.players.add(param0.readUtf(40));
            }
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.name);
        param0.writeByte(this.method);
        if (this.method == 0 || this.method == 2) {
            param0.writeComponent(this.displayName);
            param0.writeByte(this.options);
            param0.writeUtf(this.nametagVisibility);
            param0.writeUtf(this.collisionRule);
            param0.writeEnum(this.color);
            param0.writeComponent(this.playerPrefix);
            param0.writeComponent(this.playerSuffix);
        }

        if (this.method == 0 || this.method == 3 || this.method == 4) {
            param0.writeVarInt(this.players.size());

            for(String var0 : this.players) {
                param0.writeUtf(var0);
            }
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetPlayerTeamPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public String getName() {
        return this.name;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDisplayName() {
        return this.displayName;
    }

    @OnlyIn(Dist.CLIENT)
    public Collection<String> getPlayers() {
        return this.players;
    }

    @OnlyIn(Dist.CLIENT)
    public int getMethod() {
        return this.method;
    }

    @OnlyIn(Dist.CLIENT)
    public int getOptions() {
        return this.options;
    }

    @OnlyIn(Dist.CLIENT)
    public ChatFormatting getColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    public String getNametagVisibility() {
        return this.nametagVisibility;
    }

    @OnlyIn(Dist.CLIENT)
    public String getCollisionRule() {
        return this.collisionRule;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getPlayerPrefix() {
        return this.playerPrefix;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getPlayerSuffix() {
        return this.playerSuffix;
    }
}
