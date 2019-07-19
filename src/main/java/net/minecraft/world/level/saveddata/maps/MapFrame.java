package net.minecraft.world.level.saveddata.maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class MapFrame {
    private final BlockPos pos;
    private final int rotation;
    private final int entityId;

    public MapFrame(BlockPos param0, int param1, int param2) {
        this.pos = param0;
        this.rotation = param1;
        this.entityId = param2;
    }

    public static MapFrame load(CompoundTag param0) {
        BlockPos var0 = NbtUtils.readBlockPos(param0.getCompound("Pos"));
        int var1 = param0.getInt("Rotation");
        int var2 = param0.getInt("EntityId");
        return new MapFrame(var0, var1, var2);
    }

    public CompoundTag save() {
        CompoundTag var0 = new CompoundTag();
        var0.put("Pos", NbtUtils.writeBlockPos(this.pos));
        var0.putInt("Rotation", this.rotation);
        var0.putInt("EntityId", this.entityId);
        return var0;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getId() {
        return frameId(this.pos);
    }

    public static String frameId(BlockPos param0) {
        return "frame-" + param0.getX() + "," + param0.getY() + "," + param0.getZ();
    }
}
