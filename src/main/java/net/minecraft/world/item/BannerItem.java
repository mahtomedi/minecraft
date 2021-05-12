package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;

public class BannerItem extends StandingAndWallBlockItem {
    private static final String PATTERN_PREFIX = "block.minecraft.banner.";

    public BannerItem(Block param0, Block param1, Item.Properties param2) {
        super(param0, param1, param2);
        Validate.isInstanceOf(AbstractBannerBlock.class, param0);
        Validate.isInstanceOf(AbstractBannerBlock.class, param1);
    }

    public static void appendHoverTextFromBannerBlockEntityTag(ItemStack param0, List<Component> param1) {
        CompoundTag var0 = param0.getTagElement("BlockEntityTag");
        if (var0 != null && var0.contains("Patterns")) {
            ListTag var1 = var0.getList("Patterns", 10);

            for(int var2 = 0; var2 < var1.size() && var2 < 6; ++var2) {
                CompoundTag var3 = var1.getCompound(var2);
                DyeColor var4 = DyeColor.byId(var3.getInt("Color"));
                BannerPattern var5 = BannerPattern.byHash(var3.getString("Pattern"));
                if (var5 != null) {
                    param1.add(new TranslatableComponent("block.minecraft.banner." + var5.getFilename() + "." + var4.getName()).withStyle(ChatFormatting.GRAY));
                }
            }

        }
    }

    public DyeColor getColor() {
        return ((AbstractBannerBlock)this.getBlock()).getColor();
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        appendHoverTextFromBannerBlockEntityTag(param0, param2);
    }
}
