package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StatsScreen extends Screen implements StatsUpdateListener {
    private static final Component PENDING_TEXT = new TranslatableComponent("multiplayer.downloadingStats");
    protected final Screen lastScreen;
    private StatsScreen.GeneralStatisticsList statsList;
    private StatsScreen.ItemStatisticsList itemStatsList;
    private StatsScreen.MobsStatisticsList mobsStatsList;
    private final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    private boolean isLoading = true;

    public StatsScreen(Screen param0, StatsCounter param1) {
        super(new TranslatableComponent("gui.stats"));
        this.lastScreen = param0;
        this.stats = param1;
    }

    @Override
    protected void init() {
        this.isLoading = true;
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public void initLists() {
        this.statsList = new StatsScreen.GeneralStatisticsList(this.minecraft);
        this.itemStatsList = new StatsScreen.ItemStatisticsList(this.minecraft);
        this.mobsStatsList = new StatsScreen.MobsStatisticsList(this.minecraft);
    }

    public void initButtons() {
        this.addButton(
            new Button(
                this.width / 2 - 120, this.height - 52, 80, 20, new TranslatableComponent("stat.generalButton"), param0 -> this.setActiveList(this.statsList)
            )
        );
        Button var0 = this.addButton(
            new Button(
                this.width / 2 - 40, this.height - 52, 80, 20, new TranslatableComponent("stat.itemsButton"), param0 -> this.setActiveList(this.itemStatsList)
            )
        );
        Button var1 = this.addButton(
            new Button(
                this.width / 2 + 40, this.height - 52, 80, 20, new TranslatableComponent("stat.mobsButton"), param0 -> this.setActiveList(this.mobsStatsList)
            )
        );
        this.addButton(
            new Button(this.width / 2 - 100, this.height - 28, 200, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        if (this.itemStatsList.children().isEmpty()) {
            var0.active = false;
        }

        if (this.mobsStatsList.children().isEmpty()) {
            var1.active = false;
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.isLoading) {
            this.renderBackground(param0);
            drawCenteredString(param0, this.font, PENDING_TEXT, this.width / 2, this.height / 2, 16777215);
            drawCenteredString(
                param0,
                this.font,
                LOADING_SYMBOLS[(int)(Util.getMillis() / 150L % (long)LOADING_SYMBOLS.length)],
                this.width / 2,
                this.height / 2 + 9 * 2,
                16777215
            );
        } else {
            this.getActiveList().render(param0, param1, param2, param3);
            drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
            super.render(param0, param1, param2, param3);
        }

    }

    @Override
    public void onStatsUpdated() {
        if (this.isLoading) {
            this.initLists();
            this.initButtons();
            this.setActiveList(this.statsList);
            this.isLoading = false;
        }

    }

    @Override
    public boolean isPauseScreen() {
        return !this.isLoading;
    }

    @Nullable
    public ObjectSelectionList<?> getActiveList() {
        return this.activeList;
    }

    public void setActiveList(@Nullable ObjectSelectionList<?> param0) {
        this.children.remove(this.statsList);
        this.children.remove(this.itemStatsList);
        this.children.remove(this.mobsStatsList);
        if (param0 != null) {
            this.children.add(0, param0);
            this.activeList = param0;
        }

    }

    private static String getTranslationKey(Stat<ResourceLocation> param0) {
        return "stat." + param0.getValue().toString().replace(':', '.');
    }

    private int getColumnX(int param0) {
        return 115 + 40 * param0;
    }

    private void blitSlot(PoseStack param0, int param1, int param2, Item param3) {
        this.blitSlotIcon(param0, param1 + 1, param2 + 1, 0, 0);
        RenderSystem.enableRescaleNormal();
        this.itemRenderer.renderGuiItem(param3.getDefaultInstance(), param1 + 2, param2 + 2);
        RenderSystem.disableRescaleNormal();
    }

    private void blitSlotIcon(PoseStack param0, int param1, int param2, int param3, int param4) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(STATS_ICON_LOCATION);
        blit(param0, param1, param2, this.getBlitOffset(), (float)param3, (float)param4, 18, 18, 128, 128);
    }

    @OnlyIn(Dist.CLIENT)
    class GeneralStatisticsList extends ObjectSelectionList<StatsScreen.GeneralStatisticsList.Entry> {
        public GeneralStatisticsList(Minecraft param0) {
            super(param0, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
            ObjectArrayList<Stat<ResourceLocation>> param1 = new ObjectArrayList<>(Stats.CUSTOM.iterator());
            param1.sort(Comparator.comparing(param0x -> I18n.get(StatsScreen.getTranslationKey(param0x))));

            for(Stat<ResourceLocation> var0 : param1) {
                this.addEntry(new StatsScreen.GeneralStatisticsList.Entry(var0));
            }

        }

        @Override
        protected void renderBackground(PoseStack param0) {
            StatsScreen.this.renderBackground(param0);
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ObjectSelectionList.Entry<StatsScreen.GeneralStatisticsList.Entry> {
            private final Stat<ResourceLocation> stat;
            private final Component statDisplay;

            private Entry(Stat<ResourceLocation> param0) {
                this.stat = param0;
                this.statDisplay = new TranslatableComponent(StatsScreen.getTranslationKey(param0));
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                GuiComponent.drawString(param0, StatsScreen.this.font, this.statDisplay, param3 + 2, param2 + 1, param1 % 2 == 0 ? 16777215 : 9474192);
                String var0 = this.stat.format(StatsScreen.this.stats.getValue(this.stat));
                GuiComponent.drawString(
                    param0, StatsScreen.this.font, var0, param3 + 2 + 213 - StatsScreen.this.font.width(var0), param2 + 1, param1 % 2 == 0 ? 16777215 : 9474192
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ItemStatisticsList extends ObjectSelectionList<StatsScreen.ItemStatisticsList.ItemRow> {
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        private final int[] iconOffsets = new int[]{3, 4, 1, 2, 5, 6};
        protected int headerPressed = -1;
        protected final List<Item> statItemList;
        protected final Comparator<Item> itemStatSorter = new StatsScreen.ItemStatisticsList.ItemComparator();
        @Nullable
        protected StatType<?> sortColumn;
        protected int sortOrder;

        public ItemStatisticsList(Minecraft param0) {
            super(param0, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
            this.blockColumns = Lists.newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
            this.setRenderHeader(true, 20);
            Set<Item> param1 = Sets.newIdentityHashSet();

            for(Item var0 : Registry.ITEM) {
                boolean var1 = false;

                for(StatType<Item> var2 : this.itemColumns) {
                    if (var2.contains(var0) && StatsScreen.this.stats.getValue(var2.get(var0)) > 0) {
                        var1 = true;
                    }
                }

                if (var1) {
                    param1.add(var0);
                }
            }

            for(Block var3 : Registry.BLOCK) {
                boolean var4 = false;

                for(StatType<Block> var5 : this.blockColumns) {
                    if (var5.contains(var3) && StatsScreen.this.stats.getValue(var5.get(var3)) > 0) {
                        var4 = true;
                    }
                }

                if (var4) {
                    param1.add(var3.asItem());
                }
            }

            param1.remove(Items.AIR);
            this.statItemList = Lists.newArrayList(param1);

            for(int var6 = 0; var6 < this.statItemList.size(); ++var6) {
                this.addEntry(new StatsScreen.ItemStatisticsList.ItemRow());
            }

        }

        @Override
        protected void renderHeader(PoseStack param0, int param1, int param2, Tesselator param3) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }

            for(int var0 = 0; var0 < this.iconOffsets.length; ++var0) {
                StatsScreen.this.blitSlotIcon(param0, param1 + StatsScreen.this.getColumnX(var0) - 18, param2 + 1, 0, this.headerPressed == var0 ? 0 : 18);
            }

            if (this.sortColumn != null) {
                int var1 = StatsScreen.this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                int var2 = this.sortOrder == 1 ? 2 : 1;
                StatsScreen.this.blitSlotIcon(param0, param1 + var1, param2 + 1, 18 * var2, 0);
            }

            for(int var3 = 0; var3 < this.iconOffsets.length; ++var3) {
                int var4 = this.headerPressed == var3 ? 1 : 0;
                StatsScreen.this.blitSlotIcon(
                    param0, param1 + StatsScreen.this.getColumnX(var3) - 18 + var4, param2 + 1 + var4, 18 * this.iconOffsets[var3], 18
                );
            }

        }

        @Override
        public int getRowWidth() {
            return 375;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 140;
        }

        @Override
        protected void renderBackground(PoseStack param0) {
            StatsScreen.this.renderBackground(param0);
        }

        @Override
        protected void clickedHeader(int param0, int param1) {
            this.headerPressed = -1;

            for(int var0 = 0; var0 < this.iconOffsets.length; ++var0) {
                int var1 = param0 - StatsScreen.this.getColumnX(var0);
                if (var1 >= -36 && var1 <= 0) {
                    this.headerPressed = var0;
                    break;
                }
            }

            if (this.headerPressed >= 0) {
                this.sortByColumn(this.getColumn(this.headerPressed));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }

        }

        private StatType<?> getColumn(int param0) {
            return param0 < this.blockColumns.size() ? this.blockColumns.get(param0) : this.itemColumns.get(param0 - this.blockColumns.size());
        }

        private int getColumnIndex(StatType<?> param0) {
            int var0 = this.blockColumns.indexOf(param0);
            if (var0 >= 0) {
                return var0;
            } else {
                int var1 = this.itemColumns.indexOf(param0);
                return var1 >= 0 ? var1 + this.blockColumns.size() : -1;
            }
        }

        @Override
        protected void renderDecorations(PoseStack param0, int param1, int param2) {
            if (param2 >= this.y0 && param2 <= this.y1) {
                StatsScreen.ItemStatisticsList.ItemRow var0 = this.getEntryAtPosition((double)param1, (double)param2);
                int var1 = (this.width - this.getRowWidth()) / 2;
                if (var0 != null) {
                    if (param1 < var1 + 40 || param1 > var1 + 40 + 20) {
                        return;
                    }

                    Item var2 = this.statItemList.get(this.children().indexOf(var0));
                    this.renderMousehoverTooltip(param0, this.getString(var2), param1, param2);
                } else {
                    Component var3 = null;
                    int var4 = param1 - var1;

                    for(int var5 = 0; var5 < this.iconOffsets.length; ++var5) {
                        int var6 = StatsScreen.this.getColumnX(var5);
                        if (var4 >= var6 - 18 && var4 <= var6) {
                            var3 = this.getColumn(var5).getDisplayName();
                            break;
                        }
                    }

                    this.renderMousehoverTooltip(param0, var3, param1, param2);
                }

            }
        }

        protected void renderMousehoverTooltip(PoseStack param0, @Nullable Component param1, int param2, int param3) {
            if (param1 != null) {
                int var0 = param2 + 12;
                int var1 = param3 - 12;
                int var2 = StatsScreen.this.font.width(param1);
                this.fillGradient(param0, var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(0.0F, 0.0F, 400.0F);
                StatsScreen.this.font.drawShadow(param0, param1, (float)var0, (float)var1, -1);
                RenderSystem.popMatrix();
            }
        }

        protected Component getString(Item param0) {
            return param0.getDescription();
        }

        protected void sortByColumn(StatType<?> param0) {
            if (param0 != this.sortColumn) {
                this.sortColumn = param0;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }

            this.statItemList.sort(this.itemStatSorter);
        }

        @OnlyIn(Dist.CLIENT)
        class ItemComparator implements Comparator<Item> {
            private ItemComparator() {
            }

            public int compare(Item param0, Item param1) {
                int var0;
                int var1;
                if (ItemStatisticsList.this.sortColumn == null) {
                    var0 = 0;
                    var1 = 0;
                } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                    StatType<Block> var2 = ItemStatisticsList.this.sortColumn;
                    var0 = param0 instanceof BlockItem ? StatsScreen.this.stats.getValue(var2, ((BlockItem)param0).getBlock()) : -1;
                    var1 = param1 instanceof BlockItem ? StatsScreen.this.stats.getValue(var2, ((BlockItem)param1).getBlock()) : -1;
                } else {
                    StatType<Item> var5 = ItemStatisticsList.this.sortColumn;
                    var0 = StatsScreen.this.stats.getValue(var5, param0);
                    var1 = StatsScreen.this.stats.getValue(var5, param1);
                }

                return var0 == var1
                    ? ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(param0), Item.getId(param1))
                    : ItemStatisticsList.this.sortOrder * Integer.compare(var0, var1);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class ItemRow extends ObjectSelectionList.Entry<StatsScreen.ItemStatisticsList.ItemRow> {
            private ItemRow() {
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                Item var0 = StatsScreen.this.itemStatsList.statItemList.get(param1);
                StatsScreen.this.blitSlot(param0, param3 + 40, param2, var0);

                for(int var1 = 0; var1 < StatsScreen.this.itemStatsList.blockColumns.size(); ++var1) {
                    Stat<Block> var2;
                    if (var0 instanceof BlockItem) {
                        var2 = StatsScreen.this.itemStatsList.blockColumns.get(var1).get(((BlockItem)var0).getBlock());
                    } else {
                        var2 = null;
                    }

                    this.renderStat(param0, var2, param3 + StatsScreen.this.getColumnX(var1), param2, param1 % 2 == 0);
                }

                for(int var4 = 0; var4 < StatsScreen.this.itemStatsList.itemColumns.size(); ++var4) {
                    this.renderStat(
                        param0,
                        StatsScreen.this.itemStatsList.itemColumns.get(var4).get(var0),
                        param3 + StatsScreen.this.getColumnX(var4 + StatsScreen.this.itemStatsList.blockColumns.size()),
                        param2,
                        param1 % 2 == 0
                    );
                }

            }

            protected void renderStat(PoseStack param0, @Nullable Stat<?> param1, int param2, int param3, boolean param4) {
                String var0 = param1 == null ? "-" : param1.format(StatsScreen.this.stats.getValue(param1));
                GuiComponent.drawString(
                    param0, StatsScreen.this.font, var0, param2 - StatsScreen.this.font.width(var0), param3 + 5, param4 ? 16777215 : 9474192
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class MobsStatisticsList extends ObjectSelectionList<StatsScreen.MobsStatisticsList.MobRow> {
        public MobsStatisticsList(Minecraft param0) {
            super(param0, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 9 * 4);

            for(EntityType<?> param1 : Registry.ENTITY_TYPE) {
                if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(param1)) > 0
                    || StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(param1)) > 0) {
                    this.addEntry(new StatsScreen.MobsStatisticsList.MobRow(param1));
                }
            }

        }

        @Override
        protected void renderBackground(PoseStack param0) {
            StatsScreen.this.renderBackground(param0);
        }

        @OnlyIn(Dist.CLIENT)
        class MobRow extends ObjectSelectionList.Entry<StatsScreen.MobsStatisticsList.MobRow> {
            private final EntityType<?> type;
            private final Component mobName;
            private final Component kills;
            private final boolean hasKills;
            private final Component killedBy;
            private final boolean wasKilledBy;

            public MobRow(EntityType<?> param0) {
                this.type = param0;
                this.mobName = param0.getDescription();
                int param1 = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(param0));
                if (param1 == 0) {
                    this.kills = new TranslatableComponent("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = new TranslatableComponent("stat_type.minecraft.killed", param1, this.mobName);
                    this.hasKills = true;
                }

                int var0 = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(param0));
                if (var0 == 0) {
                    this.killedBy = new TranslatableComponent("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = new TranslatableComponent("stat_type.minecraft.killed_by", var0, this.mobName);
                    this.wasKilledBy = true;
                }

            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                GuiComponent.drawString(param0, StatsScreen.this.font, this.mobName, param3 + 2, param2 + 1, 16777215);
                GuiComponent.drawString(param0, StatsScreen.this.font, this.kills, param3 + 2 + 10, param2 + 1 + 9, this.hasKills ? 9474192 : 6316128);
                GuiComponent.drawString(param0, StatsScreen.this.font, this.killedBy, param3 + 2 + 10, param2 + 1 + 9 * 2, this.wasKilledBy ? 9474192 : 6316128);
            }
        }
    }
}
