package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.PackedBitStorage;

public class LeavesFix extends DataFix {
    private static final int NORTH_WEST_MASK = 128;
    private static final int WEST_MASK = 64;
    private static final int SOUTH_WEST_MASK = 32;
    private static final int SOUTH_MASK = 16;
    private static final int SOUTH_EAST_MASK = 8;
    private static final int EAST_MASK = 4;
    private static final int NORTH_EAST_MASK = 2;
    private static final int NORTH_MASK = 1;
    private static final int[][] DIRECTIONS = new int[][]{{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    private static final int DECAY_DISTANCE = 7;
    private static final int SIZE_BITS = 12;
    private static final int SIZE = 4096;
    private static final Object2IntMap<String> LEAVES = DataFixUtils.make(new Object2IntOpenHashMap<>(), param0 -> {
        param0.put("minecraft:acacia_leaves", 0);
        param0.put("minecraft:birch_leaves", 1);
        param0.put("minecraft:dark_oak_leaves", 2);
        param0.put("minecraft:jungle_leaves", 3);
        param0.put("minecraft:oak_leaves", 4);
        param0.put("minecraft:spruce_leaves", 5);
    });
    private static final Set<String> LOGS = ImmutableSet.of(
        "minecraft:acacia_bark",
        "minecraft:birch_bark",
        "minecraft:dark_oak_bark",
        "minecraft:jungle_bark",
        "minecraft:oak_bark",
        "minecraft:spruce_bark",
        "minecraft:acacia_log",
        "minecraft:birch_log",
        "minecraft:dark_oak_log",
        "minecraft:jungle_log",
        "minecraft:oak_log",
        "minecraft:spruce_log",
        "minecraft:stripped_acacia_log",
        "minecraft:stripped_birch_log",
        "minecraft:stripped_dark_oak_log",
        "minecraft:stripped_jungle_log",
        "minecraft:stripped_oak_log",
        "minecraft:stripped_spruce_log"
    );

    public LeavesFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("Level");
        OpticFinder<?> var2 = var1.type().findField("Sections");
        Type<?> var3 = var2.type();
        if (!(var3 instanceof ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
        } else {
            Type<?> var4 = ((ListType)var3).getElement();
            OpticFinder<?> var5 = DSL.typeFinder(var4);
            return this.fixTypeEverywhereTyped(
                "Leaves fix",
                var0,
                param3 -> param3.updateTyped(
                        var1,
                        param2x -> {
                            int[] var0x = new int[]{0};
                            Typed<?> var1x = param2x.updateTyped(
                                var2,
                                param2xx -> {
                                    Int2ObjectMap<LeavesFix.LeavesSection> var0xx = new Int2ObjectOpenHashMap(
                                        param2xx.getAllTyped(var5)
                                            .stream()
                                            .map(param0x -> new LeavesFix.LeavesSection(param0x, this.getInputSchema()))
                                            .collect(Collectors.toMap(LeavesFix.Section::getIndex, param0x -> param0x))
                                    );
                                    if (var0xx.values().stream().allMatch(LeavesFix.Section::isSkippable)) {
                                        return param2xx;
                                    } else {
                                        List<IntSet> var1xx = Lists.newArrayList();
            
                                        for(int var2x = 0; var2x < 7; ++var2x) {
                                            var1xx.add(new IntOpenHashSet());
                                        }
            
                                        for(LeavesFix.LeavesSection var3x : var0xx.values()) {
                                            if (!var3x.isSkippable()) {
                                                for(int var4x = 0; var4x < 4096; ++var4x) {
                                                    int var5x = var3x.getBlock(var4x);
                                                    if (var3x.isLog(var5x)) {
                                                        ((IntSet)var1xx.get(0)).add(var3x.getIndex() << 12 | var4x);
                                                    } else if (var3x.isLeaf(var5x)) {
                                                        int var6 = this.getX(var4x);
                                                        int var7 = this.getZ(var4x);
                                                        var0x[0] |= getSideMask(var6 == 0, var6 == 15, var7 == 0, var7 == 15);
                                                    }
                                                }
                                            }
                                        }
            
                                        for(int var8 = 1; var8 < 7; ++var8) {
                                            IntSet var9 = (IntSet)var1xx.get(var8 - 1);
                                            IntSet var10 = (IntSet)var1xx.get(var8);
                                            IntIterator var11 = var9.iterator();
            
                                            while(var11.hasNext()) {
                                                int var12 = var11.nextInt();
                                                int var13 = this.getX(var12);
                                                int var14 = this.getY(var12);
                                                int var15 = this.getZ(var12);
            
                                                for(int[] var16 : DIRECTIONS) {
                                                    int var17 = var13 + var16[0];
                                                    int var18 = var14 + var16[1];
                                                    int var19 = var15 + var16[2];
                                                    if (var17 >= 0 && var17 <= 15 && var19 >= 0 && var19 <= 15 && var18 >= 0 && var18 <= 255) {
                                                        LeavesFix.LeavesSection var20 = (LeavesFix.LeavesSection)var0xx.get(var18 >> 4);
                                                        if (var20 != null && !var20.isSkippable()) {
                                                            int var21 = getIndex(var17, var18 & 15, var19);
                                                            int var22 = var20.getBlock(var21);
                                                            if (var20.isLeaf(var22)) {
                                                                int var23 = var20.getDistance(var22);
                                                                if (var23 > var8) {
                                                                    var20.setDistance(var21, var22, var8);
                                                                    var10.add(getIndex(var17, var18, var19));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
            
                                        return param2xx.updateTyped(
                                            var5,
                                            param1x -> ((LeavesFix.LeavesSection)var0xx.get(param1x.get(DSL.remainderFinder()).get("Y").asInt(0)))
                                                    .write(param1x)
                                        );
                                    }
                                }
                            );
                            if (var0x[0] != 0) {
                                var1x = var1x.update(
                                    DSL.remainderFinder(),
                                    param1x -> {
                                        Dynamic<?> var0xx = DataFixUtils.orElse(param1x.get("UpgradeData").result(), param1x.emptyMap());
                                        return param1x.set(
                                            "UpgradeData", var0xx.set("Sides", param1x.createByte((byte)(var0xx.get("Sides").asByte((byte)0) | var0x[0])))
                                        );
                                    }
                                );
                            }
        
                            return var1x;
                        }
                    )
            );
        }
    }

    public static int getIndex(int param0, int param1, int param2) {
        return param1 << 8 | param2 << 4 | param0;
    }

    private int getX(int param0) {
        return param0 & 15;
    }

    private int getY(int param0) {
        return param0 >> 8 & 0xFF;
    }

    private int getZ(int param0) {
        return param0 >> 4 & 15;
    }

    public static int getSideMask(boolean param0, boolean param1, boolean param2, boolean param3) {
        int var0 = 0;
        if (param2) {
            if (param1) {
                var0 |= 2;
            } else if (param0) {
                var0 |= 128;
            } else {
                var0 |= 1;
            }
        } else if (param3) {
            if (param0) {
                var0 |= 32;
            } else if (param1) {
                var0 |= 8;
            } else {
                var0 |= 16;
            }
        } else if (param1) {
            var0 |= 4;
        } else if (param0) {
            var0 |= 64;
        }

        return var0;
    }

    public static final class LeavesSection extends LeavesFix.Section {
        private static final String PERSISTENT = "persistent";
        private static final String DECAYABLE = "decayable";
        private static final String DISTANCE = "distance";
        @Nullable
        private IntSet leaveIds;
        @Nullable
        private IntSet logIds;
        @Nullable
        private Int2IntMap stateToIdMap;

        public LeavesSection(Typed<?> param0, Schema param1) {
            super(param0, param1);
        }

        @Override
        protected boolean skippable() {
            this.leaveIds = new IntOpenHashSet();
            this.logIds = new IntOpenHashSet();
            this.stateToIdMap = new Int2IntOpenHashMap();

            for(int var0 = 0; var0 < this.palette.size(); ++var0) {
                Dynamic<?> var1 = this.palette.get(var0);
                String var2 = var1.get("Name").asString("");
                if (LeavesFix.LEAVES.containsKey(var2)) {
                    boolean var3 = Objects.equals(var1.get("Properties").get("decayable").asString(""), "false");
                    this.leaveIds.add(var0);
                    this.stateToIdMap.put(this.getStateId(var2, var3, 7), var0);
                    this.palette.set(var0, this.makeLeafTag(var1, var2, var3, 7));
                }

                if (LeavesFix.LOGS.contains(var2)) {
                    this.logIds.add(var0);
                }
            }

            return this.leaveIds.isEmpty() && this.logIds.isEmpty();
        }

        private Dynamic<?> makeLeafTag(Dynamic<?> param0, String param1, boolean param2, int param3) {
            Dynamic<?> var0 = param0.emptyMap();
            var0 = var0.set("persistent", var0.createString(param2 ? "true" : "false"));
            var0 = var0.set("distance", var0.createString(Integer.toString(param3)));
            Dynamic<?> var1 = param0.emptyMap();
            var1 = var1.set("Properties", var0);
            return var1.set("Name", var1.createString(param1));
        }

        public boolean isLog(int param0) {
            return this.logIds.contains(param0);
        }

        public boolean isLeaf(int param0) {
            return this.leaveIds.contains(param0);
        }

        private int getDistance(int param0) {
            return this.isLog(param0) ? 0 : Integer.parseInt(this.palette.get(param0).get("Properties").get("distance").asString(""));
        }

        private void setDistance(int param0, int param1, int param2) {
            Dynamic<?> var0 = this.palette.get(param1);
            String var1 = var0.get("Name").asString("");
            boolean var2 = Objects.equals(var0.get("Properties").get("persistent").asString(""), "true");
            int var3 = this.getStateId(var1, var2, param2);
            if (!this.stateToIdMap.containsKey(var3)) {
                int var4 = this.palette.size();
                this.leaveIds.add(var4);
                this.stateToIdMap.put(var3, var4);
                this.palette.add(this.makeLeafTag(var0, var1, var2, param2));
            }

            int var5 = this.stateToIdMap.get(var3);
            if (1 << this.storage.getBits() <= var5) {
                PackedBitStorage var6 = new PackedBitStorage(this.storage.getBits() + 1, 4096);

                for(int var7 = 0; var7 < 4096; ++var7) {
                    var6.set(var7, this.storage.get(var7));
                }

                this.storage = var6;
            }

            this.storage.set(param0, var5);
        }
    }

    public abstract static class Section {
        protected static final String BLOCK_STATES_TAG = "BlockStates";
        protected static final String NAME_TAG = "Name";
        protected static final String PROPERTIES_TAG = "Properties";
        private final Type<Pair<String, Dynamic<?>>> blockStateType = DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType());
        protected final OpticFinder<List<Pair<String, Dynamic<?>>>> paletteFinder = DSL.fieldFinder("Palette", DSL.list(this.blockStateType));
        protected final List<Dynamic<?>> palette;
        protected final int index;
        @Nullable
        protected PackedBitStorage storage;

        public Section(Typed<?> param0, Schema param1) {
            if (!Objects.equals(param1.getType(References.BLOCK_STATE), this.blockStateType)) {
                throw new IllegalStateException("Block state type is not what was expected.");
            } else {
                Optional<List<Pair<String, Dynamic<?>>>> var0 = param0.getOptional(this.paletteFinder);
                this.palette = var0.<List>map(param0x -> param0x.stream().map(Pair::getSecond).collect(Collectors.toList())).orElse(ImmutableList.of());
                Dynamic<?> var1 = param0.get(DSL.remainderFinder());
                this.index = var1.get("Y").asInt(0);
                this.readStorage(var1);
            }
        }

        protected void readStorage(Dynamic<?> param0) {
            if (this.skippable()) {
                this.storage = null;
            } else {
                long[] var0 = param0.get("BlockStates").asLongStream().toArray();
                int var1 = Math.max(4, DataFixUtils.ceillog2(this.palette.size()));
                this.storage = new PackedBitStorage(var1, 4096, var0);
            }

        }

        public Typed<?> write(Typed<?> param0) {
            return this.isSkippable()
                ? param0
                : param0.update(DSL.remainderFinder(), param0x -> param0x.set("BlockStates", param0x.createLongList(Arrays.stream(this.storage.getRaw()))))
                    .set(
                        this.paletteFinder,
                        this.palette.stream().map(param0x -> Pair.of(References.BLOCK_STATE.typeName(), param0x)).collect(Collectors.toList())
                    );
        }

        public boolean isSkippable() {
            return this.storage == null;
        }

        public int getBlock(int param0) {
            return this.storage.get(param0);
        }

        protected int getStateId(String param0, boolean param1, int param2) {
            return LeavesFix.LEAVES.get(param0) << 5 | (param1 ? 16 : 0) | param2;
        }

        int getIndex() {
            return this.index;
        }

        protected abstract boolean skippable();
    }
}
