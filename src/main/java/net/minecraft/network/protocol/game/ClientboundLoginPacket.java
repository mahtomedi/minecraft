package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
    private final int playerId;
    private final long seed;
    private final boolean hardcore;
    private final GameType gameType;
    @Nullable
    private final GameType previousGameType;
    private final Set<ResourceKey<Level>> levels;
    private final RegistryAccess.RegistryHolder registryHolder;
    private final DimensionType dimensionType;
    private final ResourceKey<Level> dimension;
    private final int maxPlayers;
    private final int chunkRadius;
    private final boolean reducedDebugInfo;
    private final boolean showDeathScreen;
    private final boolean isDebug;
    private final boolean isFlat;

    public ClientboundLoginPacket(
        int param0,
        GameType param1,
        @Nullable GameType param2,
        long param3,
        boolean param4,
        Set<ResourceKey<Level>> param5,
        RegistryAccess.RegistryHolder param6,
        DimensionType param7,
        ResourceKey<Level> param8,
        int param9,
        int param10,
        boolean param11,
        boolean param12,
        boolean param13,
        boolean param14
    ) {
        this.playerId = param0;
        this.levels = param5;
        this.registryHolder = param6;
        this.dimensionType = param7;
        this.dimension = param8;
        this.seed = param3;
        this.gameType = param1;
        this.previousGameType = param2;
        this.maxPlayers = param9;
        this.hardcore = param4;
        this.chunkRadius = param10;
        this.reducedDebugInfo = param11;
        this.showDeathScreen = param12;
        this.isDebug = param13;
        this.isFlat = param14;
    }

    public ClientboundLoginPacket(FriendlyByteBuf param0) {
        this.playerId = param0.readInt();
        this.hardcore = param0.readBoolean();
        this.gameType = GameType.byId(param0.readByte());
        this.previousGameType = GameType.byNullableId(param0.readByte());
        this.levels = param0.readCollection(
            Sets::newHashSetWithExpectedSize, param0x -> ResourceKey.create(Registry.DIMENSION_REGISTRY, param0x.readResourceLocation())
        );
        this.registryHolder = param0.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC);
        this.dimensionType = param0.readWithCodec(DimensionType.CODEC).get();
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.readResourceLocation());
        this.seed = param0.readLong();
        this.maxPlayers = param0.readVarInt();
        this.chunkRadius = param0.readVarInt();
        this.reducedDebugInfo = param0.readBoolean();
        this.showDeathScreen = param0.readBoolean();
        this.isDebug = param0.readBoolean();
        this.isFlat = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.playerId);
        param0.writeBoolean(this.hardcore);
        param0.writeByte(this.gameType.getId());
        param0.writeByte(GameType.getNullableId(this.previousGameType));
        param0.writeCollection(this.levels, (param0x, param1) -> param0x.writeResourceLocation(param1.location()));
        param0.writeWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC, this.registryHolder);
        param0.writeWithCodec(DimensionType.CODEC, () -> this.dimensionType);
        param0.writeResourceLocation(this.dimension.location());
        param0.writeLong(this.seed);
        param0.writeVarInt(this.maxPlayers);
        param0.writeVarInt(this.chunkRadius);
        param0.writeBoolean(this.reducedDebugInfo);
        param0.writeBoolean(this.showDeathScreen);
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLogin(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getPlayerId() {
        return this.playerId;
    }

    @OnlyIn(Dist.CLIENT)
    public long getSeed() {
        return this.seed;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isHardcore() {
        return this.hardcore;
    }

    @OnlyIn(Dist.CLIENT)
    public GameType getGameType() {
        return this.gameType;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public GameType getPreviousGameType() {
        return this.previousGameType;
    }

    @OnlyIn(Dist.CLIENT)
    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    @OnlyIn(Dist.CLIENT)
    public RegistryAccess registryAccess() {
        return this.registryHolder;
    }

    @OnlyIn(Dist.CLIENT)
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    @OnlyIn(Dist.CLIENT)
    public int getChunkRadius() {
        return this.chunkRadius;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldShowDeathScreen() {
        return this.showDeathScreen;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isDebug() {
        return this.isDebug;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFlat() {
        return this.isFlat;
    }
}
