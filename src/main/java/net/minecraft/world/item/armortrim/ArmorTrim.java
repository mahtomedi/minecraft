package net.minecraft.world.item.armortrim;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class ArmorTrim {
    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern)
                )
                .apply(param0, ArmorTrim::new)
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TAG_TRIM_ID = "Trim";
    private static final Component UPGRADE_TITLE = Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("smithing_template.upgrade")))
        .withStyle(ChatFormatting.GRAY);
    private final Holder<TrimMaterial> material;
    private final Holder<TrimPattern> pattern;
    private final Function<ArmorMaterial, ResourceLocation> innerTexture;
    private final Function<ArmorMaterial, ResourceLocation> outerTexture;

    public ArmorTrim(Holder<TrimMaterial> param0, Holder<TrimPattern> param1) {
        this.material = param0;
        this.pattern = param1;
        this.innerTexture = Util.memoize(param1x -> {
            ResourceLocation var0 = param1.value().assetId();
            String var1x = this.getColorPaletteSuffix(param1x);
            return var0.withPath(param1xx -> "trims/models/armor/" + param1xx + "_leggings_" + var1x);
        });
        this.outerTexture = Util.memoize(param1x -> {
            ResourceLocation var0 = param1.value().assetId();
            String var1x = this.getColorPaletteSuffix(param1x);
            return var0.withPath(param1xx -> "trims/models/armor/" + param1xx + "_" + var1x);
        });
    }

    private String getColorPaletteSuffix(ArmorMaterial param0) {
        Map<ArmorMaterials, String> var0 = this.material.value().overrideArmorMaterials();
        return param0 instanceof ArmorMaterials && var0.containsKey(param0) ? var0.get(param0) : this.material.value().assetName();
    }

    public boolean hasPatternAndMaterial(Holder<TrimPattern> param0, Holder<TrimMaterial> param1) {
        return param0 == this.pattern && param1 == this.material;
    }

    public Holder<TrimPattern> pattern() {
        return this.pattern;
    }

    public Holder<TrimMaterial> material() {
        return this.material;
    }

    public ResourceLocation innerTexture(ArmorMaterial param0) {
        return this.innerTexture.apply(param0);
    }

    public ResourceLocation outerTexture(ArmorMaterial param0) {
        return this.outerTexture.apply(param0);
    }

    @Override
    public boolean equals(Object param0) {
        if (!(param0 instanceof ArmorTrim)) {
            return false;
        } else {
            ArmorTrim var0 = (ArmorTrim)param0;
            return var0.pattern == this.pattern && var0.material == this.material;
        }
    }

    public static boolean setTrim(RegistryAccess param0, ItemStack param1, ArmorTrim param2) {
        if (param1.is(ItemTags.TRIMMABLE_ARMOR)) {
            param1.getOrCreateTag().put("Trim", CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, param0), param2).result().orElseThrow());
            return true;
        } else {
            return false;
        }
    }

    public static Optional<ArmorTrim> getTrim(RegistryAccess param0, ItemStack param1) {
        if (param1.is(ItemTags.TRIMMABLE_ARMOR) && param1.getTag() != null && param1.getTag().contains("Trim")) {
            CompoundTag var0 = param1.getTagElement("Trim");
            ArmorTrim var1 = CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, param0), var0).resultOrPartial(LOGGER::error).orElse(null);
            return Optional.ofNullable(var1);
        } else {
            return Optional.empty();
        }
    }

    public static void appendUpgradeHoverText(ItemStack param0, RegistryAccess param1, List<Component> param2) {
        Optional<ArmorTrim> var0 = getTrim(param1, param0);
        if (var0.isPresent()) {
            ArmorTrim var1 = var0.get();
            param2.add(UPGRADE_TITLE);
            param2.add(CommonComponents.space().append(var1.pattern().value().copyWithStyle(var1.material())));
            param2.add(CommonComponents.space().append(var1.material().value().description()));
        }

    }
}
