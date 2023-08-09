package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
    private static final ResourceLocation CHEST_SLOTS_SPRITE = new ResourceLocation("container/horse/chest_slots");
    private static final ResourceLocation SADDLE_SLOT_SPRITE = new ResourceLocation("container/horse/saddle_slot");
    private static final ResourceLocation LLAMA_ARMOR_SLOT_SPRITE = new ResourceLocation("container/horse/llama_armor_slot");
    private static final ResourceLocation ARMOR_SLOT_SPRITE = new ResourceLocation("container/horse/armor_slot");
    private static final ResourceLocation HORSE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/horse.png");
    private final AbstractHorse horse;
    private float xMouse;
    private float yMouse;

    public HorseInventoryScreen(HorseInventoryMenu param0, Inventory param1, AbstractHorse param2) {
        super(param0, param1, param2.getDisplayName());
        this.horse = param2;
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        param0.blit(HORSE_INVENTORY_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        AbstractHorse var8 = this.horse;
        if (var8 instanceof AbstractChestedHorse var2 && var2.hasChest()) {
            param0.blitSprite(CHEST_SLOTS_SPRITE, 90, 54, 0, 0, var0 + 79, var1 + 17, var2.getInventoryColumns() * 18, 54);
        }

        if (this.horse.isSaddleable()) {
            param0.blitSprite(SADDLE_SLOT_SPRITE, var0 + 7, var1 + 35 - 18, 18, 18);
        }

        if (this.horse.canWearArmor()) {
            if (this.horse instanceof Llama) {
                param0.blitSprite(LLAMA_ARMOR_SLOT_SPRITE, var0 + 7, var1 + 35, 18, 18);
            } else {
                param0.blitSprite(ARMOR_SLOT_SPRITE, var0 + 7, var1 + 35, 18, 18);
            }
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(param0, var0 + 26, var1 + 18, var0 + 78, var1 + 70, 17, 0.25F, this.xMouse, this.yMouse, this.horse);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.xMouse = (float)param1;
        this.yMouse = (float)param2;
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }
}
