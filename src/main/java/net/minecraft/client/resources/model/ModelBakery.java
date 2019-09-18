package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ModelBakery {
    public static final ResourceLocation FIRE_0 = new ResourceLocation("block/fire_0");
    public static final ResourceLocation FIRE_1 = new ResourceLocation("block/fire_1");
    public static final ResourceLocation LAVA_FLOW = new ResourceLocation("block/lava_flow");
    public static final ResourceLocation WATER_FLOW = new ResourceLocation("block/water_flow");
    public static final ResourceLocation WATER_OVERLAY = new ResourceLocation("block/water_overlay");
    public static final ResourceLocation DEFAULT_SHULKER_TEXTURE_LOCATION = new ResourceLocation("entity/shulker/shulker");
    public static final List<ResourceLocation> SHULKER_TEXTURE_LOCATION = ImmutableList.of(
        new ResourceLocation("entity/shulker/shulker_white"),
        new ResourceLocation("entity/shulker/shulker_orange"),
        new ResourceLocation("entity/shulker/shulker_magenta"),
        new ResourceLocation("entity/shulker/shulker_light_blue"),
        new ResourceLocation("entity/shulker/shulker_yellow"),
        new ResourceLocation("entity/shulker/shulker_lime"),
        new ResourceLocation("entity/shulker/shulker_pink"),
        new ResourceLocation("entity/shulker/shulker_gray"),
        new ResourceLocation("entity/shulker/shulker_light_gray"),
        new ResourceLocation("entity/shulker/shulker_cyan"),
        new ResourceLocation("entity/shulker/shulker_purple"),
        new ResourceLocation("entity/shulker/shulker_blue"),
        new ResourceLocation("entity/shulker/shulker_brown"),
        new ResourceLocation("entity/shulker/shulker_green"),
        new ResourceLocation("entity/shulker/shulker_red"),
        new ResourceLocation("entity/shulker/shulker_black")
    );
    public static final ResourceLocation BANNER_BASE = new ResourceLocation("entity/banner_base");
    public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10)
        .mapToObj(param0 -> new ResourceLocation("block/destroy_stage_" + param0))
        .collect(Collectors.toList());
    private static final Set<ResourceLocation> UNREFERENCED_TEXTURES = Util.make(Sets.newHashSet(), param0 -> {
        param0.add(WATER_FLOW);
        param0.add(LAVA_FLOW);
        param0.add(WATER_OVERLAY);
        param0.add(FIRE_0);
        param0.add(FIRE_1);
        param0.add(BellRenderer.BELL_RESOURCE_LOCATION);
        param0.addAll(Arrays.asList(BedRenderer.TEXTURES));
        param0.add(ChestRenderer.CHEST_LARGE_TRAP_LOCATION);
        param0.add(ChestRenderer.CHEST_LARGE_XMAS_LOCATION);
        param0.add(ChestRenderer.CHEST_LARGE_LOCATION);
        param0.add(ChestRenderer.CHEST_TRAP_LOCATION);
        param0.add(ChestRenderer.CHEST_XMAS_LOCATION);
        param0.add(ChestRenderer.CHEST_LOCATION);
        param0.add(ChestRenderer.ENDER_CHEST_LOCATION);
        param0.add(ConduitRenderer.SHELL_TEXTURE);
        param0.add(ConduitRenderer.ACTIVE_SHELL_TEXTURE);
        param0.add(ConduitRenderer.WIND_TEXTURE);
        param0.add(ConduitRenderer.VERTICAL_WIND_TEXTURE);
        param0.add(ConduitRenderer.OPEN_EYE_TEXTURE);
        param0.add(ConduitRenderer.CLOSED_EYE_TEXTURE);
        param0.add(EnchantTableRenderer.BOOK_LOCATION);
        param0.add(DEFAULT_SHULKER_TEXTURE_LOCATION);
        param0.addAll(SHULKER_TEXTURE_LOCATION);
        param0.add(BANNER_BASE);

        for(BannerPattern var0 : BannerPattern.values()) {
            param0.add(var0.location());
        }

        param0.addAll(DESTROY_STAGES);
        param0.add(new ResourceLocation("item/empty_armor_slot_helmet"));
        param0.add(new ResourceLocation("item/empty_armor_slot_chestplate"));
        param0.add(new ResourceLocation("item/empty_armor_slot_leggings"));
        param0.add(new ResourceLocation("item/empty_armor_slot_boots"));
        param0.add(new ResourceLocation("item/empty_armor_slot_shield"));
    });
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ModelResourceLocation MISSING_MODEL_LOCATION = new ModelResourceLocation("builtin/missing", "missing");
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
    public static final BlockModel GENERATION_MARKER = Util.make(BlockModel.fromString("{}"), param0 -> param0.name = "generation marker");
    public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(BlockModel.fromString("{}"), param0 -> param0.name = "block entity marker");
    private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)
        .add(BooleanProperty.create("map"))
        .create(BlockState::new);
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(
        new ResourceLocation("item_frame"), ITEM_FRAME_FAKE_DEFINITION
    );
    private final ResourceManager resourceManager;
    private final TextureAtlas blockAtlas;
    private final BlockColors blockColors;
    private final Set<ResourceLocation> loadingStack = Sets.newHashSet();
    private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();
    private final Map<ResourceLocation, UnbakedModel> unbakedCache = Maps.newHashMap();
    private final Map<Triple<ResourceLocation, BlockModelRotation, Boolean>, BakedModel> bakedCache = Maps.newHashMap();
    private final Map<ResourceLocation, UnbakedModel> topLevelModels = Maps.newHashMap();
    private final Map<ResourceLocation, BakedModel> bakedTopLevelModels = Maps.newHashMap();
    private final TextureAtlas.Preparations atlasPreparations;
    private int nextModelGroup = 1;
    private final Object2IntMap<BlockState> modelGroups = Util.make(new Object2IntOpenHashMap<>(), param0x -> param0x.defaultReturnValue(-1));

    public ModelBakery(ResourceManager param0, TextureAtlas param1, BlockColors param2, ProfilerFiller param3) {
        this.resourceManager = param0;
        this.blockAtlas = param1;
        this.blockColors = param2;
        param3.push("missing_model");

        try {
            this.unbakedCache.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
            this.loadTopLevel(MISSING_MODEL_LOCATION);
        } catch (IOException var7) {
            LOGGER.error("Error loading missing model, should never happen :(", (Throwable)var7);
            throw new RuntimeException(var7);
        }

        param3.popPush("static_definitions");
        STATIC_DEFINITIONS.forEach(
            (param0x, param1x) -> param1x.getPossibleStates().forEach(param1xx -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(param0x, param1xx)))
        );
        param3.popPush("blocks");

        for(Block var1 : Registry.BLOCK) {
            var1.getStateDefinition().getPossibleStates().forEach(param0x -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(param0x)));
        }

        param3.popPush("items");

        for(ResourceLocation var2 : Registry.ITEM.keySet()) {
            this.loadTopLevel(new ModelResourceLocation(var2, "inventory"));
        }

        param3.popPush("special");
        this.loadTopLevel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        param3.popPush("textures");
        Set<String> var3 = Sets.newLinkedHashSet();
        Set<ResourceLocation> var4 = this.topLevelModels
            .values()
            .stream()
            .flatMap(param1x -> param1x.getTextures(this::getModel, var3).stream())
            .collect(Collectors.toSet());
        var4.addAll(UNREFERENCED_TEXTURES);
        var3.forEach(param0x -> LOGGER.warn("Unable to resolve texture reference: {}", param0x));
        param3.popPush("stitching");
        this.atlasPreparations = this.blockAtlas.prepareToStitch(this.resourceManager, var4, param3);
        param3.pop();
    }

    public void uploadTextures(ProfilerFiller param0) {
        param0.push("atlas");
        this.blockAtlas.reload(this.atlasPreparations);
        param0.popPush("baking");
        this.topLevelModels.keySet().forEach(param0x -> {
            BakedModel var0 = null;

            try {
                var0 = this.bake(param0x, BlockModelRotation.X0_Y0);
            } catch (Exception var4) {
                LOGGER.warn("Unable to bake model: '{}': {}", param0x, var4);
            }

            if (var0 != null) {
                this.bakedTopLevelModels.put(param0x, var0);
            }

        });
        param0.pop();
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
            if (param2 != null && var7 == param2.getBlock()) {
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
                ResourceLocation var1 = new ResourceLocation(param0.getNamespace(), "item/" + param0.getPath());
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
                var6.forEach(param2 -> BlockModelShaper.stateToModelLocation(var3, param2));
                Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> var8 = Maps.newHashMap();
                ResourceLocation var9 = new ResourceLocation(param0.getNamespace(), "blockstates/" + param0.getPath() + ".json");
                UnbakedModel var10 = this.unbakedCache.get(MISSING_MODEL_LOCATION);
                ModelBakery.ModelGroupKey var11 = new ModelBakery.ModelGroupKey(ImmutableList.of(var10), ImmutableList.of());
                Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> var12 = Pair.of(var10, () -> var11);

                try {
                    List<Pair<String, BlockModelDefinition>> var13;
                    try {
                        var13 = this.resourceManager
                            .getResources(var9)
                            .stream()
                            .map(
                                param0x -> {
                                    try (InputStream var0x = param0x.getInputStream()) {
                                        return Pair.of(
                                            param0x.getSourceName(),
                                            BlockModelDefinition.fromStream(this.context, new InputStreamReader(var0x, StandardCharsets.UTF_8))
                                        );
                                    } catch (Exception var16x) {
                                        throw new ModelBakery.BlockStateDefinitionException(
                                            String.format(
                                                "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s",
                                                param0x.getLocation(),
                                                param0x.getSourceName(),
                                                var16x.getMessage()
                                            )
                                        );
                                    }
                                }
                            )
                            .collect(Collectors.toList());
                    } catch (IOException var251) {
                        LOGGER.warn("Exception loading blockstate definition: {}: {}", var9, var251);
                        return;
                    }

                    for(Pair<String, BlockModelDefinition> var17 : var13) {
                        BlockModelDefinition var18 = var17.getSecond();
                        Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> var19 = Maps.newIdentityHashMap();
                        MultiPart var20;
                        if (var18.isMultiPart()) {
                            var20 = var18.getMultiPart();
                            var6.forEach(param3 -> {
                            });
                        } else {
                            var20 = null;
                        }

                        var18.getVariants()
                            .forEach(
                                (param9, param10) -> {
                                    try {
                                        var6.stream()
                                            .filter(predicate(var4, param9))
                                            .forEach(
                                                param6x -> {
                                                    Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> var0x = var19.put(
                                                        param6x, Pair.of(param10, () -> ModelBakery.ModelGroupKey.create(param6x, param10, var5))
                                                    );
                                                    if (var0x != null && var0x.getFirst() != var20) {
                                                        var19.put(param6x, var12);
                                                        throw new RuntimeException(
                                                            "Overlapping definition with: "
                                                                + (String)var18.getVariants()
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
                                            var17.getFirst(),
                                            param9,
                                            var12x.getMessage()
                                        );
                                    }
        
                                }
                            );
                        var8.putAll(var19);
                    }

                } catch (ModelBakery.BlockStateDefinitionException var26) {
                    throw var26;
                } catch (Exception var27) {
                    throw new ModelBakery.BlockStateDefinitionException(String.format("Exception loading blockstate definition: '%s': %s", var9, var27));
                } finally {
                    HashMap var25 = Maps.newHashMap();
                    var7.forEach((param4, param5) -> {
                        Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> var0x = var8.get(param5);
                        if (var0x == null) {
                            LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", var9, param4);
                            var0x = var12;
                        }

                        this.cacheAndQueueDependencies(param4, var0x.getFirst());

                        try {
                            ModelBakery.ModelGroupKey var1x = var0x.getSecond().get();
                            var25.computeIfAbsent(var1x, param0x -> Sets.newIdentityHashSet()).add(param5);
                        } catch (Exception var9x) {
                            LOGGER.warn("Exception evaluating model definition: '{}'", param4, var9x);
                        }

                    });
                    var25.forEach((param0x, param1) -> {
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

    @Nullable
    public BakedModel bake(ResourceLocation param0, ModelState param1) {
        Triple<ResourceLocation, BlockModelRotation, Boolean> var0 = Triple.of(param0, param1.getRotation(), param1.isUvLocked());
        if (this.bakedCache.containsKey(var0)) {
            return this.bakedCache.get(var0);
        } else {
            UnbakedModel var1 = this.getModel(param0);
            if (var1 instanceof BlockModel) {
                BlockModel var2 = (BlockModel)var1;
                if (var2.getRootModel() == GENERATION_MARKER) {
                    return ITEM_MODEL_GENERATOR.generateBlockModel(this.blockAtlas::getSprite, var2).bake(this, var2, this.blockAtlas::getSprite, param1);
                }
            }

            BakedModel var3 = var1.bake(this, this.blockAtlas::getSprite, param1);
            this.bakedCache.put(var0, var3);
            return var3;
        }
    }

    private BlockModel loadBlockModel(ResourceLocation param0) throws IOException {
        Reader var0 = null;
        Resource var1 = null;

        BlockModel var5;
        try {
            String var2 = param0.getPath();
            if (!"builtin/generated".equals(var2)) {
                if ("builtin/entity".equals(var2)) {
                    return BLOCK_ENTITY_MARKER;
                }

                if (var2.startsWith("builtin/")) {
                    String var3 = var2.substring("builtin/".length());
                    String var4 = BUILTIN_MODELS.get(var3);
                    if (var4 == null) {
                        throw new FileNotFoundException(param0.toString());
                    }

                    var0 = new StringReader(var4);
                } else {
                    var1 = this.resourceManager.getResource(new ResourceLocation(param0.getNamespace(), "models/" + param0.getPath() + ".json"));
                    var0 = new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8);
                }

                var5 = BlockModel.fromStream(var0);
                var5.name = param0.toString();
                return var5;
            }

            var5 = GENERATION_MARKER;
        } finally {
            IOUtils.closeQuietly(var0);
            IOUtils.closeQuietly((Closeable)var1);
        }

        return var5;
    }

    public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() {
        return this.bakedTopLevelModels;
    }

    public Object2IntMap<BlockState> getModelGroups() {
        return this.modelGroups;
    }

    @OnlyIn(Dist.CLIENT)
    static class BlockStateDefinitionException extends RuntimeException {
        public BlockStateDefinitionException(String param0) {
            super(param0);
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
