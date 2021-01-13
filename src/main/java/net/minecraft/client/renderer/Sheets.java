package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Sheets {
    public static final ResourceLocation SHULKER_SHEET = new ResourceLocation("textures/atlas/shulker_boxes.png");
    public static final ResourceLocation BED_SHEET = new ResourceLocation("textures/atlas/beds.png");
    public static final ResourceLocation BANNER_SHEET = new ResourceLocation("textures/atlas/banner_patterns.png");
    public static final ResourceLocation SHIELD_SHEET = new ResourceLocation("textures/atlas/shield_patterns.png");
    public static final ResourceLocation SIGN_SHEET = new ResourceLocation("textures/atlas/signs.png");
    public static final ResourceLocation CHEST_SHEET = new ResourceLocation("textures/atlas/chest.png");
    private static final RenderType SHULKER_BOX_SHEET_TYPE = RenderType.entityCutoutNoCull(SHULKER_SHEET);
    private static final RenderType BED_SHEET_TYPE = RenderType.entitySolid(BED_SHEET);
    private static final RenderType BANNER_SHEET_TYPE = RenderType.entityNoOutline(BANNER_SHEET);
    private static final RenderType SHIELD_SHEET_TYPE = RenderType.entityNoOutline(SHIELD_SHEET);
    private static final RenderType SIGN_SHEET_TYPE = RenderType.entityCutoutNoCull(SIGN_SHEET);
    private static final RenderType CHEST_SHEET_TYPE = RenderType.entityCutout(CHEST_SHEET);
    private static final RenderType SOLID_BLOCK_SHEET = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType CUTOUT_BLOCK_SHEET = RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType TRANSLUCENT_ITEM_CULL_BLOCK_SHEET = RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType TRANSLUCENT_CULL_BLOCK_SHEET = RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
    public static final Material DEFAULT_SHULKER_TEXTURE_LOCATION = new Material(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker"));
    public static final List<Material> SHULKER_TEXTURE_LOCATION = Stream.of(
            "white",
            "orange",
            "magenta",
            "light_blue",
            "yellow",
            "lime",
            "pink",
            "gray",
            "light_gray",
            "cyan",
            "purple",
            "blue",
            "brown",
            "green",
            "red",
            "black"
        )
        .map(param0 -> new Material(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker_" + param0)))
        .collect(ImmutableList.toImmutableList());
    public static final Map<WoodType, Material> SIGN_MATERIALS = WoodType.values().collect(Collectors.toMap(Function.identity(), Sheets::signTexture));
    public static final Material[] BED_TEXTURES = Arrays.stream(DyeColor.values())
        .sorted(Comparator.comparingInt(DyeColor::getId))
        .map(param0 -> new Material(BED_SHEET, new ResourceLocation("entity/bed/" + param0.getName())))
        .toArray(param0 -> new Material[param0]);
    public static final Material CHEST_TRAP_LOCATION = chestMaterial("trapped");
    public static final Material CHEST_TRAP_LOCATION_LEFT = chestMaterial("trapped_left");
    public static final Material CHEST_TRAP_LOCATION_RIGHT = chestMaterial("trapped_right");
    public static final Material CHEST_XMAS_LOCATION = chestMaterial("christmas");
    public static final Material CHEST_XMAS_LOCATION_LEFT = chestMaterial("christmas_left");
    public static final Material CHEST_XMAS_LOCATION_RIGHT = chestMaterial("christmas_right");
    public static final Material CHEST_LOCATION = chestMaterial("normal");
    public static final Material CHEST_LOCATION_LEFT = chestMaterial("normal_left");
    public static final Material CHEST_LOCATION_RIGHT = chestMaterial("normal_right");
    public static final Material ENDER_CHEST_LOCATION = chestMaterial("ender");

    public static RenderType bannerSheet() {
        return BANNER_SHEET_TYPE;
    }

    public static RenderType shieldSheet() {
        return SHIELD_SHEET_TYPE;
    }

    public static RenderType bedSheet() {
        return BED_SHEET_TYPE;
    }

    public static RenderType shulkerBoxSheet() {
        return SHULKER_BOX_SHEET_TYPE;
    }

    public static RenderType signSheet() {
        return SIGN_SHEET_TYPE;
    }

    public static RenderType chestSheet() {
        return CHEST_SHEET_TYPE;
    }

    public static RenderType solidBlockSheet() {
        return SOLID_BLOCK_SHEET;
    }

    public static RenderType cutoutBlockSheet() {
        return CUTOUT_BLOCK_SHEET;
    }

    public static RenderType translucentItemSheet() {
        return TRANSLUCENT_ITEM_CULL_BLOCK_SHEET;
    }

    public static RenderType translucentCullBlockSheet() {
        return TRANSLUCENT_CULL_BLOCK_SHEET;
    }

    public static void getAllMaterials(Consumer<Material> param0) {
        param0.accept(DEFAULT_SHULKER_TEXTURE_LOCATION);
        SHULKER_TEXTURE_LOCATION.forEach(param0);

        for(BannerPattern var0 : BannerPattern.values()) {
            param0.accept(new Material(BANNER_SHEET, var0.location(true)));
            param0.accept(new Material(SHIELD_SHEET, var0.location(false)));
        }

        SIGN_MATERIALS.values().forEach(param0);

        for(Material var1 : BED_TEXTURES) {
            param0.accept(var1);
        }

        param0.accept(CHEST_TRAP_LOCATION);
        param0.accept(CHEST_TRAP_LOCATION_LEFT);
        param0.accept(CHEST_TRAP_LOCATION_RIGHT);
        param0.accept(CHEST_XMAS_LOCATION);
        param0.accept(CHEST_XMAS_LOCATION_LEFT);
        param0.accept(CHEST_XMAS_LOCATION_RIGHT);
        param0.accept(CHEST_LOCATION);
        param0.accept(CHEST_LOCATION_LEFT);
        param0.accept(CHEST_LOCATION_RIGHT);
        param0.accept(ENDER_CHEST_LOCATION);
    }

    public static Material signTexture(WoodType param0) {
        return new Material(SIGN_SHEET, new ResourceLocation("entity/signs/" + param0.name()));
    }

    private static Material chestMaterial(String param0) {
        return new Material(CHEST_SHEET, new ResourceLocation("entity/chest/" + param0));
    }

    public static Material chooseMaterial(BlockEntity param0, ChestType param1, boolean param2) {
        if (param2) {
            return chooseMaterial(param1, CHEST_XMAS_LOCATION, CHEST_XMAS_LOCATION_LEFT, CHEST_XMAS_LOCATION_RIGHT);
        } else if (param0 instanceof TrappedChestBlockEntity) {
            return chooseMaterial(param1, CHEST_TRAP_LOCATION, CHEST_TRAP_LOCATION_LEFT, CHEST_TRAP_LOCATION_RIGHT);
        } else {
            return param0 instanceof EnderChestBlockEntity
                ? ENDER_CHEST_LOCATION
                : chooseMaterial(param1, CHEST_LOCATION, CHEST_LOCATION_LEFT, CHEST_LOCATION_RIGHT);
        }
    }

    private static Material chooseMaterial(ChestType param0, Material param1, Material param2, Material param3) {
        switch(param0) {
            case LEFT:
                return param2;
            case RIGHT:
                return param3;
            case SINGLE:
            default:
                return param1;
        }
    }
}
