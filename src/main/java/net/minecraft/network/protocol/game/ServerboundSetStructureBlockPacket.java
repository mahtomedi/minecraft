package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class ServerboundSetStructureBlockPacket implements Packet<ServerGamePacketListener> {
    private static final int FLAG_IGNORE_ENTITIES = 1;
    private static final int FLAG_SHOW_AIR = 2;
    private static final int FLAG_SHOW_BOUNDING_BOX = 4;
    private final BlockPos pos;
    private final StructureBlockEntity.UpdateType updateType;
    private final StructureMode mode;
    private final String name;
    private final BlockPos offset;
    private final Vec3i size;
    private final Mirror mirror;
    private final Rotation rotation;
    private final String data;
    private final boolean ignoreEntities;
    private final boolean showAir;
    private final boolean showBoundingBox;
    private final float integrity;
    private final long seed;

    public ServerboundSetStructureBlockPacket(
        BlockPos param0,
        StructureBlockEntity.UpdateType param1,
        StructureMode param2,
        String param3,
        BlockPos param4,
        Vec3i param5,
        Mirror param6,
        Rotation param7,
        String param8,
        boolean param9,
        boolean param10,
        boolean param11,
        float param12,
        long param13
    ) {
        this.pos = param0;
        this.updateType = param1;
        this.mode = param2;
        this.name = param3;
        this.offset = param4;
        this.size = param5;
        this.mirror = param6;
        this.rotation = param7;
        this.data = param8;
        this.ignoreEntities = param9;
        this.showAir = param10;
        this.showBoundingBox = param11;
        this.integrity = param12;
        this.seed = param13;
    }

    public ServerboundSetStructureBlockPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.updateType = param0.readEnum(StructureBlockEntity.UpdateType.class);
        this.mode = param0.readEnum(StructureMode.class);
        this.name = param0.readUtf();
        int var0 = 48;
        this.offset = new BlockPos(Mth.clamp(param0.readByte(), -48, 48), Mth.clamp(param0.readByte(), -48, 48), Mth.clamp(param0.readByte(), -48, 48));
        int var1 = 48;
        this.size = new Vec3i(Mth.clamp(param0.readByte(), 0, 48), Mth.clamp(param0.readByte(), 0, 48), Mth.clamp(param0.readByte(), 0, 48));
        this.mirror = param0.readEnum(Mirror.class);
        this.rotation = param0.readEnum(Rotation.class);
        this.data = param0.readUtf(128);
        this.integrity = Mth.clamp(param0.readFloat(), 0.0F, 1.0F);
        this.seed = param0.readVarLong();
        int var2 = param0.readByte();
        this.ignoreEntities = (var2 & 1) != 0;
        this.showAir = (var2 & 2) != 0;
        this.showBoundingBox = (var2 & 4) != 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeEnum(this.updateType);
        param0.writeEnum(this.mode);
        param0.writeUtf(this.name);
        param0.writeByte(this.offset.getX());
        param0.writeByte(this.offset.getY());
        param0.writeByte(this.offset.getZ());
        param0.writeByte(this.size.getX());
        param0.writeByte(this.size.getY());
        param0.writeByte(this.size.getZ());
        param0.writeEnum(this.mirror);
        param0.writeEnum(this.rotation);
        param0.writeUtf(this.data);
        param0.writeFloat(this.integrity);
        param0.writeVarLong(this.seed);
        int var0 = 0;
        if (this.ignoreEntities) {
            var0 |= 1;
        }

        if (this.showAir) {
            var0 |= 2;
        }

        if (this.showBoundingBox) {
            var0 |= 4;
        }

        param0.writeByte(var0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetStructureBlock(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public StructureBlockEntity.UpdateType getUpdateType() {
        return this.updateType;
    }

    public StructureMode getMode() {
        return this.mode;
    }

    public String getName() {
        return this.name;
    }

    public BlockPos getOffset() {
        return this.offset;
    }

    public Vec3i getSize() {
        return this.size;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public String getData() {
        return this.data;
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public boolean isShowAir() {
        return this.showAir;
    }

    public boolean isShowBoundingBox() {
        return this.showBoundingBox;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public long getSeed() {
        return this.seed;
    }
}
