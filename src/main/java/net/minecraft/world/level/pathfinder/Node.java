package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Node {
    public final int x;
    public final int y;
    public final int z;
    private final int hash;
    public int heapIdx = -1;
    public float g;
    public float h;
    public float f;
    @Nullable
    public Node cameFrom;
    public boolean closed;
    public float walkedDistance;
    public float costMalus;
    public BlockPathTypes type = BlockPathTypes.BLOCKED;

    public Node(int param0, int param1, int param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.hash = createHash(param0, param1, param2);
    }

    public Node cloneAndMove(int param0, int param1, int param2) {
        Node var0 = new Node(param0, param1, param2);
        var0.heapIdx = this.heapIdx;
        var0.g = this.g;
        var0.h = this.h;
        var0.f = this.f;
        var0.cameFrom = this.cameFrom;
        var0.closed = this.closed;
        var0.walkedDistance = this.walkedDistance;
        var0.costMalus = this.costMalus;
        var0.type = this.type;
        return var0;
    }

    public static int createHash(int param0, int param1, int param2) {
        return param1 & 0xFF | (param0 & 32767) << 8 | (param2 & 32767) << 24 | (param0 < 0 ? Integer.MIN_VALUE : 0) | (param2 < 0 ? 32768 : 0);
    }

    public float distanceTo(Node param0) {
        float var0 = (float)(param0.x - this.x);
        float var1 = (float)(param0.y - this.y);
        float var2 = (float)(param0.z - this.z);
        return Mth.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
    }

    public float distanceTo(BlockPos param0) {
        float var0 = (float)(param0.getX() - this.x);
        float var1 = (float)(param0.getY() - this.y);
        float var2 = (float)(param0.getZ() - this.z);
        return Mth.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
    }

    public float distanceToSqr(Node param0) {
        float var0 = (float)(param0.x - this.x);
        float var1 = (float)(param0.y - this.y);
        float var2 = (float)(param0.z - this.z);
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public float distanceToSqr(BlockPos param0) {
        float var0 = (float)(param0.getX() - this.x);
        float var1 = (float)(param0.getY() - this.y);
        float var2 = (float)(param0.getZ() - this.z);
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public float distanceManhattan(Node param0) {
        float var0 = (float)Math.abs(param0.x - this.x);
        float var1 = (float)Math.abs(param0.y - this.y);
        float var2 = (float)Math.abs(param0.z - this.z);
        return var0 + var1 + var2;
    }

    public float distanceManhattan(BlockPos param0) {
        float var0 = (float)Math.abs(param0.getX() - this.x);
        float var1 = (float)Math.abs(param0.getY() - this.y);
        float var2 = (float)Math.abs(param0.getZ() - this.z);
        return var0 + var1 + var2;
    }

    public BlockPos asBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vec3 asVec3() {
        return new Vec3((double)this.x, (double)this.y, (double)this.z);
    }

    @Override
    public boolean equals(Object param0) {
        if (!(param0 instanceof Node)) {
            return false;
        } else {
            Node var0 = (Node)param0;
            return this.hash == var0.hash && this.x == var0.x && this.y == var0.y && this.z == var0.z;
        }
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    public boolean inOpenSet() {
        return this.heapIdx >= 0;
    }

    @Override
    public String toString() {
        return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
    }

    public void writeToStream(FriendlyByteBuf param0) {
        param0.writeInt(this.x);
        param0.writeInt(this.y);
        param0.writeInt(this.z);
        param0.writeFloat(this.walkedDistance);
        param0.writeFloat(this.costMalus);
        param0.writeBoolean(this.closed);
        param0.writeInt(this.type.ordinal());
        param0.writeFloat(this.f);
    }

    public static Node createFromStream(FriendlyByteBuf param0) {
        Node var0 = new Node(param0.readInt(), param0.readInt(), param0.readInt());
        var0.walkedDistance = param0.readFloat();
        var0.costMalus = param0.readFloat();
        var0.closed = param0.readBoolean();
        var0.type = BlockPathTypes.values()[param0.readInt()];
        var0.f = param0.readFloat();
        return var0;
    }
}
