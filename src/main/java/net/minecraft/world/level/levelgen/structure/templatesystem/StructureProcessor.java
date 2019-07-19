package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelReader;

public abstract class StructureProcessor {
    @Nullable
    public abstract StructureTemplate.StructureBlockInfo processBlock(
        LevelReader var1, BlockPos var2, StructureTemplate.StructureBlockInfo var3, StructureTemplate.StructureBlockInfo var4, StructurePlaceSettings var5
    );

    protected abstract StructureProcessorType getType();

    protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> var1);

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.mergeInto(
                this.getDynamic(param0).getValue(),
                param0.createString("processor_type"),
                param0.createString(Registry.STRUCTURE_PROCESSOR.getKey(this.getType()).toString())
            )
        );
    }
}
