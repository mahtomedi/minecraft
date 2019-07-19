package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VisGraph {
    private static final int DX = (int)Math.pow(16.0, 0.0);
    private static final int DZ = (int)Math.pow(16.0, 1.0);
    private static final int DY = (int)Math.pow(16.0, 2.0);
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BitSet bitSet = new BitSet(4096);
    private static final int[] INDEX_OF_EDGES = Util.make(new int[1352], param0 -> {
        int var0 = 0;
        int var1 = 15;
        int var2 = 0;

        for(int var3 = 0; var3 < 16; ++var3) {
            for(int var4 = 0; var4 < 16; ++var4) {
                for(int var5 = 0; var5 < 16; ++var5) {
                    if (var3 == 0 || var3 == 15 || var4 == 0 || var4 == 15 || var5 == 0 || var5 == 15) {
                        param0[var2++] = getIndex(var3, var4, var5);
                    }
                }
            }
        }

    });
    private int empty = 4096;

    public void setOpaque(BlockPos param0) {
        this.bitSet.set(getIndex(param0), true);
        --this.empty;
    }

    private static int getIndex(BlockPos param0) {
        return getIndex(param0.getX() & 15, param0.getY() & 15, param0.getZ() & 15);
    }

    private static int getIndex(int param0, int param1, int param2) {
        return param0 << 0 | param1 << 8 | param2 << 4;
    }

    public VisibilitySet resolve() {
        VisibilitySet var0 = new VisibilitySet();
        if (4096 - this.empty < 256) {
            var0.setAll(true);
        } else if (this.empty == 0) {
            var0.setAll(false);
        } else {
            for(int var1 : INDEX_OF_EDGES) {
                if (!this.bitSet.get(var1)) {
                    var0.add(this.floodFill(var1));
                }
            }
        }

        return var0;
    }

    public Set<Direction> floodFill(BlockPos param0) {
        return this.floodFill(getIndex(param0));
    }

    private Set<Direction> floodFill(int param0) {
        Set<Direction> var0 = EnumSet.noneOf(Direction.class);
        IntPriorityQueue var1 = new IntArrayFIFOQueue();
        var1.enqueue(param0);
        this.bitSet.set(param0, true);

        while(!var1.isEmpty()) {
            int var2 = var1.dequeueInt();
            this.addEdges(var2, var0);

            for(Direction var3 : DIRECTIONS) {
                int var4 = this.getNeighborIndexAtFace(var2, var3);
                if (var4 >= 0 && !this.bitSet.get(var4)) {
                    this.bitSet.set(var4, true);
                    var1.enqueue(var4);
                }
            }
        }

        return var0;
    }

    private void addEdges(int param0, Set<Direction> param1) {
        int var0 = param0 >> 0 & 15;
        if (var0 == 0) {
            param1.add(Direction.WEST);
        } else if (var0 == 15) {
            param1.add(Direction.EAST);
        }

        int var1 = param0 >> 8 & 15;
        if (var1 == 0) {
            param1.add(Direction.DOWN);
        } else if (var1 == 15) {
            param1.add(Direction.UP);
        }

        int var2 = param0 >> 4 & 15;
        if (var2 == 0) {
            param1.add(Direction.NORTH);
        } else if (var2 == 15) {
            param1.add(Direction.SOUTH);
        }

    }

    private int getNeighborIndexAtFace(int param0, Direction param1) {
        switch(param1) {
            case DOWN:
                if ((param0 >> 8 & 15) == 0) {
                    return -1;
                }

                return param0 - DY;
            case UP:
                if ((param0 >> 8 & 15) == 15) {
                    return -1;
                }

                return param0 + DY;
            case NORTH:
                if ((param0 >> 4 & 15) == 0) {
                    return -1;
                }

                return param0 - DZ;
            case SOUTH:
                if ((param0 >> 4 & 15) == 15) {
                    return -1;
                }

                return param0 + DZ;
            case WEST:
                if ((param0 >> 0 & 15) == 0) {
                    return -1;
                }

                return param0 - DX;
            case EAST:
                if ((param0 >> 0 & 15) == 15) {
                    return -1;
                }

                return param0 + DX;
            default:
                return -1;
        }
    }
}
