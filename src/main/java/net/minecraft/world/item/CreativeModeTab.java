package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class CreativeModeTab {
    public static final CreativeModeTab[] TABS = new CreativeModeTab[12];
    public static final CreativeModeTab TAB_BUILDING_BLOCKS = (new CreativeModeTab(0, "buildingBlocks") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.BRICKS);
        }
    }).setRecipeFolderName("building_blocks");
    public static final CreativeModeTab TAB_DECORATIONS = new CreativeModeTab(1, "decorations") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.PEONY);
        }
    };
    public static final CreativeModeTab TAB_REDSTONE = new CreativeModeTab(2, "redstone") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.REDSTONE);
        }
    };
    public static final CreativeModeTab TAB_TRANSPORTATION = new CreativeModeTab(3, "transportation") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.POWERED_RAIL);
        }
    };
    public static final CreativeModeTab TAB_MISC = new CreativeModeTab(6, "misc") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.LAVA_BUCKET);
        }
    };
    public static final CreativeModeTab TAB_SEARCH = (new CreativeModeTab(5, "search") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.COMPASS);
        }
    }).setBackgroundSuffix("item_search.png");
    public static final CreativeModeTab TAB_FOOD = new CreativeModeTab(7, "food") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.APPLE);
        }
    };
    public static final CreativeModeTab TAB_TOOLS = (new CreativeModeTab(8, "tools") {
            @OnlyIn(Dist.CLIENT)
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.IRON_AXE);
            }
        })
        .setEnchantmentCategories(
            new EnchantmentCategory[]{
                EnchantmentCategory.VANISHABLE, EnchantmentCategory.DIGGER, EnchantmentCategory.FISHING_ROD, EnchantmentCategory.BREAKABLE
            }
        );
    public static final CreativeModeTab TAB_COMBAT = (new CreativeModeTab(9, "combat") {
            @OnlyIn(Dist.CLIENT)
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.GOLDEN_SWORD);
            }
        })
        .setEnchantmentCategories(
            new EnchantmentCategory[]{
                EnchantmentCategory.VANISHABLE,
                EnchantmentCategory.ARMOR,
                EnchantmentCategory.ARMOR_FEET,
                EnchantmentCategory.ARMOR_HEAD,
                EnchantmentCategory.ARMOR_LEGS,
                EnchantmentCategory.ARMOR_CHEST,
                EnchantmentCategory.BOW,
                EnchantmentCategory.WEAPON,
                EnchantmentCategory.WEARABLE,
                EnchantmentCategory.BREAKABLE,
                EnchantmentCategory.TRIDENT,
                EnchantmentCategory.CROSSBOW
            }
        );
    public static final CreativeModeTab TAB_BREWING = new CreativeModeTab(10, "brewing") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        }
    };
    public static final CreativeModeTab TAB_MATERIALS = TAB_MISC;
    public static final CreativeModeTab TAB_HOTBAR = new CreativeModeTab(4, "hotbar") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.BOOKSHELF);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void fillItemList(NonNullList<ItemStack> param0) {
            throw new RuntimeException("Implement exception client-side.");
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public boolean isAlignedRight() {
            return true;
        }
    };
    public static final CreativeModeTab TAB_INVENTORY = (new CreativeModeTab(11, "inventory") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.CHEST);
        }
    }).setBackgroundSuffix("inventory.png").hideScroll().hideTitle();
    private final int id;
    private final String langId;
    private final Component displayName;
    private String recipeFolderName;
    private String backgroundSuffix = "items.png";
    private boolean canScroll = true;
    private boolean showTitle = true;
    private EnchantmentCategory[] enchantmentCategories = new EnchantmentCategory[0];
    private ItemStack iconItemStack;

    public CreativeModeTab(int param0, String param1) {
        this.id = param0;
        this.langId = param1;
        this.displayName = new TranslatableComponent("itemGroup." + param1);
        this.iconItemStack = ItemStack.EMPTY;
        TABS[param0] = this;
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    public String getRecipeFolderName() {
        return this.recipeFolderName == null ? this.langId : this.recipeFolderName;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDisplayName() {
        return this.displayName;
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getIconItem() {
        if (this.iconItemStack.isEmpty()) {
            this.iconItemStack = this.makeIcon();
        }

        return this.iconItemStack;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract ItemStack makeIcon();

    @OnlyIn(Dist.CLIENT)
    public String getBackgroundSuffix() {
        return this.backgroundSuffix;
    }

    public CreativeModeTab setBackgroundSuffix(String param0) {
        this.backgroundSuffix = param0;
        return this;
    }

    public CreativeModeTab setRecipeFolderName(String param0) {
        this.recipeFolderName = param0;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean showTitle() {
        return this.showTitle;
    }

    public CreativeModeTab hideTitle() {
        this.showTitle = false;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canScroll() {
        return this.canScroll;
    }

    public CreativeModeTab hideScroll() {
        this.canScroll = false;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public int getColumn() {
        return this.id % 6;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isTopRow() {
        return this.id < 6;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isAlignedRight() {
        return this.getColumn() == 5;
    }

    public EnchantmentCategory[] getEnchantmentCategories() {
        return this.enchantmentCategories;
    }

    public CreativeModeTab setEnchantmentCategories(EnchantmentCategory... param0) {
        this.enchantmentCategories = param0;
        return this;
    }

    public boolean hasEnchantmentCategory(@Nullable EnchantmentCategory param0) {
        if (param0 != null) {
            for(EnchantmentCategory var0 : this.enchantmentCategories) {
                if (var0 == param0) {
                    return true;
                }
            }
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void fillItemList(NonNullList<ItemStack> param0) {
        for(Item var0 : Registry.ITEM) {
            var0.fillItemCategory(this, param0);
        }

    }
}
