package net.minecraft.world.level.pathfinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Path {
    private final List<Node> nodes;
    @Nullable
    private Path.DebugData debugData;
    private int nextNodeIndex;
    private final BlockPos target;
    private final float distToTarget;
    private final boolean reached;

    public Path(List<Node> param0, BlockPos param1, boolean param2) {
        this.nodes = param0;
        this.target = param1;
        this.distToTarget = param0.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
        this.reached = param2;
    }

    public void advance() {
        ++this.nextNodeIndex;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    @Nullable
    public Node getEndNode() {
        return !this.nodes.isEmpty() ? this.nodes.get(this.nodes.size() - 1) : null;
    }

    public Node getNode(int param0) {
        return this.nodes.get(param0);
    }

    public void truncateNodes(int param0) {
        if (this.nodes.size() > param0) {
            this.nodes.subList(param0, this.nodes.size()).clear();
        }

    }

    public void replaceNode(int param0, Node param1) {
        this.nodes.set(param0, param1);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(int param0) {
        this.nextNodeIndex = param0;
    }

    public Vec3 getEntityPosAtNode(Entity param0, int param1) {
        Node var0 = this.nodes.get(param1);
        double var1 = (double)var0.x + (double)((int)(param0.getBbWidth() + 1.0F)) * 0.5;
        double var2 = (double)var0.y;
        double var3 = (double)var0.z + (double)((int)(param0.getBbWidth() + 1.0F)) * 0.5;
        return new Vec3(var1, var2, var3);
    }

    public BlockPos getNodePos(int param0) {
        return this.nodes.get(param0).asBlockPos();
    }

    public Vec3 getNextEntityPos(Entity param0) {
        return this.getEntityPosAtNode(param0, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return this.nodes.get(this.nextNodeIndex).asBlockPos();
    }

    public Node getNextNode() {
        return this.nodes.get(this.nextNodeIndex);
    }

    @Nullable
    public Node getPreviousNode() {
        return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
    }

    public boolean sameAs(@Nullable Path param0) {
        if (param0 == null) {
            return false;
        } else if (param0.nodes.size() != this.nodes.size()) {
            return false;
        } else {
            for(int var0 = 0; var0 < this.nodes.size(); ++var0) {
                Node var1 = this.nodes.get(var0);
                Node var2 = param0.nodes.get(var0);
                if (var1.x != var2.x || var1.y != var2.y || var1.z != var2.z) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean canReach() {
        return this.reached;
    }

    @VisibleForDebug
    void setDebug(Node[] param0, Node[] param1, Set<Target> param2) {
        this.debugData = new Path.DebugData(param0, param1, param2);
    }

    @Nullable
    public Path.DebugData debugData() {
        return this.debugData;
    }

    public void writeToStream(FriendlyByteBuf param0) {
        if (this.debugData != null && !this.debugData.targetNodes.isEmpty()) {
            param0.writeBoolean(this.reached);
            param0.writeInt(this.nextNodeIndex);
            param0.writeBlockPos(this.target);
            param0.writeCollection(this.nodes, (param0x, param1) -> param1.writeToStream(param0x));
            this.debugData.write(param0);
        }
    }

    public static Path createFromStream(FriendlyByteBuf param0) {
        boolean var0 = param0.readBoolean();
        int var1 = param0.readInt();
        BlockPos var2 = param0.readBlockPos();
        List<Node> var3 = param0.readList(Node::createFromStream);
        Path.DebugData var4 = Path.DebugData.read(param0);
        Path var5 = new Path(var3, var2, var0);
        var5.debugData = var4;
        var5.nextNodeIndex = var1;
        return var5;
    }

    @Override
    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }

    static Node[] readNodeArray(FriendlyByteBuf param0) {
        Node[] var0 = new Node[param0.readVarInt()];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            var0[var1] = Node.createFromStream(param0);
        }

        return var0;
    }

    static void writeNodeArray(FriendlyByteBuf param0, Node[] param1) {
        param0.writeVarInt(param1.length);

        for(Node var0 : param1) {
            var0.writeToStream(param0);
        }

    }

    public Path copy() {
        Path var0 = new Path(this.nodes, this.target, this.reached);
        var0.debugData = this.debugData;
        var0.nextNodeIndex = this.nextNodeIndex;
        return var0;
    }

    public static record DebugData(Node[] openSet, Node[] closedSet, Set<Target> targetNodes) {
        public void write(FriendlyByteBuf param0) {
            param0.writeCollection(this.targetNodes, (param0x, param1) -> param1.writeToStream(param0x));
            Path.writeNodeArray(param0, this.openSet);
            Path.writeNodeArray(param0, this.closedSet);
        }

        public static Path.DebugData read(FriendlyByteBuf param0) {
            HashSet<Target> var0 = param0.readCollection(HashSet::new, Target::createFromStream);
            Node[] var1 = Path.readNodeArray(param0);
            Node[] var2 = Path.readNodeArray(param0);
            return new Path.DebugData(var1, var2, var0);
        }
    }
}
