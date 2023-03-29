package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ServerLevelAccessor;

public class CappedProcessor extends StructureProcessor {
    public static final Codec<CappedProcessor> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    StructureProcessorType.SINGLE_CODEC.fieldOf("delegate").forGetter(param0x -> param0x.delegate),
                    IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter(param0x -> param0x.limit)
                )
                .apply(param0, CappedProcessor::new)
    );
    private final StructureProcessor delegate;
    private final IntProvider limit;

    public CappedProcessor(StructureProcessor param0, IntProvider param1) {
        this.delegate = param0;
        this.limit = param1;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.CAPPED;
    }

    @Override
    public final List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
        ServerLevelAccessor param0,
        BlockPos param1,
        BlockPos param2,
        List<StructureTemplate.StructureBlockInfo> param3,
        List<StructureTemplate.StructureBlockInfo> param4,
        StructurePlaceSettings param5
    ) {
        if (this.limit.getMaxValue() != 0 && !param4.isEmpty()) {
            if (param3.size() != param4.size()) {
                Util.logAndPauseIfInIde(
                    "Original block info list not in sync with processed list, skipping processing. Original size: "
                        + param3.size()
                        + ", Processed size: "
                        + param4.size()
                );
                return param4;
            } else {
                RandomSource var0 = RandomSource.create(param0.getLevel().getSeed()).forkPositional().at(param1);
                int var1 = Math.min(this.limit.sample(var0), param4.size());
                if (var1 < 1) {
                    return param4;
                } else {
                    IntArrayList var2 = Util.toShuffledList(IntStream.range(0, param4.size()), var0);
                    IntIterator var3 = var2.intIterator();
                    int var4 = 0;

                    while(var3.hasNext() && var4 < var1) {
                        int var5 = var3.nextInt();
                        StructureTemplate.StructureBlockInfo var6 = param3.get(var5);
                        StructureTemplate.StructureBlockInfo var7 = param4.get(var5);
                        StructureTemplate.StructureBlockInfo var8 = this.delegate.processBlock(param0, param1, param2, var6, var7, param5);
                        if (var8 != null && !var7.equals(var8)) {
                            ++var4;
                            param4.set(var5, var8);
                        }
                    }

                    return param4;
                }
            }
        } else {
            return param4;
        }
    }
}
