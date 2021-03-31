package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.PlayerTeam;

public class ClientboundSetPlayerTeamPacket implements Packet<ClientGamePacketListener> {
    private static final int METHOD_ADD = 0;
    private static final int METHOD_REMOVE = 1;
    private static final int METHOD_CHANGE = 2;
    private static final int METHOD_JOIN = 3;
    private static final int METHOD_LEAVE = 4;
    private static final int MAX_VISIBILITY_LENGTH = 40;
    private static final int MAX_COLLISION_LENGTH = 40;
    private final int method;
    private final String name;
    private final Collection<String> players;
    private final Optional<ClientboundSetPlayerTeamPacket.Parameters> parameters;

    private ClientboundSetPlayerTeamPacket(String param0, int param1, Optional<ClientboundSetPlayerTeamPacket.Parameters> param2, Collection<String> param3) {
        this.name = param0;
        this.method = param1;
        this.parameters = param2;
        this.players = ImmutableList.copyOf(param3);
    }

    public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam param0, boolean param1) {
        return new ClientboundSetPlayerTeamPacket(
            param0.getName(),
            param1 ? 0 : 2,
            Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(param0)),
            (Collection<String>)(param1 ? param0.getPlayers() : ImmutableList.of())
        );
    }

    public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam param0) {
        return new ClientboundSetPlayerTeamPacket(param0.getName(), 1, Optional.empty(), ImmutableList.of());
    }

    public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam param0, String param1, ClientboundSetPlayerTeamPacket.Action param2) {
        return new ClientboundSetPlayerTeamPacket(
            param0.getName(), param2 == ClientboundSetPlayerTeamPacket.Action.ADD ? 3 : 4, Optional.empty(), ImmutableList.of(param1)
        );
    }

    public ClientboundSetPlayerTeamPacket(FriendlyByteBuf param0) {
        this.name = param0.readUtf(16);
        this.method = param0.readByte();
        if (shouldHaveParameters(this.method)) {
            this.parameters = Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(param0));
        } else {
            this.parameters = Optional.empty();
        }

        if (shouldHavePlayerList(this.method)) {
            this.players = param0.readList(FriendlyByteBuf::readUtf);
        } else {
            this.players = ImmutableList.of();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.name);
        param0.writeByte(this.method);
        if (shouldHaveParameters(this.method)) {
            this.parameters.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.method)).write(param0);
        }

        if (shouldHavePlayerList(this.method)) {
            param0.writeCollection(this.players, FriendlyByteBuf::writeUtf);
        }

    }

    private static boolean shouldHavePlayerList(int param0) {
        return param0 == 0 || param0 == 3 || param0 == 4;
    }

    private static boolean shouldHaveParameters(int param0) {
        return param0 == 0 || param0 == 2;
    }

    @Nullable
    public ClientboundSetPlayerTeamPacket.Action getPlayerAction() {
        switch(this.method) {
            case 0:
            case 3:
                return ClientboundSetPlayerTeamPacket.Action.ADD;
            case 1:
            case 2:
            default:
                return null;
            case 4:
                return ClientboundSetPlayerTeamPacket.Action.REMOVE;
        }
    }

    @Nullable
    public ClientboundSetPlayerTeamPacket.Action getTeamAction() {
        switch(this.method) {
            case 0:
                return ClientboundSetPlayerTeamPacket.Action.ADD;
            case 1:
                return ClientboundSetPlayerTeamPacket.Action.REMOVE;
            default:
                return null;
        }
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetPlayerTeamPacket(this);
    }

    public String getName() {
        return this.name;
    }

    public Collection<String> getPlayers() {
        return this.players;
    }

    public Optional<ClientboundSetPlayerTeamPacket.Parameters> getParameters() {
        return this.parameters;
    }

    public static enum Action {
        ADD,
        REMOVE;
    }

    public static class Parameters {
        private final Component displayName;
        private final Component playerPrefix;
        private final Component playerSuffix;
        private final String nametagVisibility;
        private final String collisionRule;
        private final ChatFormatting color;
        private final int options;

        public Parameters(PlayerTeam param0) {
            this.displayName = param0.getDisplayName();
            this.options = param0.packOptions();
            this.nametagVisibility = param0.getNameTagVisibility().name;
            this.collisionRule = param0.getCollisionRule().name;
            this.color = param0.getColor();
            this.playerPrefix = param0.getPlayerPrefix();
            this.playerSuffix = param0.getPlayerSuffix();
        }

        public Parameters(FriendlyByteBuf param0) {
            this.displayName = param0.readComponent();
            this.options = param0.readByte();
            this.nametagVisibility = param0.readUtf(40);
            this.collisionRule = param0.readUtf(40);
            this.color = param0.readEnum(ChatFormatting.class);
            this.playerPrefix = param0.readComponent();
            this.playerSuffix = param0.readComponent();
        }

        public Component getDisplayName() {
            return this.displayName;
        }

        public int getOptions() {
            return this.options;
        }

        public ChatFormatting getColor() {
            return this.color;
        }

        public String getNametagVisibility() {
            return this.nametagVisibility;
        }

        public String getCollisionRule() {
            return this.collisionRule;
        }

        public Component getPlayerPrefix() {
            return this.playerPrefix;
        }

        public Component getPlayerSuffix() {
            return this.playerSuffix;
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeComponent(this.displayName);
            param0.writeByte(this.options);
            param0.writeUtf(this.nametagVisibility);
            param0.writeUtf(this.collisionRule);
            param0.writeEnum(this.color);
            param0.writeComponent(this.playerPrefix);
            param0.writeComponent(this.playerSuffix);
        }
    }
}
