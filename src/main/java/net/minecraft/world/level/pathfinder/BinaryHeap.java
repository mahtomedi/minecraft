package net.minecraft.world.level.pathfinder;

import java.util.Arrays;

public class BinaryHeap {
    private Node[] heap = new Node[128];
    private int size;

    public Node insert(Node param0) {
        if (param0.heapIdx >= 0) {
            throw new IllegalStateException("OW KNOWS!");
        } else {
            if (this.size == this.heap.length) {
                Node[] var0 = new Node[this.size << 1];
                System.arraycopy(this.heap, 0, var0, 0, this.size);
                this.heap = var0;
            }

            this.heap[this.size] = param0;
            param0.heapIdx = this.size;
            this.upHeap(this.size++);
            return param0;
        }
    }

    public void clear() {
        this.size = 0;
    }

    public Node peek() {
        return this.heap[0];
    }

    public Node pop() {
        Node var0 = this.heap[0];
        this.heap[0] = this.heap[--this.size];
        this.heap[this.size] = null;
        if (this.size > 0) {
            this.downHeap(0);
        }

        var0.heapIdx = -1;
        return var0;
    }

    public void remove(Node param0) {
        this.heap[param0.heapIdx] = this.heap[--this.size];
        this.heap[this.size] = null;
        if (this.size > param0.heapIdx) {
            if (this.heap[param0.heapIdx].f < param0.f) {
                this.upHeap(param0.heapIdx);
            } else {
                this.downHeap(param0.heapIdx);
            }
        }

        param0.heapIdx = -1;
    }

    public void changeCost(Node param0, float param1) {
        float var0 = param0.f;
        param0.f = param1;
        if (param1 < var0) {
            this.upHeap(param0.heapIdx);
        } else {
            this.downHeap(param0.heapIdx);
        }

    }

    public int size() {
        return this.size;
    }

    private void upHeap(int param0) {
        Node var0 = this.heap[param0];

        int var2;
        for(float var1 = var0.f; param0 > 0; param0 = var2) {
            var2 = param0 - 1 >> 1;
            Node var3 = this.heap[var2];
            if (!(var1 < var3.f)) {
                break;
            }

            this.heap[param0] = var3;
            var3.heapIdx = param0;
        }

        this.heap[param0] = var0;
        var0.heapIdx = param0;
    }

    private void downHeap(int param0) {
        Node var0 = this.heap[param0];
        float var1 = var0.f;

        while(true) {
            int var2 = 1 + (param0 << 1);
            int var3 = var2 + 1;
            if (var2 >= this.size) {
                break;
            }

            Node var4 = this.heap[var2];
            float var5 = var4.f;
            Node var6;
            float var7;
            if (var3 >= this.size) {
                var6 = null;
                var7 = Float.POSITIVE_INFINITY;
            } else {
                var6 = this.heap[var3];
                var7 = var6.f;
            }

            if (var5 < var7) {
                if (!(var5 < var1)) {
                    break;
                }

                this.heap[param0] = var4;
                var4.heapIdx = param0;
                param0 = var2;
            } else {
                if (!(var7 < var1)) {
                    break;
                }

                this.heap[param0] = var6;
                var6.heapIdx = param0;
                param0 = var3;
            }
        }

        this.heap[param0] = var0;
        var0.heapIdx = param0;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public Node[] getHeap() {
        return Arrays.copyOf(this.heap, this.size);
    }
}
