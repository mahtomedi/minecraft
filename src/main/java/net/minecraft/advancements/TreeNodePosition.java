package net.minecraft.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;

public class TreeNodePosition {
    private final Advancement advancement;
    @Nullable
    private final TreeNodePosition parent;
    @Nullable
    private final TreeNodePosition previousSibling;
    private final int childIndex;
    private final List<TreeNodePosition> children = Lists.newArrayList();
    private TreeNodePosition ancestor;
    @Nullable
    private TreeNodePosition thread;
    private int x;
    private float y;
    private float mod;
    private float change;
    private float shift;

    public TreeNodePosition(Advancement param0, @Nullable TreeNodePosition param1, @Nullable TreeNodePosition param2, int param3, int param4) {
        if (param0.getDisplay() == null) {
            throw new IllegalArgumentException("Can't position an invisible advancement!");
        } else {
            this.advancement = param0;
            this.parent = param1;
            this.previousSibling = param2;
            this.childIndex = param3;
            this.ancestor = this;
            this.x = param4;
            this.y = -1.0F;
            TreeNodePosition var0 = null;

            for(Advancement var1 : param0.getChildren()) {
                var0 = this.addChild(var1, var0);
            }

        }
    }

    @Nullable
    private TreeNodePosition addChild(Advancement param0, @Nullable TreeNodePosition param1) {
        if (param0.getDisplay() != null) {
            param1 = new TreeNodePosition(param0, this, param1, this.children.size() + 1, this.x + 1);
            this.children.add(param1);
        } else {
            for(Advancement var0 : param0.getChildren()) {
                param1 = this.addChild(var0, param1);
            }
        }

        return param1;
    }

    private void firstWalk() {
        if (this.children.isEmpty()) {
            if (this.previousSibling != null) {
                this.y = this.previousSibling.y + 1.0F;
            } else {
                this.y = 0.0F;
            }

        } else {
            TreeNodePosition var0 = null;

            for(TreeNodePosition var1 : this.children) {
                var1.firstWalk();
                var0 = var1.apportion(var0 == null ? var1 : var0);
            }

            this.executeShifts();
            float var2 = (this.children.get(0).y + this.children.get(this.children.size() - 1).y) / 2.0F;
            if (this.previousSibling != null) {
                this.y = this.previousSibling.y + 1.0F;
                this.mod = this.y - var2;
            } else {
                this.y = var2;
            }

        }
    }

    private float secondWalk(float param0, int param1, float param2) {
        this.y += param0;
        this.x = param1;
        if (this.y < param2) {
            param2 = this.y;
        }

        for(TreeNodePosition var0 : this.children) {
            param2 = var0.secondWalk(param0 + this.mod, param1 + 1, param2);
        }

        return param2;
    }

    private void thirdWalk(float param0) {
        this.y += param0;

        for(TreeNodePosition var0 : this.children) {
            var0.thirdWalk(param0);
        }

    }

    private void executeShifts() {
        float var0 = 0.0F;
        float var1 = 0.0F;

        for(int var2 = this.children.size() - 1; var2 >= 0; --var2) {
            TreeNodePosition var3 = this.children.get(var2);
            var3.y += var0;
            var3.mod += var0;
            var1 += var3.change;
            var0 += var3.shift + var1;
        }

    }

    @Nullable
    private TreeNodePosition previousOrThread() {
        if (this.thread != null) {
            return this.thread;
        } else {
            return !this.children.isEmpty() ? this.children.get(0) : null;
        }
    }

    @Nullable
    private TreeNodePosition nextOrThread() {
        if (this.thread != null) {
            return this.thread;
        } else {
            return !this.children.isEmpty() ? this.children.get(this.children.size() - 1) : null;
        }
    }

    private TreeNodePosition apportion(TreeNodePosition param0) {
        if (this.previousSibling == null) {
            return param0;
        } else {
            TreeNodePosition var0 = this;
            TreeNodePosition var1 = this;
            TreeNodePosition var2 = this.previousSibling;
            TreeNodePosition var3 = this.parent.children.get(0);
            float var4 = this.mod;
            float var5 = this.mod;
            float var6 = var2.mod;

            float var7;
            for(var7 = var3.mod; var2.nextOrThread() != null && var0.previousOrThread() != null; var5 += var1.mod) {
                var2 = var2.nextOrThread();
                var0 = var0.previousOrThread();
                var3 = var3.previousOrThread();
                var1 = var1.nextOrThread();
                var1.ancestor = this;
                float var8 = var2.y + var6 - (var0.y + var4) + 1.0F;
                if (var8 > 0.0F) {
                    var2.getAncestor(this, param0).moveSubtree(this, var8);
                    var4 += var8;
                    var5 += var8;
                }

                var6 += var2.mod;
                var4 += var0.mod;
                var7 += var3.mod;
            }

            if (var2.nextOrThread() != null && var1.nextOrThread() == null) {
                var1.thread = var2.nextOrThread();
                var1.mod += var6 - var5;
            } else {
                if (var0.previousOrThread() != null && var3.previousOrThread() == null) {
                    var3.thread = var0.previousOrThread();
                    var3.mod += var4 - var7;
                }

                param0 = this;
            }

            return param0;
        }
    }

    private void moveSubtree(TreeNodePosition param0, float param1) {
        float var0 = (float)(param0.childIndex - this.childIndex);
        if (var0 != 0.0F) {
            param0.change -= param1 / var0;
            this.change += param1 / var0;
        }

        param0.shift += param1;
        param0.y += param1;
        param0.mod += param1;
    }

    private TreeNodePosition getAncestor(TreeNodePosition param0, TreeNodePosition param1) {
        return this.ancestor != null && param0.parent.children.contains(this.ancestor) ? this.ancestor : param1;
    }

    private void finalizePosition() {
        if (this.advancement.getDisplay() != null) {
            this.advancement.getDisplay().setLocation((float)this.x, this.y);
        }

        if (!this.children.isEmpty()) {
            for(TreeNodePosition var0 : this.children) {
                var0.finalizePosition();
            }
        }

    }

    public static void run(Advancement param0) {
        if (param0.getDisplay() == null) {
            throw new IllegalArgumentException("Can't position children of an invisible root!");
        } else {
            TreeNodePosition var0 = new TreeNodePosition(param0, null, null, 1, 0);
            var0.firstWalk();
            float var1 = var0.secondWalk(0.0F, 0, var0.y);
            if (var1 < 0.0F) {
                var0.thirdWalk(-var1);
            }

            var0.finalizePosition();
        }
    }
}
