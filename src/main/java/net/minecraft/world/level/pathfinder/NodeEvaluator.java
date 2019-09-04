package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

public abstract class NodeEvaluator {
    protected PathNavigationRegion level;
    protected Mob mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors;
    protected boolean canOpenDoors;
    protected boolean canFloat;

    public void prepare(PathNavigationRegion param0, Mob param1) {
        this.level = param0;
        this.mob = param1;
        this.nodes.clear();
        this.entityWidth = Mth.floor(param1.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(param1.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(param1.getBbWidth() + 1.0F);
    }

    public void done() {
        this.level = null;
        this.mob = null;
    }

    protected Node getNode(int param0, int param1, int param2) {
        return this.nodes.computeIfAbsent(Node.createHash(param0, param1, param2), param3 -> new Node(param0, param1, param2));
    }

    public abstract Node getStart();

    public abstract Target getGoal(double var1, double var3, double var5);

    public abstract int getNeighbors(Node[] var1, Node var2);

    public abstract BlockPathTypes getBlockPathType(
        BlockGetter var1, int var2, int var3, int var4, Mob var5, int var6, int var7, int var8, boolean var9, boolean var10
    );

    public abstract BlockPathTypes getBlockPathType(BlockGetter var1, int var2, int var3, int var4);

    public void setCanPassDoors(boolean param0) {
        this.canPassDoors = param0;
    }

    public void setCanOpenDoors(boolean param0) {
        this.canOpenDoors = param0;
    }

    public void setCanFloat(boolean param0) {
        this.canFloat = param0;
    }

    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }
}
