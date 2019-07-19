package net.minecraft.client.gui.screens;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class MenuScreens {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> SCREENS = Maps.newHashMap();

    public static <T extends AbstractContainerMenu> void create(@Nullable MenuType<T> param0, Minecraft param1, int param2, Component param3) {
        if (param0 == null) {
            LOGGER.warn("Trying to open invalid screen with name: {}", param3.getString());
        } else {
            MenuScreens.ScreenConstructor<T, ?> var0 = getConstructor(param0);
            if (var0 == null) {
                LOGGER.warn("Failed to create screen for menu type: {}", Registry.MENU.getKey(param0));
            } else {
                var0.fromPacket(param3, param0, param1, param2);
            }
        }
    }

    @Nullable
    private static <T extends AbstractContainerMenu> MenuScreens.ScreenConstructor<T, ?> getConstructor(MenuType<T> param0) {
        return (MenuScreens.ScreenConstructor<T, ?>)SCREENS.get(param0);
    }

    private static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
        MenuType<? extends M> param0, MenuScreens.ScreenConstructor<M, U> param1
    ) {
        MenuScreens.ScreenConstructor<?, ?> var0 = SCREENS.put(param0, param1);
        if (var0 != null) {
            throw new IllegalStateException("Duplicate registration for " + Registry.MENU.getKey(param0));
        }
    }

    public static boolean selfTest() {
        boolean var0 = false;

        for(MenuType<?> var1 : Registry.MENU) {
            if (!SCREENS.containsKey(var1)) {
                LOGGER.debug("Menu {} has no matching screen", Registry.MENU.getKey(var1));
                var0 = true;
            }
        }

        return var0;
    }

    static {
        register(MenuType.GENERIC_9x1, ContainerScreen::new);
        register(MenuType.GENERIC_9x2, ContainerScreen::new);
        register(MenuType.GENERIC_9x3, ContainerScreen::new);
        register(MenuType.GENERIC_9x4, ContainerScreen::new);
        register(MenuType.GENERIC_9x5, ContainerScreen::new);
        register(MenuType.GENERIC_9x6, ContainerScreen::new);
        register(MenuType.GENERIC_3x3, DispenserScreen::new);
        register(MenuType.ANVIL, AnvilScreen::new);
        register(MenuType.BEACON, BeaconScreen::new);
        register(MenuType.BLAST_FURNACE, BlastFurnaceScreen::new);
        register(MenuType.BREWING_STAND, BrewingStandScreen::new);
        register(MenuType.CRAFTING, CraftingScreen::new);
        register(MenuType.ENCHANTMENT, EnchantmentScreen::new);
        register(MenuType.FURNACE, FurnaceScreen::new);
        register(MenuType.GRINDSTONE, GrindstoneScreen::new);
        register(MenuType.HOPPER, HopperScreen::new);
        register(MenuType.LECTERN, LecternScreen::new);
        register(MenuType.LOOM, LoomScreen::new);
        register(MenuType.MERCHANT, MerchantScreen::new);
        register(MenuType.SHULKER_BOX, ShulkerBoxScreen::new);
        register(MenuType.SMOKER, SmokerScreen::new);
        register(MenuType.CARTOGRAPHY, CartographyScreen::new);
        register(MenuType.STONECUTTER, StonecutterScreen::new);
    }

    @OnlyIn(Dist.CLIENT)
    interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        default void fromPacket(Component param0, MenuType<T> param1, Minecraft param2, int param3) {
            U var0 = this.create(param1.create(param3, param2.player.inventory), param2.player.inventory, param0);
            param2.player.containerMenu = var0.getMenu();
            param2.setScreen(var0);
        }

        U create(T var1, Inventory var2, Component var3);
    }
}
