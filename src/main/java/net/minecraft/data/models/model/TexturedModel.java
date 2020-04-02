package net.minecraft.data.models.model;

import com.google.gson.JsonElement;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class TexturedModel {
    public static final TexturedModel.Provider CUBE = createDefault(TextureMapping::cube, ModelTemplates.CUBE_ALL);
    public static final TexturedModel.Provider CUBE_MIRRORED = createDefault(TextureMapping::cube, ModelTemplates.CUBE_MIRRORED_ALL);
    public static final TexturedModel.Provider COLUMN = createDefault(TextureMapping::column, ModelTemplates.CUBE_COLUMN);
    public static final TexturedModel.Provider COLUMN_HORIZONTAL = createDefault(TextureMapping::column, ModelTemplates.CUBE_COLUMN_HORIZONTAL);
    public static final TexturedModel.Provider CUBE_TOP_BOTTOM = createDefault(TextureMapping::cubeBottomTop, ModelTemplates.CUBE_BOTTOM_TOP);
    public static final TexturedModel.Provider CUBE_TOP = createDefault(TextureMapping::cubeTop, ModelTemplates.CUBE_TOP);
    public static final TexturedModel.Provider ORIENTABLE_ONLY_TOP = createDefault(TextureMapping::orientableCubeOnlyTop, ModelTemplates.CUBE_ORIENTABLE);
    public static final TexturedModel.Provider ORIENTABLE = createDefault(TextureMapping::orientableCube, ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM);
    public static final TexturedModel.Provider CARPET = createDefault(TextureMapping::wool, ModelTemplates.CARPET);
    public static final TexturedModel.Provider GLAZED_TERRACOTTA = createDefault(TextureMapping::pattern, ModelTemplates.GLAZED_TERRACOTTA);
    public static final TexturedModel.Provider CORAL_FAN = createDefault(TextureMapping::fan, ModelTemplates.CORAL_FAN);
    public static final TexturedModel.Provider PARTICLE_ONLY = createDefault(TextureMapping::particle, ModelTemplates.PARTICLE_ONLY);
    public static final TexturedModel.Provider ANVIL = createDefault(TextureMapping::top, ModelTemplates.ANVIL);
    public static final TexturedModel.Provider LEAVES = createDefault(TextureMapping::cube, ModelTemplates.LEAVES);
    public static final TexturedModel.Provider LANTERN = createDefault(TextureMapping::lantern, ModelTemplates.LANTERN);
    public static final TexturedModel.Provider HANGING_LANTERN = createDefault(TextureMapping::lantern, ModelTemplates.HANGING_LANTERN);
    public static final TexturedModel.Provider TORCH = createDefault(TextureMapping::torch, ModelTemplates.TORCH);
    public static final TexturedModel.Provider WALL_TORCH = createDefault(TextureMapping::torch, ModelTemplates.WALL_TORCH);
    public static final TexturedModel.Provider SEAGRASS = createDefault(TextureMapping::defaultTexture, ModelTemplates.SEAGRASS);
    public static final TexturedModel.Provider COLUMN_ALT = createDefault(TextureMapping::logColumn, ModelTemplates.CUBE_COLUMN);
    public static final TexturedModel.Provider COLUMN_HORIZONTAL_ALT = createDefault(TextureMapping::logColumn, ModelTemplates.CUBE_COLUMN_HORIZONTAL);
    public static final TexturedModel.Provider TOP_BOTTOM_WITH_WALL = createDefault(TextureMapping::cubeBottomTopWithWall, ModelTemplates.CUBE_BOTTOM_TOP);
    private final TextureMapping mapping;
    private final ModelTemplate template;

    private TexturedModel(TextureMapping param0, ModelTemplate param1) {
        this.mapping = param0;
        this.template = param1;
    }

    public ModelTemplate getTemplate() {
        return this.template;
    }

    public TextureMapping getMapping() {
        return this.mapping;
    }

    public TexturedModel updateTextures(Consumer<TextureMapping> param0) {
        param0.accept(this.mapping);
        return this;
    }

    public ResourceLocation create(Block param0, BiConsumer<ResourceLocation, Supplier<JsonElement>> param1) {
        return this.template.create(param0, this.mapping, param1);
    }

    public ResourceLocation createWithSuffix(Block param0, String param1, BiConsumer<ResourceLocation, Supplier<JsonElement>> param2) {
        return this.template.createWithSuffix(param0, param1, this.mapping, param2);
    }

    private static TexturedModel.Provider createDefault(Function<Block, TextureMapping> param0, ModelTemplate param1) {
        return param2 -> new TexturedModel(param0.apply(param2), param1);
    }

    public static TexturedModel createAllSame(ResourceLocation param0) {
        return new TexturedModel(TextureMapping.cube(param0), ModelTemplates.CUBE_ALL);
    }

    @FunctionalInterface
    public interface Provider {
        TexturedModel get(Block var1);

        default ResourceLocation create(Block param0, BiConsumer<ResourceLocation, Supplier<JsonElement>> param1) {
            return this.get(param0).create(param0, param1);
        }

        default ResourceLocation createWithSuffix(Block param0, String param1, BiConsumer<ResourceLocation, Supplier<JsonElement>> param2) {
            return this.get(param0).createWithSuffix(param0, param1, param2);
        }

        default TexturedModel.Provider updateTexture(Consumer<TextureMapping> param0) {
            return param1 -> this.get(param1).updateTextures(param0);
        }
    }
}
