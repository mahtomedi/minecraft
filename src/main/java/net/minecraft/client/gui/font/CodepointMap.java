package net.minecraft.client.gui.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CodepointMap<T> {
    private static final int BLOCK_BITS = 8;
    private static final int BLOCK_SIZE = 256;
    private static final int IN_BLOCK_MASK = 255;
    private static final int MAX_BLOCK = 4351;
    private static final int BLOCK_COUNT = 4352;
    private final T[] empty;
    private final T[][] blockMap;
    private final IntFunction<T[]> blockConstructor;

    public CodepointMap(IntFunction<T[]> param0, IntFunction<T[][]> param1) {
        this.empty = (T[])((Object[])param0.apply(256));
        this.blockMap = (T[][])((Object[][])param1.apply(4352));
        Arrays.fill(this.blockMap, this.empty);
        this.blockConstructor = param0;
    }

    public void clear() {
        Arrays.fill(this.blockMap, this.empty);
    }

    @Nullable
    public T get(int param0) {
        int var0 = param0 >> 8;
        int var1 = param0 & 0xFF;
        return this.blockMap[var0][var1];
    }

    @Nullable
    public T put(int param0, T param1) {
        int var0 = param0 >> 8;
        int var1 = param0 & 0xFF;
        T[] var2 = this.blockMap[var0];
        if (var2 == this.empty) {
            var2 = (T[])((Object[])this.blockConstructor.apply(256));
            this.blockMap[var0] = var2;
            var2[var1] = param1;
            return null;
        } else {
            T var3 = var2[var1];
            var2[var1] = param1;
            return var3;
        }
    }

    public T computeIfAbsent(int param0, IntFunction<T> param1) {
        int var0 = param0 >> 8;
        int var1 = param0 & 0xFF;
        T[] var2 = this.blockMap[var0];
        T var3 = var2[var1];
        if (var3 != null) {
            return var3;
        } else {
            if (var2 == this.empty) {
                var2 = (T[])((Object[])this.blockConstructor.apply(256));
                this.blockMap[var0] = var2;
            }

            T var4 = param1.apply(param0);
            var2[var1] = var4;
            return var4;
        }
    }

    @Nullable
    public T remove(int param0) {
        int var0 = param0 >> 8;
        int var1 = param0 & 0xFF;
        T[] var2 = this.blockMap[var0];
        if (var2 == this.empty) {
            return null;
        } else {
            T var3 = var2[var1];
            var2[var1] = null;
            return var3;
        }
    }

    public void forEach(CodepointMap.Output<T> param0) {
        for(int var0 = 0; var0 < this.blockMap.length; ++var0) {
            T[] var1 = this.blockMap[var0];
            if (var1 != this.empty) {
                for(int var2 = 0; var2 < var1.length; ++var2) {
                    T var3 = var1[var2];
                    if (var3 != null) {
                        int var4 = var0 << 8 | var2;
                        param0.accept(var4, var3);
                    }
                }
            }
        }

    }

    public IntSet keySet() {
        IntOpenHashSet var0 = new IntOpenHashSet();
        this.forEach((param1, param2) -> var0.add(param1));
        return var0;
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface Output<T> {
        void accept(int var1, T var2);
    }
}
