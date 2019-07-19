package net.minecraft.world.inventory;

public abstract class DataSlot {
    private int prevValue;

    public static DataSlot forContainer(final ContainerData param0, final int param1) {
        return new DataSlot() {
            @Override
            public int get() {
                return param0.get(param1);
            }

            @Override
            public void set(int param0x) {
                param0.set(param1, param0);
            }
        };
    }

    public static DataSlot shared(final int[] param0, final int param1) {
        return new DataSlot() {
            @Override
            public int get() {
                return param0[param1];
            }

            @Override
            public void set(int param0x) {
                param0[param1] = param0;
            }
        };
    }

    public static DataSlot standalone() {
        return new DataSlot() {
            private int value;

            @Override
            public int get() {
                return this.value;
            }

            @Override
            public void set(int param0) {
                this.value = param0;
            }
        };
    }

    public abstract int get();

    public abstract void set(int var1);

    public boolean checkAndClearUpdateFlag() {
        int var0 = this.get();
        boolean var1 = var0 != this.prevValue;
        this.prevValue = var0;
        return var1;
    }
}
