package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public class StructureUpdater implements SnbtToNbt.Filter {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public CompoundTag apply(String param0, CompoundTag param1) {
        return param0.startsWith("data/minecraft/structures/") ? update(param0, param1) : param1;
    }

    public static CompoundTag update(String param0, CompoundTag param1) {
        return updateStructure(param0, patchVersion(param1));
    }

    private static CompoundTag patchVersion(CompoundTag param0) {
        if (!param0.contains("DataVersion", 99)) {
            param0.putInt("DataVersion", 500);
        }

        return param0;
    }

    private static CompoundTag updateStructure(String param0, CompoundTag param1) {
        StructureTemplate var0 = new StructureTemplate();
        int var1 = param1.getInt("DataVersion");
        int var2 = 2965;
        if (var1 < 2965) {
            LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", var1, 2965, param0);
        }

        CompoundTag var3 = NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, param1, var1);
        var0.load(var3);
        return var0.save(new CompoundTag());
    }
}
