package net.minecraft.world.level.pathfinder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Target extends Node {
    private float bestHeuristic = Float.MAX_VALUE;
    private Node bestNode;
    private boolean reached;

    public Target(Node param0) {
        super(param0.x, param0.y, param0.z);
    }

    @OnlyIn(Dist.CLIENT)
    public Target(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    public void updateBest(float param0, Node param1) {
        if (param0 < this.bestHeuristic) {
            this.bestHeuristic = param0;
            this.bestNode = param1;
        }

    }

    public Node getBestNode() {
        return this.bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    public boolean isReached() {
        return this.reached;
    }

    @OnlyIn(Dist.CLIENT)
    public static Target createFromStream(FriendlyByteBuf param0) {
        Target var0 = new Target(param0.readInt(), param0.readInt(), param0.readInt());
        var0.walkedDistance = param0.readFloat();
        var0.costMalus = param0.readFloat();
        var0.closed = param0.readBoolean();
        var0.type = BlockPathTypes.values()[param0.readInt()];
        var0.f = param0.readFloat();
        return var0;
    }
}
