package net.minecraft.world.level.levelgen.structure.pieces;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public record StructurePieceSerializationContext(ResourceManager resourceManager, RegistryAccess registryAccess, StructureManager structureManager) {
    public static StructurePieceSerializationContext fromLevel(ServerLevel param0) {
        MinecraftServer var0 = param0.getServer();
        return new StructurePieceSerializationContext(var0.getResourceManager(), var0.registryAccess(), var0.getStructureManager());
    }
}
