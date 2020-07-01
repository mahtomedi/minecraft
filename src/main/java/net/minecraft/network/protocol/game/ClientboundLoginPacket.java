package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
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
    private int playerId;
    private long seed;
    private boolean hardcore;
    private GameType gameType;
    private GameType previousGameType;
    private Set<ResourceKey<Level>> levels;
    private RegistryAccess.RegistryHolder registryHolder;
    private ResourceKey<DimensionType> dimensionType;
    private ResourceKey<Level> dimension;
    private int maxPlayers;
    private int chunkRadius;
    private boolean reducedDebugInfo;
    private boolean showDeathScreen;
    private boolean isDebug;
    private boolean isFlat;

    public ClientboundLoginPacket() {
    }

    public ClientboundLoginPacket(
        int param0,
        GameType param1,
        GameType param2,
        long param3,
        boolean param4,
        Set<ResourceKey<Level>> param5,
        RegistryAccess.RegistryHolder param6,
        ResourceKey<DimensionType> param7,
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

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.playerId = param0.readInt();
        this.hardcore = param0.readBoolean();
        this.gameType = GameType.byId(param0.readByte());
        this.previousGameType = GameType.byId(param0.readByte());
        int var0 = param0.readVarInt();
        this.levels = Sets.newHashSet();

        for(int var1 = 0; var1 < var0; ++var1) {
            this.levels.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.readResourceLocation()));
        }

        this.registryHolder = param0.readWithCodec(RegistryAccess.RegistryHolder.CODEC);
        this.dimensionType = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, param0.readResourceLocation());
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
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.playerId);
        param0.writeBoolean(this.hardcore);
        param0.writeByte(this.gameType.getId());
        param0.writeByte(this.previousGameType.getId());
        param0.writeVarInt(this.levels.size());

        for(ResourceKey<Level> var0 : this.levels) {
            param0.writeResourceLocation(var0.location());
        }

        param0.writeWithCodec(RegistryAccess.RegistryHolder.CODEC, this.registryHolder);
        param0.writeResourceLocation(this.dimensionType.location());
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
    public ResourceKey<DimensionType> getDimensionType() {
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
