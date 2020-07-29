package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;

public class StructureProcessorList {
    private final List<StructureProcessor> list;

    public StructureProcessorList(List<StructureProcessor> param0) {
        this.list = param0;
    }

    public List<StructureProcessor> list() {
        return this.list;
    }

    @Override
    public String toString() {
        return "ProcessorList[" + this.list + "]";
    }
}
