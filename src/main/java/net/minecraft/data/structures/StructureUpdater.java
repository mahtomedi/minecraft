package net.minecraft.data.structures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureUpdater implements SnbtToNbt.Filter {
    @Override
    public CompoundTag apply(String param0, CompoundTag param1) {
        return param0.startsWith("data/minecraft/structures/") ? updateStructure(patchVersion(param1)) : param1;
    }

    private static CompoundTag patchVersion(CompoundTag param0) {
        if (!param0.contains("DataVersion", 99)) {
            param0.putInt("DataVersion", 500);
        }

        return param0;
    }

    private static CompoundTag updateStructure(CompoundTag param0) {
        StructureTemplate var0 = new StructureTemplate();
        var0.load(NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, param0, param0.getInt("DataVersion")));
        return var0.save(new CompoundTag());
    }
}
