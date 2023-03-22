package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
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
        StructureTemplate var0 = new StructureTemplate();
        int var1 = NbtUtils.getDataVersion(param1, 500);
        int var2 = 3437;
        if (var1 < 3437) {
            LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", var1, 3437, param0);
        }

        CompoundTag var3 = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), param1, var1);
        var0.load(BuiltInRegistries.BLOCK.asLookup(), var3);
        return var0.save(new CompoundTag());
    }
}
