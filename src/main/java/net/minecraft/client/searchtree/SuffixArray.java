package net.minecraft.client.searchtree;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SuffixArray<T> {
    private static final boolean DEBUG_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
    private static final boolean DEBUG_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
    private static final Logger LOGGER = LogManager.getLogger();
    protected final List<T> list = Lists.newArrayList();
    private final IntList chars = new IntArrayList();
    private final IntList wordStarts = new IntArrayList();
    private IntList suffixToT = new IntArrayList();
    private IntList offsets = new IntArrayList();
    private int maxStringLength;

    public void add(T param0, String param1) {
        this.maxStringLength = Math.max(this.maxStringLength, param1.length());
        int var0 = this.list.size();
        this.list.add(param0);
        this.wordStarts.add(this.chars.size());

        for(int var1 = 0; var1 < param1.length(); ++var1) {
            this.suffixToT.add(var0);
            this.offsets.add(var1);
            this.chars.add(param1.charAt(var1));
        }

        this.suffixToT.add(var0);
        this.offsets.add(param1.length());
        this.chars.add(-1);
    }

    public void generate() {
        int var0 = this.chars.size();
        int[] var1 = new int[var0];
        final int[] var2 = new int[var0];
        final int[] var3 = new int[var0];
        int[] var4 = new int[var0];
        IntComparator var5 = new IntComparator() {
            @Override
            public int compare(int param0, int param1) {
                return var2[param0] == var2[param1] ? Integer.compare(var3[param0], var3[param1]) : Integer.compare(var2[param0], var2[param1]);
            }

            @Override
            public int compare(Integer param0, Integer param1) {
                return this.compare(param0.intValue(), param1.intValue());
            }
        };
        Swapper var6 = (param3, param4) -> {
            if (param3 != param4) {
                int var0x = var2[param3];
                var2[param3] = var2[param4];
                var2[param4] = var0x;
                var0x = var3[param3];
                var3[param3] = var3[param4];
                var3[param4] = var0x;
                var0x = var4[param3];
                var4[param3] = var4[param4];
                var4[param4] = var0x;
            }

        };

        for(int var7 = 0; var7 < var0; ++var7) {
            var1[var7] = this.chars.getInt(var7);
        }

        int var8 = 1;

        for(int var9 = Math.min(var0, this.maxStringLength); var8 * 2 < var9; var8 *= 2) {
            for(int var10 = 0; var10 < var0; var4[var10] = var10++) {
                var2[var10] = var1[var10];
                var3[var10] = var10 + var8 < var0 ? var1[var10 + var8] : -2;
            }

            Arrays.quickSort(0, var0, var5, var6);

            for(int var11 = 0; var11 < var0; ++var11) {
                if (var11 > 0 && var2[var11] == var2[var11 - 1] && var3[var11] == var3[var11 - 1]) {
                    var1[var4[var11]] = var1[var4[var11 - 1]];
                } else {
                    var1[var4[var11]] = var11;
                }
            }
        }

        IntList var12 = this.suffixToT;
        IntList var13 = this.offsets;
        this.suffixToT = new IntArrayList(var12.size());
        this.offsets = new IntArrayList(var13.size());

        for(int var14 = 0; var14 < var0; ++var14) {
            int var15 = var4[var14];
            this.suffixToT.add(var12.getInt(var15));
            this.offsets.add(var13.getInt(var15));
        }

        if (DEBUG_ARRAY) {
            this.print();
        }

    }

    private void print() {
        for(int var0 = 0; var0 < this.suffixToT.size(); ++var0) {
            LOGGER.debug("{} {}", var0, this.getString(var0));
        }

        LOGGER.debug("");
    }

    private String getString(int param0) {
        int var0 = this.offsets.getInt(param0);
        int var1 = this.wordStarts.getInt(this.suffixToT.getInt(param0));
        StringBuilder var2 = new StringBuilder();

        for(int var3 = 0; var1 + var3 < this.chars.size(); ++var3) {
            if (var3 == var0) {
                var2.append('^');
            }

            int var4 = this.chars.get(var1 + var3);
            if (var4 == -1) {
                break;
            }

            var2.append((char)var4);
        }

        return var2.toString();
    }

    private int compare(String param0, int param1) {
        int var0 = this.wordStarts.getInt(this.suffixToT.getInt(param1));
        int var1 = this.offsets.getInt(param1);

        for(int var2 = 0; var2 < param0.length(); ++var2) {
            int var3 = this.chars.getInt(var0 + var1 + var2);
            if (var3 == -1) {
                return 1;
            }

            char var4 = param0.charAt(var2);
            char var5 = (char)var3;
            if (var4 < var5) {
                return -1;
            }

            if (var4 > var5) {
                return 1;
            }
        }

        return 0;
    }

    public List<T> search(String param0) {
        int var0 = this.suffixToT.size();
        int var1 = 0;
        int var2 = var0;

        while(var1 < var2) {
            int var3 = var1 + (var2 - var1) / 2;
            int var4 = this.compare(param0, var3);
            if (DEBUG_COMPARISONS) {
                LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", param0, var3, this.getString(var3), var4);
            }

            if (var4 > 0) {
                var1 = var3 + 1;
            } else {
                var2 = var3;
            }
        }

        if (var1 >= 0 && var1 < var0) {
            int var5 = var1;
            var2 = var0;

            while(var1 < var2) {
                int var6 = var1 + (var2 - var1) / 2;
                int var7 = this.compare(param0, var6);
                if (DEBUG_COMPARISONS) {
                    LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", param0, var6, this.getString(var6), var7);
                }

                if (var7 >= 0) {
                    var1 = var6 + 1;
                } else {
                    var2 = var6;
                }
            }

            int var8 = var1;
            IntSet var9 = new IntOpenHashSet();

            for(int var10 = var5; var10 < var8; ++var10) {
                var9.add(this.suffixToT.getInt(var10));
            }

            int[] var11 = var9.toIntArray();
            java.util.Arrays.sort(var11);
            Set<T> var12 = Sets.newLinkedHashSet();

            for(int var13 : var11) {
                var12.add(this.list.get(var13));
            }

            return Lists.newArrayList(var12);
        } else {
            return Collections.emptyList();
        }
    }
}
