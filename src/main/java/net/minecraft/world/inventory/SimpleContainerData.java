package net.minecraft.world.inventory;

public class SimpleContainerData implements ContainerData {
    private final int[] ints;

    public SimpleContainerData(int param0) {
        this.ints = new int[param0];
    }

    @Override
    public int get(int param0) {
        return this.ints[param0];
    }

    @Override
    public void set(int param0, int param1) {
        this.ints[param0] = param1;
    }

    @Override
    public int getCount() {
        return this.ints.length;
    }
}
