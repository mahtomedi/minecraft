package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AirItem extends Item {
    private final Block block;

    public AirItem(Block param0, Item.Properties param1) {
        super(param1);
        this.block = param0;
    }

    @Override
    public String getDescriptionId() {
        return this.block.getDescriptionId();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        this.block.appendHoverText(param0, param1, param2, param3);
    }
}
