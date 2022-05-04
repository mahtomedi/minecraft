package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternItem extends Item {
    private final TagKey<BannerPattern> bannerPattern;

    public BannerPatternItem(TagKey<BannerPattern> param0, Item.Properties param1) {
        super(param1);
        this.bannerPattern = param0;
    }

    public TagKey<BannerPattern> getBannerPattern() {
        return this.bannerPattern;
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        param2.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.getDescriptionId() + ".desc");
    }
}
