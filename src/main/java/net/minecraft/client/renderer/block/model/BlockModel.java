package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BlockModel implements UnbakedModel {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    @VisibleForTesting
    static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(BlockModel.class, new BlockModel.Deserializer())
        .registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer())
        .registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer())
        .registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
        .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
        .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
        .registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer())
        .create();
    private static final char REFERENCE_CHAR = '#';
    public static final String PARTICLE_TEXTURE_REFERENCE = "particle";
    private final List<BlockElement> elements;
    @Nullable
    private final BlockModel.GuiLight guiLight;
    private final boolean hasAmbientOcclusion;
    private final ItemTransforms transforms;
    private final List<ItemOverride> overrides;
    public String name = "";
    @VisibleForTesting
    protected final Map<String, Either<Material, String>> textureMap;
    @Nullable
    protected BlockModel parent;
    @Nullable
    protected ResourceLocation parentLocation;

    public static BlockModel fromStream(Reader param0) {
        return GsonHelper.fromJson(GSON, param0, BlockModel.class);
    }

    public static BlockModel fromString(String param0) {
        return fromStream(new StringReader(param0));
    }

    public BlockModel(
        @Nullable ResourceLocation param0,
        List<BlockElement> param1,
        Map<String, Either<Material, String>> param2,
        boolean param3,
        @Nullable BlockModel.GuiLight param4,
        ItemTransforms param5,
        List<ItemOverride> param6
    ) {
        this.elements = param1;
        this.hasAmbientOcclusion = param3;
        this.guiLight = param4;
        this.textureMap = param2;
        this.parentLocation = param0;
        this.transforms = param5;
        this.overrides = param6;
    }

    public List<BlockElement> getElements() {
        return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
    }

    public boolean hasAmbientOcclusion() {
        return this.parent != null ? this.parent.hasAmbientOcclusion() : this.hasAmbientOcclusion;
    }

    public BlockModel.GuiLight getGuiLight() {
        if (this.guiLight != null) {
            return this.guiLight;
        } else {
            return this.parent != null ? this.parent.getGuiLight() : BlockModel.GuiLight.SIDE;
        }
    }

    public boolean isResolved() {
        return this.parentLocation == null || this.parent != null && this.parent.isResolved();
    }

    public List<ItemOverride> getOverrides() {
        return this.overrides;
    }

    private ItemOverrides getItemOverrides(ModelBakery param0, BlockModel param1) {
        return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(param0, param1, param0::getModel, this.overrides);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        Set<ResourceLocation> var0 = Sets.newHashSet();

        for(ItemOverride var1 : this.overrides) {
            var0.add(var1.getModel());
        }

        if (this.parentLocation != null) {
            var0.add(this.parentLocation);
        }

        return var0;
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> param0, Set<Pair<String, String>> param1) {
        Set<UnbakedModel> var0 = Sets.newLinkedHashSet();

        for(BlockModel var1 = this; var1.parentLocation != null && var1.parent == null; var1 = var1.parent) {
            var0.add(var1);
            UnbakedModel var2 = param0.apply(var1.parentLocation);
            if (var2 == null) {
                LOGGER.warn("No parent '{}' while loading model '{}'", this.parentLocation, var1);
            }

            if (var0.contains(var2)) {
                LOGGER.warn(
                    "Found 'parent' loop while loading model '{}' in chain: {} -> {}",
                    var1,
                    var0.stream().map(Object::toString).collect(Collectors.joining(" -> ")),
                    this.parentLocation
                );
                var2 = null;
            }

            if (var2 == null) {
                var1.parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
                var2 = param0.apply(var1.parentLocation);
            }

            if (!(var2 instanceof BlockModel)) {
                throw new IllegalStateException("BlockModel parent has to be a block model.");
            }

            var1.parent = (BlockModel)var2;
        }

        Set<Material> var3 = Sets.newHashSet(this.getMaterial("particle"));

        for(BlockElement var4 : this.getElements()) {
            for(BlockElementFace var5 : var4.faces.values()) {
                Material var6 = this.getMaterial(var5.texture);
                if (Objects.equals(var6.texture(), MissingTextureAtlasSprite.getLocation())) {
                    param1.add(Pair.of(var5.texture, this.name));
                }

                var3.add(var6);
            }
        }

        this.overrides.forEach(param3 -> {
            UnbakedModel var0x = param0.apply(param3.getModel());
            if (!Objects.equals(var0x, this)) {
                var3.addAll(var0x.getMaterials(param0, param1));
            }
        });
        if (this.getRootModel() == ModelBakery.GENERATION_MARKER) {
            ItemModelGenerator.LAYERS.forEach(param1x -> var3.add(this.getMaterial(param1x)));
        }

        return var3;
    }

    @Override
    public BakedModel bake(ModelBakery param0, Function<Material, TextureAtlasSprite> param1, ModelState param2, ResourceLocation param3) {
        return this.bake(param0, this, param1, param2, param3, true);
    }

    public BakedModel bake(
        ModelBakery param0, BlockModel param1, Function<Material, TextureAtlasSprite> param2, ModelState param3, ResourceLocation param4, boolean param5
    ) {
        TextureAtlasSprite var0 = param2.apply(this.getMaterial("particle"));
        if (this.getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
            return new BuiltInModel(this.getTransforms(), this.getItemOverrides(param0, param1), var0, this.getGuiLight().lightLikeBlock());
        } else {
            SimpleBakedModel.Builder var1 = new SimpleBakedModel.Builder(this, this.getItemOverrides(param0, param1), param5).particle(var0);

            for(BlockElement var2 : this.getElements()) {
                for(Direction var3 : var2.faces.keySet()) {
                    BlockElementFace var4 = var2.faces.get(var3);
                    TextureAtlasSprite var5 = param2.apply(this.getMaterial(var4.texture));
                    if (var4.cullForDirection == null) {
                        var1.addUnculledFace(bakeFace(var2, var4, var5, var3, param3, param4));
                    } else {
                        var1.addCulledFace(
                            Direction.rotate(param3.getRotation().getMatrix(), var4.cullForDirection), bakeFace(var2, var4, var5, var3, param3, param4)
                        );
                    }
                }
            }

            return var1.build();
        }
    }

    private static BakedQuad bakeFace(
        BlockElement param0, BlockElementFace param1, TextureAtlasSprite param2, Direction param3, ModelState param4, ResourceLocation param5
    ) {
        return FACE_BAKERY.bakeQuad(param0.from, param0.to, param1, param2, param3, param4, param0.rotation, param0.shade, param5);
    }

    public boolean hasTexture(String param0) {
        return !MissingTextureAtlasSprite.getLocation().equals(this.getMaterial(param0).texture());
    }

    public Material getMaterial(String param0) {
        if (isTextureReference(param0)) {
            param0 = param0.substring(1);
        }

        List<String> var0 = Lists.newArrayList();

        while(true) {
            Either<Material, String> var1 = this.findTextureEntry(param0);
            Optional<Material> var2 = var1.left();
            if (var2.isPresent()) {
                return var2.get();
            }

            param0 = var1.right().get();
            if (var0.contains(param0)) {
                LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", Joiner.on("->").join(var0), param0, this.name);
                return new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
            }

            var0.add(param0);
        }
    }

    private Either<Material, String> findTextureEntry(String param0) {
        for(BlockModel var0 = this; var0 != null; var0 = var0.parent) {
            Either<Material, String> var1 = var0.textureMap.get(param0);
            if (var1 != null) {
                return var1;
            }
        }

        return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
    }

    static boolean isTextureReference(String param0) {
        return param0.charAt(0) == '#';
    }

    public BlockModel getRootModel() {
        return this.parent == null ? this : this.parent.getRootModel();
    }

    public ItemTransforms getTransforms() {
        ItemTransform var0 = this.getTransform(ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
        ItemTransform var1 = this.getTransform(ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        ItemTransform var2 = this.getTransform(ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
        ItemTransform var3 = this.getTransform(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
        ItemTransform var4 = this.getTransform(ItemTransforms.TransformType.HEAD);
        ItemTransform var5 = this.getTransform(ItemTransforms.TransformType.GUI);
        ItemTransform var6 = this.getTransform(ItemTransforms.TransformType.GROUND);
        ItemTransform var7 = this.getTransform(ItemTransforms.TransformType.FIXED);
        return new ItemTransforms(var0, var1, var2, var3, var4, var5, var6, var7);
    }

    private ItemTransform getTransform(ItemTransforms.TransformType param0) {
        return this.parent != null && !this.transforms.hasTransform(param0) ? this.parent.getTransform(param0) : this.transforms.getTransform(param0);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<BlockModel> {
        private static final boolean DEFAULT_AMBIENT_OCCLUSION = true;

        public BlockModel deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            List<BlockElement> var1 = this.getElements(param2, var0);
            String var2 = this.getParentName(var0);
            Map<String, Either<Material, String>> var3 = this.getTextureMap(var0);
            boolean var4 = this.getAmbientOcclusion(var0);
            ItemTransforms var5 = ItemTransforms.NO_TRANSFORMS;
            if (var0.has("display")) {
                JsonObject var6 = GsonHelper.getAsJsonObject(var0, "display");
                var5 = param2.deserialize(var6, ItemTransforms.class);
            }

            List<ItemOverride> var7 = this.getOverrides(param2, var0);
            BlockModel.GuiLight var8 = null;
            if (var0.has("gui_light")) {
                var8 = BlockModel.GuiLight.getByName(GsonHelper.getAsString(var0, "gui_light"));
            }

            ResourceLocation var9 = var2.isEmpty() ? null : new ResourceLocation(var2);
            return new BlockModel(var9, var1, var3, var4, var8, var5, var7);
        }

        protected List<ItemOverride> getOverrides(JsonDeserializationContext param0, JsonObject param1) {
            List<ItemOverride> var0 = Lists.newArrayList();
            if (param1.has("overrides")) {
                for(JsonElement var2 : GsonHelper.getAsJsonArray(param1, "overrides")) {
                    var0.add(param0.deserialize(var2, ItemOverride.class));
                }
            }

            return var0;
        }

        private Map<String, Either<Material, String>> getTextureMap(JsonObject param0) {
            ResourceLocation var0 = TextureAtlas.LOCATION_BLOCKS;
            Map<String, Either<Material, String>> var1 = Maps.newHashMap();
            if (param0.has("textures")) {
                JsonObject var2 = GsonHelper.getAsJsonObject(param0, "textures");

                for(Entry<String, JsonElement> var3 : var2.entrySet()) {
                    var1.put(var3.getKey(), parseTextureLocationOrReference(var0, var3.getValue().getAsString()));
                }
            }

            return var1;
        }

        private static Either<Material, String> parseTextureLocationOrReference(ResourceLocation param0, String param1) {
            if (BlockModel.isTextureReference(param1)) {
                return Either.right(param1.substring(1));
            } else {
                ResourceLocation var0 = ResourceLocation.tryParse(param1);
                if (var0 == null) {
                    throw new JsonParseException(param1 + " is not valid resource location");
                } else {
                    return Either.left(new Material(param0, var0));
                }
            }
        }

        private String getParentName(JsonObject param0) {
            return GsonHelper.getAsString(param0, "parent", "");
        }

        protected boolean getAmbientOcclusion(JsonObject param0) {
            return GsonHelper.getAsBoolean(param0, "ambientocclusion", true);
        }

        protected List<BlockElement> getElements(JsonDeserializationContext param0, JsonObject param1) {
            List<BlockElement> var0 = Lists.newArrayList();
            if (param1.has("elements")) {
                for(JsonElement var1 : GsonHelper.getAsJsonArray(param1, "elements")) {
                    var0.add(param0.deserialize(var1, BlockElement.class));
                }
            }

            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum GuiLight {
        FRONT("front"),
        SIDE("side");

        private final String name;

        private GuiLight(String param0) {
            this.name = param0;
        }

        public static BlockModel.GuiLight getByName(String param0) {
            for(BlockModel.GuiLight var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            throw new IllegalArgumentException("Invalid gui light: " + param0);
        }

        public boolean lightLikeBlock() {
            return this == SIDE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LoopException extends RuntimeException {
        public LoopException(String param0) {
            super(param0);
        }
    }
}
