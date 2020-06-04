package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PackResourcesAdapterV4 implements PackResources {
    private static final Map<String, Pair<ChestType, ResourceLocation>> CHESTS = Util.make(
        Maps.newHashMap(),
        param0 -> {
            param0.put("textures/entity/chest/normal_left.png", new Pair<>(ChestType.LEFT, new ResourceLocation("textures/entity/chest/normal_double.png")));
            param0.put("textures/entity/chest/normal_right.png", new Pair<>(ChestType.RIGHT, new ResourceLocation("textures/entity/chest/normal_double.png")));
            param0.put("textures/entity/chest/normal.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/normal.png")));
            param0.put("textures/entity/chest/trapped_left.png", new Pair<>(ChestType.LEFT, new ResourceLocation("textures/entity/chest/trapped_double.png")));
            param0.put("textures/entity/chest/trapped_right.png", new Pair<>(ChestType.RIGHT, new ResourceLocation("textures/entity/chest/trapped_double.png")));
            param0.put("textures/entity/chest/trapped.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/trapped.png")));
            param0.put(
                "textures/entity/chest/christmas_left.png", new Pair<>(ChestType.LEFT, new ResourceLocation("textures/entity/chest/christmas_double.png"))
            );
            param0.put(
                "textures/entity/chest/christmas_right.png", new Pair<>(ChestType.RIGHT, new ResourceLocation("textures/entity/chest/christmas_double.png"))
            );
            param0.put("textures/entity/chest/christmas.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/christmas.png")));
            param0.put("textures/entity/chest/ender.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/ender.png")));
        }
    );
    private static final List<String> PATTERNS = Lists.newArrayList(
        "base",
        "border",
        "bricks",
        "circle",
        "creeper",
        "cross",
        "curly_border",
        "diagonal_left",
        "diagonal_right",
        "diagonal_up_left",
        "diagonal_up_right",
        "flower",
        "globe",
        "gradient",
        "gradient_up",
        "half_horizontal",
        "half_horizontal_bottom",
        "half_vertical",
        "half_vertical_right",
        "mojang",
        "rhombus",
        "skull",
        "small_stripes",
        "square_bottom_left",
        "square_bottom_right",
        "square_top_left",
        "square_top_right",
        "straight_cross",
        "stripe_bottom",
        "stripe_center",
        "stripe_downleft",
        "stripe_downright",
        "stripe_left",
        "stripe_middle",
        "stripe_right",
        "stripe_top",
        "triangle_bottom",
        "triangle_top",
        "triangles_bottom",
        "triangles_top"
    );
    private static final Set<String> SHIELDS = PATTERNS.stream().map(param0 -> "textures/entity/shield/" + param0 + ".png").collect(Collectors.toSet());
    private static final Set<String> BANNERS = PATTERNS.stream().map(param0 -> "textures/entity/banner/" + param0 + ".png").collect(Collectors.toSet());
    public static final ResourceLocation SHIELD_BASE = new ResourceLocation("textures/entity/shield_base.png");
    public static final ResourceLocation BANNER_BASE = new ResourceLocation("textures/entity/banner_base.png");
    public static final ResourceLocation OLD_IRON_GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem.png");
    private final PackResources pack;

    public PackResourcesAdapterV4(PackResources param0) {
        this.pack = param0;
    }

    @Override
    public InputStream getRootResource(String param0) throws IOException {
        return this.pack.getRootResource(param0);
    }

    @Override
    public boolean hasResource(PackType param0, ResourceLocation param1) {
        if (!"minecraft".equals(param1.getNamespace())) {
            return this.pack.hasResource(param0, param1);
        } else {
            String var0 = param1.getPath();
            if ("textures/misc/enchanted_item_glint.png".equals(var0)) {
                return false;
            } else if ("textures/entity/iron_golem/iron_golem.png".equals(var0)) {
                return this.pack.hasResource(param0, OLD_IRON_GOLEM_LOCATION);
            } else if ("textures/entity/conduit/wind.png".equals(var0) || "textures/entity/conduit/wind_vertical.png".equals(var0)) {
                return false;
            } else if (SHIELDS.contains(var0)) {
                return this.pack.hasResource(param0, SHIELD_BASE) && this.pack.hasResource(param0, param1);
            } else if (!BANNERS.contains(var0)) {
                Pair<ChestType, ResourceLocation> var1 = CHESTS.get(var0);
                return var1 != null && this.pack.hasResource(param0, var1.getSecond()) ? true : this.pack.hasResource(param0, param1);
            } else {
                return this.pack.hasResource(param0, BANNER_BASE) && this.pack.hasResource(param0, param1);
            }
        }
    }

    @Override
    public InputStream getResource(PackType param0, ResourceLocation param1) throws IOException {
        if (!"minecraft".equals(param1.getNamespace())) {
            return this.pack.getResource(param0, param1);
        } else {
            String var0 = param1.getPath();
            if ("textures/entity/iron_golem/iron_golem.png".equals(var0)) {
                return this.pack.getResource(param0, OLD_IRON_GOLEM_LOCATION);
            } else {
                if (SHIELDS.contains(var0)) {
                    InputStream var1 = fixPattern(this.pack.getResource(param0, SHIELD_BASE), this.pack.getResource(param0, param1), 64, 2, 2, 12, 22);
                    if (var1 != null) {
                        return var1;
                    }
                } else if (BANNERS.contains(var0)) {
                    InputStream var2 = fixPattern(this.pack.getResource(param0, BANNER_BASE), this.pack.getResource(param0, param1), 64, 0, 0, 42, 41);
                    if (var2 != null) {
                        return var2;
                    }
                } else {
                    if ("textures/entity/enderdragon/dragon.png".equals(var0) || "textures/entity/enderdragon/dragon_exploding.png".equals(var0)) {
                        ByteArrayInputStream var23;
                        try (NativeImage var3 = NativeImage.read(this.pack.getResource(param0, param1))) {
                            int var4 = var3.getWidth() / 256;

                            for(int var5 = 88 * var4; var5 < 200 * var4; ++var5) {
                                for(int var6 = 56 * var4; var6 < 112 * var4; ++var6) {
                                    var3.setPixelRGBA(var6, var5, 0);
                                }
                            }

                            var23 = new ByteArrayInputStream(var3.asByteArray());
                        }

                        return var23;
                    }

                    if ("textures/entity/conduit/closed_eye.png".equals(var0) || "textures/entity/conduit/open_eye.png".equals(var0)) {
                        return fixConduitEyeTexture(this.pack.getResource(param0, param1));
                    }

                    Pair<ChestType, ResourceLocation> var7 = CHESTS.get(var0);
                    if (var7 != null) {
                        ChestType var8 = var7.getFirst();
                        InputStream var9 = this.pack.getResource(param0, var7.getSecond());
                        if (var8 == ChestType.SINGLE) {
                            return fixSingleChest(var9);
                        }

                        if (var8 == ChestType.LEFT) {
                            return fixLeftChest(var9);
                        }

                        if (var8 == ChestType.RIGHT) {
                            return fixRightChest(var9);
                        }
                    }
                }

                return this.pack.getResource(param0, param1);
            }
        }
    }

    @Nullable
    public static InputStream fixPattern(InputStream param0, InputStream param1, int param2, int param3, int param4, int param5, int param6) throws IOException {
        ByteArrayInputStream var6;
        try (
            NativeImage var0 = NativeImage.read(param0);
            NativeImage var1 = NativeImage.read(param1);
        ) {
            int var2 = var0.getWidth();
            int var3 = var0.getHeight();
            if (var2 != var1.getWidth() || var3 != var1.getHeight()) {
                return null;
            }

            try (NativeImage var4 = new NativeImage(var2, var3, true)) {
                int var5 = var2 / param2;

                for(int var6 = param4 * var5; var6 < param6 * var5; ++var6) {
                    for(int var7 = param3 * var5; var7 < param5 * var5; ++var7) {
                        int var8 = NativeImage.getR(var1.getPixelRGBA(var7, var6));
                        int var9 = var0.getPixelRGBA(var7, var6);
                        var4.setPixelRGBA(var7, var6, NativeImage.combine(var8, NativeImage.getB(var9), NativeImage.getG(var9), NativeImage.getR(var9)));
                    }
                }

                var6 = new ByteArrayInputStream(var4.asByteArray());
            }
        }

        return var6;
    }

    public static InputStream fixConduitEyeTexture(InputStream param0) throws IOException {
        ByteArrayInputStream var7;
        try (NativeImage var0 = NativeImage.read(param0)) {
            int var1 = var0.getWidth();
            int var2 = var0.getHeight();

            try (NativeImage var3 = new NativeImage(2 * var1, 2 * var2, true)) {
                copyRect(var0, var3, 0, 0, 0, 0, var1, var2, 1, false, false);
                var7 = new ByteArrayInputStream(var3.asByteArray());
            }
        }

        return var7;
    }

    public static InputStream fixLeftChest(InputStream param0) throws IOException {
        ByteArrayInputStream var8;
        try (NativeImage var0 = NativeImage.read(param0)) {
            int var1 = var0.getWidth();
            int var2 = var0.getHeight();

            try (NativeImage var3 = new NativeImage(var1 / 2, var2, true)) {
                int var4 = var2 / 64;
                copyRect(var0, var3, 29, 0, 29, 0, 15, 14, var4, false, true);
                copyRect(var0, var3, 59, 0, 14, 0, 15, 14, var4, false, true);
                copyRect(var0, var3, 29, 14, 43, 14, 15, 5, var4, true, true);
                copyRect(var0, var3, 44, 14, 29, 14, 14, 5, var4, true, true);
                copyRect(var0, var3, 58, 14, 14, 14, 15, 5, var4, true, true);
                copyRect(var0, var3, 29, 19, 29, 19, 15, 14, var4, false, true);
                copyRect(var0, var3, 59, 19, 14, 19, 15, 14, var4, false, true);
                copyRect(var0, var3, 29, 33, 43, 33, 15, 10, var4, true, true);
                copyRect(var0, var3, 44, 33, 29, 33, 14, 10, var4, true, true);
                copyRect(var0, var3, 58, 33, 14, 33, 15, 10, var4, true, true);
                copyRect(var0, var3, 2, 0, 2, 0, 1, 1, var4, false, true);
                copyRect(var0, var3, 4, 0, 1, 0, 1, 1, var4, false, true);
                copyRect(var0, var3, 2, 1, 3, 1, 1, 4, var4, true, true);
                copyRect(var0, var3, 3, 1, 2, 1, 1, 4, var4, true, true);
                copyRect(var0, var3, 4, 1, 1, 1, 1, 4, var4, true, true);
                var8 = new ByteArrayInputStream(var3.asByteArray());
            }
        }

        return var8;
    }

    public static InputStream fixRightChest(InputStream param0) throws IOException {
        ByteArrayInputStream var8;
        try (NativeImage var0 = NativeImage.read(param0)) {
            int var1 = var0.getWidth();
            int var2 = var0.getHeight();

            try (NativeImage var3 = new NativeImage(var1 / 2, var2, true)) {
                int var4 = var2 / 64;
                copyRect(var0, var3, 14, 0, 29, 0, 15, 14, var4, false, true);
                copyRect(var0, var3, 44, 0, 14, 0, 15, 14, var4, false, true);
                copyRect(var0, var3, 0, 14, 0, 14, 14, 5, var4, true, true);
                copyRect(var0, var3, 14, 14, 43, 14, 15, 5, var4, true, true);
                copyRect(var0, var3, 73, 14, 14, 14, 15, 5, var4, true, true);
                copyRect(var0, var3, 14, 19, 29, 19, 15, 14, var4, false, true);
                copyRect(var0, var3, 44, 19, 14, 19, 15, 14, var4, false, true);
                copyRect(var0, var3, 0, 33, 0, 33, 14, 10, var4, true, true);
                copyRect(var0, var3, 14, 33, 43, 33, 15, 10, var4, true, true);
                copyRect(var0, var3, 73, 33, 14, 33, 15, 10, var4, true, true);
                copyRect(var0, var3, 1, 0, 2, 0, 1, 1, var4, false, true);
                copyRect(var0, var3, 3, 0, 1, 0, 1, 1, var4, false, true);
                copyRect(var0, var3, 0, 1, 0, 1, 1, 4, var4, true, true);
                copyRect(var0, var3, 1, 1, 3, 1, 1, 4, var4, true, true);
                copyRect(var0, var3, 5, 1, 1, 1, 1, 4, var4, true, true);
                var8 = new ByteArrayInputStream(var3.asByteArray());
            }
        }

        return var8;
    }

    public static InputStream fixSingleChest(InputStream param0) throws IOException {
        ByteArrayInputStream var8;
        try (NativeImage var0 = NativeImage.read(param0)) {
            int var1 = var0.getWidth();
            int var2 = var0.getHeight();

            try (NativeImage var3 = new NativeImage(var1, var2, true)) {
                int var4 = var2 / 64;
                copyRect(var0, var3, 14, 0, 28, 0, 14, 14, var4, false, true);
                copyRect(var0, var3, 28, 0, 14, 0, 14, 14, var4, false, true);
                copyRect(var0, var3, 0, 14, 0, 14, 14, 5, var4, true, true);
                copyRect(var0, var3, 14, 14, 42, 14, 14, 5, var4, true, true);
                copyRect(var0, var3, 28, 14, 28, 14, 14, 5, var4, true, true);
                copyRect(var0, var3, 42, 14, 14, 14, 14, 5, var4, true, true);
                copyRect(var0, var3, 14, 19, 28, 19, 14, 14, var4, false, true);
                copyRect(var0, var3, 28, 19, 14, 19, 14, 14, var4, false, true);
                copyRect(var0, var3, 0, 33, 0, 33, 14, 10, var4, true, true);
                copyRect(var0, var3, 14, 33, 42, 33, 14, 10, var4, true, true);
                copyRect(var0, var3, 28, 33, 28, 33, 14, 10, var4, true, true);
                copyRect(var0, var3, 42, 33, 14, 33, 14, 10, var4, true, true);
                copyRect(var0, var3, 1, 0, 3, 0, 2, 1, var4, false, true);
                copyRect(var0, var3, 3, 0, 1, 0, 2, 1, var4, false, true);
                copyRect(var0, var3, 0, 1, 0, 1, 1, 4, var4, true, true);
                copyRect(var0, var3, 1, 1, 4, 1, 2, 4, var4, true, true);
                copyRect(var0, var3, 3, 1, 3, 1, 1, 4, var4, true, true);
                copyRect(var0, var3, 4, 1, 1, 1, 2, 4, var4, true, true);
                var8 = new ByteArrayInputStream(var3.asByteArray());
            }
        }

        return var8;
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType param0, String param1, String param2, int param3, Predicate<String> param4) {
        return this.pack.getResources(param0, param1, param2, param3, param4);
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        return this.pack.getNamespaces(param0);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> param0) throws IOException {
        return this.pack.getMetadataSection(param0);
    }

    @Override
    public String getName() {
        return this.pack.getName();
    }

    @Override
    public void close() {
        this.pack.close();
    }

    private static void copyRect(
        NativeImage param0,
        NativeImage param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        int param8,
        boolean param9,
        boolean param10
    ) {
        param7 *= param8;
        param6 *= param8;
        param4 *= param8;
        param5 *= param8;
        param2 *= param8;
        param3 *= param8;

        for(int var0 = 0; var0 < param7; ++var0) {
            for(int var1 = 0; var1 < param6; ++var1) {
                param1.setPixelRGBA(
                    param4 + var1,
                    param5 + var0,
                    param0.getPixelRGBA(param2 + (param9 ? param6 - 1 - var1 : var1), param3 + (param10 ? param7 - 1 - var0 : var0))
                );
            }
        }

    }
}
