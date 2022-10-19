package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ModelBakery {
    public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_0"));
    public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_1"));
    public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/lava_flow"));
    public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_flow"));
    public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_overlay"));
    public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, new ResourceLocation("entity/banner_base"));
    public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base"));
    public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base_nopattern"));
    public static final int DESTROY_STAGE_COUNT = 10;
    public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10)
        .mapToObj(param0 -> new ResourceLocation("block/destroy_stage_" + param0))
        .collect(Collectors.toList());
    public static final List<ResourceLocation> BREAKING_LOCATIONS = DESTROY_STAGES.stream()
        .map(param0 -> new ResourceLocation("textures/" + param0.getPath() + ".png"))
        .collect(Collectors.toList());
    public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
    static final int SINGLETON_MODEL_GROUP = -1;
    private static final int INVISIBLE_MODEL_GROUP = 0;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String BUILTIN_SLASH = "builtin/";
    private static final String BUILTIN_SLASH_GENERATED = "builtin/generated";
    private static final String BUILTIN_BLOCK_ENTITY = "builtin/entity";
    private static final String MISSING_MODEL_NAME = "missing";
    public static final ModelResourceLocation MISSING_MODEL_LOCATION = ModelResourceLocation.vanilla("builtin/missing", "missing");
    public static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");
    public static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
    @VisibleForTesting
    public static final String MISSING_MODEL_MESH = ("{    'textures': {       'particle': '"
            + MissingTextureAtlasSprite.getLocation().getPath()
            + "',       'missingno': '"
            + MissingTextureAtlasSprite.getLocation().getPath()
            + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}")
        .replace('\'', '"');
    private static final Map<String, String> BUILTIN_MODELS = Maps.newHashMap(ImmutableMap.of("missing", MISSING_MODEL_MESH));
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
    public static final BlockModel GENERATION_MARKER = Util.make(
        BlockModel.fromString("{\"gui_light\": \"front\"}"), param0 -> param0.name = "generation marker"
    );
    public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(
        BlockModel.fromString("{\"gui_light\": \"side\"}"), param0 -> param0.name = "block entity marker"
    );
    private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)
        .add(BooleanProperty.create("map"))
        .create(Block::defaultBlockState, BlockState::new);
    static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(
        new ResourceLocation("item_frame"), ITEM_FRAME_FAKE_DEFINITION, new ResourceLocation("glow_item_frame"), ITEM_FRAME_FAKE_DEFINITION
    );
    private final BlockColors blockColors;
    private final Map<ResourceLocation, BlockModel> modelResources;
    private final Map<ResourceLocation, List<ModelBakery.LoadedJson>> blockStateResources;
    private final Set<ResourceLocation> loadingStack = Sets.newHashSet();
    private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();
    private final Map<ResourceLocation, UnbakedModel> unbakedCache = Maps.newHashMap();
    final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = Maps.newHashMap();
    private final Map<ResourceLocation, UnbakedModel> topLevelModels = Maps.newHashMap();
    private final Map<ResourceLocation, BakedModel> bakedTopLevelModels = Maps.newHashMap();
    private int nextModelGroup = 1;
    private final Object2IntMap<BlockState> modelGroups = Util.make(new Object2IntOpenHashMap<>(), param0x -> param0x.defaultReturnValue(-1));

    public ModelBakery(
        BlockColors param0, ProfilerFiller param1, Map<ResourceLocation, BlockModel> param2, Map<ResourceLocation, List<ModelBakery.LoadedJson>> param3
    ) {
        this.blockColors = param0;
        this.modelResources = param2;
        this.blockStateResources = param3;
        param1.push("missing_model");

        try {
            this.unbakedCache.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
            this.loadTopLevel(MISSING_MODEL_LOCATION);
        } catch (IOException var7) {
            LOGGER.error("Error loading missing model, should never happen :(", (Throwable)var7);
            throw new RuntimeException(var7);
        }

        param1.popPush("static_definitions");
        STATIC_DEFINITIONS.forEach(
            (param0x, param1x) -> param1x.getPossibleStates().forEach(param1xx -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(param0x, param1xx)))
        );
        param1.popPush("blocks");

        for(Block var1 : Registry.BLOCK) {
            var1.getStateDefinition().getPossibleStates().forEach(param0x -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(param0x)));
        }

        param1.popPush("items");

        for(ResourceLocation var2 : Registry.ITEM.keySet()) {
            this.loadTopLevel(new ModelResourceLocation(var2, "inventory"));
        }

        param1.popPush("special");
        this.loadTopLevel(ItemRenderer.TRIDENT_IN_HAND_MODEL);
        this.loadTopLevel(ItemRenderer.SPYGLASS_IN_HAND_MODEL);
        this.topLevelModels.values().forEach(param0x -> param0x.resolveParents(this::getModel));
        param1.pop();
    }

    public void bakeModels(BiFunction<ResourceLocation, Material, TextureAtlasSprite> param0) {
        this.topLevelModels.keySet().forEach(param1 -> {
            BakedModel var0 = null;

            try {
                var0 = new ModelBakery.ModelBakerImpl(param0, param1).bake(param1, BlockModelRotation.X0_Y0);
            } catch (Exception var5) {
                LOGGER.warn("Unable to bake model: '{}': {}", param1, var5);
            }

            if (var0 != null) {
                this.bakedTopLevelModels.put(param1, var0);
            }

        });
    }

    private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> param0, String param1) {
        Map<Property<?>, Comparable<?>> var0 = Maps.newHashMap();

        for(String var1 : COMMA_SPLITTER.split(param1)) {
            Iterator<String> var2 = EQUAL_SPLITTER.split(var1).iterator();
            if (var2.hasNext()) {
                String var3 = var2.next();
                Property<?> var4 = param0.getProperty(var3);
                if (var4 != null && var2.hasNext()) {
                    String var5 = var2.next();
                    Comparable<?> var6 = getValueHelper(var4, var5);
                    if (var6 == null) {
                        throw new RuntimeException("Unknown value: '" + var5 + "' for blockstate property: '" + var3 + "' " + var4.getPossibleValues());
                    }

                    var0.put(var4, var6);
                } else if (!var3.isEmpty()) {
                    throw new RuntimeException("Unknown blockstate property: '" + var3 + "'");
                }
            }
        }

        Block var7 = param0.getOwner();
        return param2 -> {
            if (param2 != null && param2.is(var7)) {
                for(Entry<Property<?>, Comparable<?>> var0x : var0.entrySet()) {
                    if (!Objects.equals(param2.getValue(var0x.getKey()), var0x.getValue())) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        };
    }

    @Nullable
    static <T extends Comparable<T>> T getValueHelper(Property<T> param0, String param1) {
        return param0.getValue(param1).orElse((T)null);
    }

    public UnbakedModel getModel(ResourceLocation param0) {
        if (this.unbakedCache.containsKey(param0)) {
            return this.unbakedCache.get(param0);
        } else if (this.loadingStack.contains(param0)) {
            throw new IllegalStateException("Circular reference while loading " + param0);
        } else {
            this.loadingStack.add(param0);
            UnbakedModel var0 = this.unbakedCache.get(MISSING_MODEL_LOCATION);

            while(!this.loadingStack.isEmpty()) {
                ResourceLocation var1 = this.loadingStack.iterator().next();

                try {
                    if (!this.unbakedCache.containsKey(var1)) {
                        this.loadModel(var1);
                    }
                } catch (ModelBakery.BlockStateDefinitionException var9) {
                    LOGGER.warn(var9.getMessage());
                    this.unbakedCache.put(var1, var0);
                } catch (Exception var10) {
                    LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", var1, param0, var10);
                    this.unbakedCache.put(var1, var0);
                } finally {
                    this.loadingStack.remove(var1);
                }
            }

            return this.unbakedCache.getOrDefault(param0, var0);
        }
    }

    private void loadModel(ResourceLocation param0) throws Exception {
        if (!(param0 instanceof ModelResourceLocation)) {
            this.cacheAndQueueDependencies(param0, this.loadBlockModel(param0));
        } else {
            ModelResourceLocation var0 = (ModelResourceLocation)param0;
            if (Objects.equals(var0.getVariant(), "inventory")) {
                ResourceLocation var1 = param0.withPrefix("item/");
                BlockModel var2 = this.loadBlockModel(var1);
                this.cacheAndQueueDependencies(var0, var2);
                this.unbakedCache.put(var1, var2);
            } else {
                ResourceLocation var3 = new ResourceLocation(param0.getNamespace(), param0.getPath());
                StateDefinition<Block, BlockState> var4 = Optional.ofNullable(STATIC_DEFINITIONS.get(var3))
                    .orElseGet(() -> Registry.BLOCK.get(var3).getStateDefinition());
                this.context.setDefinition(var4);
                List<Property<?>> var5 = ImmutableList.copyOf(this.blockColors.getColoringProperties(var4.getOwner()));
                ImmutableList<BlockState> var6 = var4.getPossibleStates();
                Map<ModelResourceLocation, BlockState> var7 = Maps.newHashMap();
                var6.forEach(param2 -> var7.put(BlockModelShaper.stateToModelLocation(var3, param2), param2));
                Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> var8 = Maps.newHashMap();
                ResourceLocation var9 = BLOCKSTATE_LISTER.idToFile(param0);
                UnbakedModel var10 = this.unbakedCache.get(MISSING_MODEL_LOCATION);
                ModelBakery.ModelGroupKey var11 = new ModelBakery.ModelGroupKey(ImmutableList.of(var10), ImmutableList.of());
                Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> var12 = Pair.of(var10, () -> var11);

                try {
                    for(Pair<String, BlockModelDefinition> var14 : this.blockStateResources
                        .getOrDefault(var9, List.of())
                        .stream()
                        .map(
                            param1 -> {
                                try {
                                    return Pair.of(param1.source, BlockModelDefinition.fromJsonElement(this.context, param1.data));
                                } catch (Exception var4x) {
                                    throw new ModelBakery.BlockStateDefinitionException(
                                        String.format(
                                            Locale.ROOT,
                                            "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s",
                                            var9,
                                            param1.source,
                                            var4x.getMessage()
                                        )
                                    );
                                }
                            }
                        )
                        .toList()) {
                        BlockModelDefinition var15 = var14.getSecond();
                        Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> var16 = Maps.newIdentityHashMap();
                        MultiPart var17;
                        if (var15.isMultiPart()) {
                            var17 = var15.getMultiPart();
                            var6.forEach(param3 -> var16.put(param3, Pair.of(var17, () -> ModelBakery.ModelGroupKey.create(param3, var17, var5))));
                        } else {
                            var17 = null;
                        }

                        var15.getVariants()
                            .forEach(
                                (param9, param10) -> {
                                    try {
                                        var6.stream()
                                            .filter(predicate(var4, param9))
                                            .forEach(
                                                param6x -> {
                                                    Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> var0x = var16.put(
                                                        param6x, Pair.of(param10, () -> ModelBakery.ModelGroupKey.create(param6x, param10, var5))
                                                    );
                                                    if (var0x != null && var0x.getFirst() != var17) {
                                                        var16.put(param6x, var12);
                                                        throw new RuntimeException(
                                                            "Overlapping definition with: "
                                                                + (String)var15.getVariants()
                                                                    .entrySet()
                                                                    .stream()
                                                                    .filter(param1x -> param1x.getValue() == var0x.getFirst())
                                                                    .findFirst()
                                                                    .get()
                                                                    .getKey()
                                                        );
                                                    }
                                                }
                                            );
                                    } catch (Exception var12x) {
                                        LOGGER.warn(
                                            "Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}",
                                            var9,
                                            var14.getFirst(),
                                            param9,
                                            var12x.getMessage()
                                        );
                                    }
        
                                }
                            );
                        var8.putAll(var16);
                    }
                } catch (ModelBakery.BlockStateDefinitionException var24) {
                    throw var24;
                } catch (Exception var25) {
                    throw new ModelBakery.BlockStateDefinitionException(
                        String.format(Locale.ROOT, "Exception loading blockstate definition: '%s': %s", var9, var25)
                    );
                } finally {
                    HashMap var22 = Maps.newHashMap();
                    var7.forEach((param4, param5) -> {
                        Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> var0x = var8.get(param5);
                        if (var0x == null) {
                            LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", var9, param4);
                            var0x = var12;
                        }

                        this.cacheAndQueueDependencies(param4, var0x.getFirst());

                        try {
                            ModelBakery.ModelGroupKey var1x = var0x.getSecond().get();
                            var22.computeIfAbsent(var1x, param0x -> Sets.newIdentityHashSet()).add(param5);
                        } catch (Exception var9x) {
                            LOGGER.warn("Exception evaluating model definition: '{}'", param4, var9x);
                        }

                    });
                    var22.forEach((param0x, param1) -> {
                        Iterator<BlockState> var0x = param1.iterator();

                        while(var0x.hasNext()) {
                            BlockState var1x = var0x.next();
                            if (var1x.getRenderShape() != RenderShape.MODEL) {
                                var0x.remove();
                                this.modelGroups.put(var1x, 0);
                            }
                        }

                        if (param1.size() > 1) {
                            this.registerModelGroup(param1);
                        }

                    });
                }
            }

        }
    }

    private void cacheAndQueueDependencies(ResourceLocation param0, UnbakedModel param1) {
        this.unbakedCache.put(param0, param1);
        this.loadingStack.addAll(param1.getDependencies());
    }

    private void loadTopLevel(ModelResourceLocation param0) {
        UnbakedModel var0 = this.getModel(param0);
        this.unbakedCache.put(param0, var0);
        this.topLevelModels.put(param0, var0);
    }

    private void registerModelGroup(Iterable<BlockState> param0) {
        int var0 = this.nextModelGroup++;
        param0.forEach(param1 -> this.modelGroups.put(param1, var0));
    }

    private BlockModel loadBlockModel(ResourceLocation param0) throws IOException {
        String var0 = param0.getPath();
        if ("builtin/generated".equals(var0)) {
            return GENERATION_MARKER;
        } else if ("builtin/entity".equals(var0)) {
            return BLOCK_ENTITY_MARKER;
        } else if (var0.startsWith("builtin/")) {
            String var1 = var0.substring("builtin/".length());
            String var2 = BUILTIN_MODELS.get(var1);
            if (var2 == null) {
                throw new FileNotFoundException(param0.toString());
            } else {
                Reader var3 = new StringReader(var2);
                BlockModel var4 = BlockModel.fromStream(var3);
                var4.name = param0.toString();
                return var4;
            }
        } else {
            ResourceLocation var5 = MODEL_LISTER.idToFile(param0);
            BlockModel var6 = this.modelResources.get(var5);
            if (var6 == null) {
                throw new FileNotFoundException(var5.toString());
            } else {
                var6.name = param0.toString();
                return var6;
            }
        }
    }

    public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() {
        return this.bakedTopLevelModels;
    }

    public Object2IntMap<BlockState> getModelGroups() {
        return this.modelGroups;
    }

    @OnlyIn(Dist.CLIENT)
    static record BakedCacheKey(ResourceLocation id, Transformation transformation, boolean isUvLocked) {
    }

    @OnlyIn(Dist.CLIENT)
    static class BlockStateDefinitionException extends RuntimeException {
        public BlockStateDefinitionException(String param0) {
            super(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record LoadedJson(String source, JsonElement data) {
    }

    @OnlyIn(Dist.CLIENT)
    class ModelBakerImpl implements ModelBaker {
        private final Function<Material, TextureAtlasSprite> modelTextureGetter;

        ModelBakerImpl(BiFunction<ResourceLocation, Material, TextureAtlasSprite> param0, ResourceLocation param1) {
            this.modelTextureGetter = param2 -> param0.apply(param1, param2);
        }

        @Override
        public UnbakedModel getModel(ResourceLocation param0) {
            return ModelBakery.this.getModel(param0);
        }

        @Override
        public BakedModel bake(ResourceLocation param0, ModelState param1) {
            ModelBakery.BakedCacheKey var0 = new ModelBakery.BakedCacheKey(param0, param1.getRotation(), param1.isUvLocked());
            BakedModel var1 = ModelBakery.this.bakedCache.get(var0);
            if (var1 != null) {
                return var1;
            } else {
                UnbakedModel var2 = this.getModel(param0);
                if (var2 instanceof BlockModel var3 && var3.getRootModel() == ModelBakery.GENERATION_MARKER) {
                    return ModelBakery.ITEM_MODEL_GENERATOR
                        .generateBlockModel(this.modelTextureGetter, var3)
                        .bake(this, var3, this.modelTextureGetter, param1, param0, false);
                }

                BakedModel var4 = var2.bake(this, this.modelTextureGetter, param1, param0);
                ModelBakery.this.bakedCache.put(var0, var4);
                return var4;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ModelGroupKey {
        private final List<UnbakedModel> models;
        private final List<Object> coloringValues;

        public ModelGroupKey(List<UnbakedModel> param0, List<Object> param1) {
            this.models = param0;
            this.coloringValues = param1;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof ModelBakery.ModelGroupKey)) {
                return false;
            } else {
                ModelBakery.ModelGroupKey var0 = (ModelBakery.ModelGroupKey)param0;
                return Objects.equals(this.models, var0.models) && Objects.equals(this.coloringValues, var0.coloringValues);
            }
        }

        @Override
        public int hashCode() {
            return 31 * this.models.hashCode() + this.coloringValues.hashCode();
        }

        public static ModelBakery.ModelGroupKey create(BlockState param0, MultiPart param1, Collection<Property<?>> param2) {
            StateDefinition<Block, BlockState> var0 = param0.getBlock().getStateDefinition();
            List<UnbakedModel> var1 = param1.getSelectors()
                .stream()
                .filter(param2x -> param2x.getPredicate(var0).test(param0))
                .map(Selector::getVariant)
                .collect(ImmutableList.toImmutableList());
            List<Object> var2 = getColoringValues(param0, param2);
            return new ModelBakery.ModelGroupKey(var1, var2);
        }

        public static ModelBakery.ModelGroupKey create(BlockState param0, UnbakedModel param1, Collection<Property<?>> param2) {
            List<Object> var0 = getColoringValues(param0, param2);
            return new ModelBakery.ModelGroupKey(ImmutableList.of(param1), var0);
        }

        private static List<Object> getColoringValues(BlockState param0, Collection<Property<?>> param1) {
            return param1.stream().map(param0::getValue).collect(ImmutableList.toImmutableList());
        }
    }
}
