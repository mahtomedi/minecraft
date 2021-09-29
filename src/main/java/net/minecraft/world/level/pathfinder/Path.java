package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
    private Node[] openSet = new Node[0];
    private Node[] closedSet = new Node[0];
    @Nullable
    private Set<Target> targetNodes;
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
        this.openSet = param0;
        this.closedSet = param1;
        this.targetNodes = param2;
    }

    @VisibleForDebug
    public Node[] getOpenSet() {
        return this.openSet;
    }

    @VisibleForDebug
    public Node[] getClosedSet() {
        return this.closedSet;
    }

    public void writeToStream(FriendlyByteBuf param0) {
        if (this.targetNodes != null && !this.targetNodes.isEmpty()) {
            param0.writeBoolean(this.reached);
            param0.writeInt(this.nextNodeIndex);
            param0.writeInt(this.targetNodes.size());
            this.targetNodes.forEach(param1 -> param1.writeToStream(param0));
            param0.writeInt(this.target.getX());
            param0.writeInt(this.target.getY());
            param0.writeInt(this.target.getZ());
            param0.writeInt(this.nodes.size());

            for(Node var0 : this.nodes) {
                var0.writeToStream(param0);
            }

            param0.writeInt(this.openSet.length);

            for(Node var1 : this.openSet) {
                var1.writeToStream(param0);
            }

            param0.writeInt(this.closedSet.length);

            for(Node var2 : this.closedSet) {
                var2.writeToStream(param0);
            }

        }
    }

    public static Path createFromStream(FriendlyByteBuf param0) {
        boolean var0 = param0.readBoolean();
        int var1 = param0.readInt();
        int var2 = param0.readInt();
        Set<Target> var3 = Sets.newHashSet();

        for(int var4 = 0; var4 < var2; ++var4) {
            var3.add(Target.createFromStream(param0));
        }

        BlockPos var5 = new BlockPos(param0.readInt(), param0.readInt(), param0.readInt());
        List<Node> var6 = Lists.newArrayList();
        int var7 = param0.readInt();

        for(int var8 = 0; var8 < var7; ++var8) {
            var6.add(Node.createFromStream(param0));
        }

        Node[] var9 = new Node[param0.readInt()];

        for(int var10 = 0; var10 < var9.length; ++var10) {
            var9[var10] = Node.createFromStream(param0);
        }

        Node[] var11 = new Node[param0.readInt()];

        for(int var12 = 0; var12 < var11.length; ++var12) {
            var11[var12] = Node.createFromStream(param0);
        }

        Path var13 = new Path(var6, var5, var0);
        var13.openSet = var9;
        var13.closedSet = var11;
        var13.targetNodes = var3;
        var13.nextNodeIndex = var1;
        return var13;
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
}
