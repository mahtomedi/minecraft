package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureBlockEditScreen extends Screen {
    private static final Component NAME_LABEL = Component.translatable("structure_block.structure_name");
    private static final Component POSITION_LABEL = Component.translatable("structure_block.position");
    private static final Component SIZE_LABEL = Component.translatable("structure_block.size");
    private static final Component INTEGRITY_LABEL = Component.translatable("structure_block.integrity");
    private static final Component CUSTOM_DATA_LABEL = Component.translatable("structure_block.custom_data");
    private static final Component INCLUDE_ENTITIES_LABEL = Component.translatable("structure_block.include_entities");
    private static final Component DETECT_SIZE_LABEL = Component.translatable("structure_block.detect_size");
    private static final Component SHOW_AIR_LABEL = Component.translatable("structure_block.show_air");
    private static final Component SHOW_BOUNDING_BOX_LABEL = Component.translatable("structure_block.show_boundingbox");
    private static final ImmutableList<StructureMode> ALL_MODES = ImmutableList.copyOf(StructureMode.values());
    private static final ImmutableList<StructureMode> DEFAULT_MODES = ALL_MODES.stream()
        .filter(param0 -> param0 != StructureMode.DATA)
        .collect(ImmutableList.toImmutableList());
    private final StructureBlockEntity structure;
    private Mirror initialMirror = Mirror.NONE;
    private Rotation initialRotation = Rotation.NONE;
    private StructureMode initialMode = StructureMode.DATA;
    private boolean initialEntityIgnoring;
    private boolean initialShowAir;
    private boolean initialShowBoundingBox;
    private EditBox nameEdit;
    private EditBox posXEdit;
    private EditBox posYEdit;
    private EditBox posZEdit;
    private EditBox sizeXEdit;
    private EditBox sizeYEdit;
    private EditBox sizeZEdit;
    private EditBox integrityEdit;
    private EditBox seedEdit;
    private EditBox dataEdit;
    private Button saveButton;
    private Button loadButton;
    private Button rot0Button;
    private Button rot90Button;
    private Button rot180Button;
    private Button rot270Button;
    private Button detectButton;
    private CycleButton<Boolean> includeEntitiesButton;
    private CycleButton<Mirror> mirrorButton;
    private CycleButton<Boolean> toggleAirButton;
    private CycleButton<Boolean> toggleBoundingBox;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public StructureBlockEditScreen(StructureBlockEntity param0) {
        super(Component.translatable(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
        this.structure = param0;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.posXEdit.tick();
        this.posYEdit.tick();
        this.posZEdit.tick();
        this.sizeXEdit.tick();
        this.sizeYEdit.tick();
        this.sizeZEdit.tick();
        this.integrityEdit.tick();
        this.seedEdit.tick();
        this.dataEdit.tick();
    }

    private void onDone() {
        if (this.sendToServer(StructureBlockEntity.UpdateType.UPDATE_DATA)) {
            this.minecraft.setScreen(null);
        }

    }

    private void onCancel() {
        this.structure.setMirror(this.initialMirror);
        this.structure.setRotation(this.initialRotation);
        this.structure.setMode(this.initialMode);
        this.structure.setIgnoreEntities(this.initialEntityIgnoring);
        this.structure.setShowAir(this.initialShowAir);
        this.structure.setShowBoundingBox(this.initialShowBoundingBox);
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.initialMirror = this.structure.getMirror();
        this.initialRotation = this.structure.getRotation();
        this.initialMode = this.structure.getMode();
        this.initialEntityIgnoring = this.structure.isIgnoreEntities();
        this.initialShowAir = this.structure.getShowAir();
        this.initialShowBoundingBox = this.structure.getShowBoundingBox();
        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), param0 -> {
            if (this.structure.getMode() == StructureMode.SAVE) {
                this.sendToServer(StructureBlockEntity.UpdateType.SAVE_AREA);
                this.minecraft.setScreen(null);
            }

        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.loadButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.load"), param0 -> {
            if (this.structure.getMode() == StructureMode.LOAD) {
                this.sendToServer(StructureBlockEntity.UpdateType.LOAD_AREA);
                this.minecraft.setScreen(null);
            }

        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.addRenderableWidget(
            CycleButton.<StructureMode>builder(param0 -> Component.translatable("structure_block.mode." + param0.getSerializedName()))
                .withValues(DEFAULT_MODES, ALL_MODES)
                .displayOnlyValue()
                .withInitialValue(this.initialMode)
                .create(this.width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (param0, param1) -> {
                    this.structure.setMode(param1);
                    this.updateMode(param1);
                })
        );
        this.detectButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.detect_size"), param0 -> {
            if (this.structure.getMode() == StructureMode.SAVE) {
                this.sendToServer(StructureBlockEntity.UpdateType.SCAN_AREA);
                this.minecraft.setScreen(null);
            }

        }).bounds(this.width / 2 + 4 + 100, 120, 50, 20).build());
        this.includeEntitiesButton = this.addRenderableWidget(
            CycleButton.onOffBuilder(!this.structure.isIgnoreEntities())
                .displayOnlyValue()
                .create(this.width / 2 + 4 + 100, 160, 50, 20, INCLUDE_ENTITIES_LABEL, (param0, param1) -> this.structure.setIgnoreEntities(!param1))
        );
        this.mirrorButton = this.addRenderableWidget(
            CycleButton.builder(Mirror::symbol)
                .withValues(Mirror.values())
                .displayOnlyValue()
                .withInitialValue(this.initialMirror)
                .create(this.width / 2 - 20, 185, 40, 20, Component.literal("MIRROR"), (param0, param1) -> this.structure.setMirror(param1))
        );
        this.toggleAirButton = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.structure.getShowAir())
                .displayOnlyValue()
                .create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_AIR_LABEL, (param0, param1) -> this.structure.setShowAir(param1))
        );
        this.toggleBoundingBox = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.structure.getShowBoundingBox())
                .displayOnlyValue()
                .create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_LABEL, (param0, param1) -> this.structure.setShowBoundingBox(param1))
        );
        this.rot0Button = this.addRenderableWidget(Button.builder(Component.literal("0"), param0 -> {
            this.structure.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot90Button = this.addRenderableWidget(Button.builder(Component.literal("90"), param0 -> {
            this.structure.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot180Button = this.addRenderableWidget(Button.builder(Component.literal("180"), param0 -> {
            this.structure.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 20, 185, 40, 20).build());
        this.rot270Button = this.addRenderableWidget(Button.builder(Component.literal("270"), param0 -> {
            this.structure.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20).build());
        this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, Component.translatable("structure_block.structure_name")) {
            @Override
            public boolean charTyped(char param0, int param1) {
                return !StructureBlockEditScreen.this.isValidCharacterForName(this.getValue(), param0, this.getCursorPosition())
                    ? false
                    : super.charTyped(param0, param1);
            }
        };
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.structure.getStructureName());
        this.addWidget(this.nameEdit);
        BlockPos var0 = this.structure.getStructurePos();
        this.posXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.position.x"));
        this.posXEdit.setMaxLength(15);
        this.posXEdit.setValue(Integer.toString(var0.getX()));
        this.addWidget(this.posXEdit);
        this.posYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.position.y"));
        this.posYEdit.setMaxLength(15);
        this.posYEdit.setValue(Integer.toString(var0.getY()));
        this.addWidget(this.posYEdit);
        this.posZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.position.z"));
        this.posZEdit.setMaxLength(15);
        this.posZEdit.setValue(Integer.toString(var0.getZ()));
        this.addWidget(this.posZEdit);
        Vec3i var1 = this.structure.getStructureSize();
        this.sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.size.x"));
        this.sizeXEdit.setMaxLength(15);
        this.sizeXEdit.setValue(Integer.toString(var1.getX()));
        this.addWidget(this.sizeXEdit);
        this.sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.size.y"));
        this.sizeYEdit.setMaxLength(15);
        this.sizeYEdit.setValue(Integer.toString(var1.getY()));
        this.addWidget(this.sizeYEdit);
        this.sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 120, 80, 20, Component.translatable("structure_block.size.z"));
        this.sizeZEdit.setMaxLength(15);
        this.sizeZEdit.setValue(Integer.toString(var1.getZ()));
        this.addWidget(this.sizeZEdit);
        this.integrityEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.integrity.integrity"));
        this.integrityEdit.setMaxLength(15);
        this.integrityEdit.setValue(this.decimalFormat.format((double)this.structure.getIntegrity()));
        this.addWidget(this.integrityEdit);
        this.seedEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.integrity.seed"));
        this.seedEdit.setMaxLength(31);
        this.seedEdit.setValue(Long.toString(this.structure.getSeed()));
        this.addWidget(this.seedEdit);
        this.dataEdit = new EditBox(this.font, this.width / 2 - 152, 120, 240, 20, Component.translatable("structure_block.custom_data"));
        this.dataEdit.setMaxLength(128);
        this.dataEdit.setValue(this.structure.getMetaData());
        this.addWidget(this.dataEdit);
        this.updateDirectionButtons();
        this.updateMode(this.initialMode);
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.nameEdit.getValue();
        String var1 = this.posXEdit.getValue();
        String var2 = this.posYEdit.getValue();
        String var3 = this.posZEdit.getValue();
        String var4 = this.sizeXEdit.getValue();
        String var5 = this.sizeYEdit.getValue();
        String var6 = this.sizeZEdit.getValue();
        String var7 = this.integrityEdit.getValue();
        String var8 = this.seedEdit.getValue();
        String var9 = this.dataEdit.getValue();
        this.init(param0, param1, param2);
        this.nameEdit.setValue(var0);
        this.posXEdit.setValue(var1);
        this.posYEdit.setValue(var2);
        this.posZEdit.setValue(var3);
        this.sizeXEdit.setValue(var4);
        this.sizeYEdit.setValue(var5);
        this.sizeZEdit.setValue(var6);
        this.integrityEdit.setValue(var7);
        this.seedEdit.setValue(var8);
        this.dataEdit.setValue(var9);
    }

    private void updateDirectionButtons() {
        this.rot0Button.active = true;
        this.rot90Button.active = true;
        this.rot180Button.active = true;
        this.rot270Button.active = true;
        switch(this.structure.getRotation()) {
            case NONE:
                this.rot0Button.active = false;
                break;
            case CLOCKWISE_180:
                this.rot180Button.active = false;
                break;
            case COUNTERCLOCKWISE_90:
                this.rot270Button.active = false;
                break;
            case CLOCKWISE_90:
                this.rot90Button.active = false;
        }

    }

    private void updateMode(StructureMode param0) {
        this.nameEdit.setVisible(false);
        this.posXEdit.setVisible(false);
        this.posYEdit.setVisible(false);
        this.posZEdit.setVisible(false);
        this.sizeXEdit.setVisible(false);
        this.sizeYEdit.setVisible(false);
        this.sizeZEdit.setVisible(false);
        this.integrityEdit.setVisible(false);
        this.seedEdit.setVisible(false);
        this.dataEdit.setVisible(false);
        this.saveButton.visible = false;
        this.loadButton.visible = false;
        this.detectButton.visible = false;
        this.includeEntitiesButton.visible = false;
        this.mirrorButton.visible = false;
        this.rot0Button.visible = false;
        this.rot90Button.visible = false;
        this.rot180Button.visible = false;
        this.rot270Button.visible = false;
        this.toggleAirButton.visible = false;
        this.toggleBoundingBox.visible = false;
        switch(param0) {
            case SAVE:
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.sizeXEdit.setVisible(true);
                this.sizeYEdit.setVisible(true);
                this.sizeZEdit.setVisible(true);
                this.saveButton.visible = true;
                this.detectButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.toggleAirButton.visible = true;
                break;
            case LOAD:
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.integrityEdit.setVisible(true);
                this.seedEdit.setVisible(true);
                this.loadButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.mirrorButton.visible = true;
                this.rot0Button.visible = true;
                this.rot90Button.visible = true;
                this.rot180Button.visible = true;
                this.rot270Button.visible = true;
                this.toggleBoundingBox.visible = true;
                this.updateDirectionButtons();
                break;
            case CORNER:
                this.nameEdit.setVisible(true);
                break;
            case DATA:
                this.dataEdit.setVisible(true);
        }

    }

    private boolean sendToServer(StructureBlockEntity.UpdateType param0) {
        BlockPos var0 = new BlockPos(
            this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue())
        );
        Vec3i var1 = new Vec3i(
            this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue())
        );
        float var2 = this.parseIntegrity(this.integrityEdit.getValue());
        long var3 = this.parseSeed(this.seedEdit.getValue());
        this.minecraft
            .getConnection()
            .send(
                new ServerboundSetStructureBlockPacket(
                    this.structure.getBlockPos(),
                    param0,
                    this.structure.getMode(),
                    this.nameEdit.getValue(),
                    var0,
                    var1,
                    this.structure.getMirror(),
                    this.structure.getRotation(),
                    this.dataEdit.getValue(),
                    this.structure.isIgnoreEntities(),
                    this.structure.getShowAir(),
                    this.structure.getShowBoundingBox(),
                    var2,
                    var3
                )
            );
        return true;
    }

    private long parseSeed(String param0) {
        try {
            return Long.valueOf(param0);
        } catch (NumberFormatException var3) {
            return 0L;
        }
    }

    private float parseIntegrity(String param0) {
        try {
            return Float.valueOf(param0);
        } catch (NumberFormatException var3) {
            return 1.0F;
        }
    }

    private int parseCoordinate(String param0) {
        try {
            return Integer.parseInt(param0);
        } catch (NumberFormatException var3) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 != 257 && param0 != 335) {
            return false;
        } else {
            this.onDone();
            return true;
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        StructureMode var0 = this.structure.getMode();
        drawCenteredString(param0, this.font, this.title, this.width / 2, 10, 16777215);
        if (var0 != StructureMode.DATA) {
            drawString(param0, this.font, NAME_LABEL, this.width / 2 - 153, 30, 10526880);
            this.nameEdit.render(param0, param1, param2, param3);
        }

        if (var0 == StructureMode.LOAD || var0 == StructureMode.SAVE) {
            drawString(param0, this.font, POSITION_LABEL, this.width / 2 - 153, 70, 10526880);
            this.posXEdit.render(param0, param1, param2, param3);
            this.posYEdit.render(param0, param1, param2, param3);
            this.posZEdit.render(param0, param1, param2, param3);
            drawString(param0, this.font, INCLUDE_ENTITIES_LABEL, this.width / 2 + 154 - this.font.width(INCLUDE_ENTITIES_LABEL), 150, 10526880);
        }

        if (var0 == StructureMode.SAVE) {
            drawString(param0, this.font, SIZE_LABEL, this.width / 2 - 153, 110, 10526880);
            this.sizeXEdit.render(param0, param1, param2, param3);
            this.sizeYEdit.render(param0, param1, param2, param3);
            this.sizeZEdit.render(param0, param1, param2, param3);
            drawString(param0, this.font, DETECT_SIZE_LABEL, this.width / 2 + 154 - this.font.width(DETECT_SIZE_LABEL), 110, 10526880);
            drawString(param0, this.font, SHOW_AIR_LABEL, this.width / 2 + 154 - this.font.width(SHOW_AIR_LABEL), 70, 10526880);
        }

        if (var0 == StructureMode.LOAD) {
            drawString(param0, this.font, INTEGRITY_LABEL, this.width / 2 - 153, 110, 10526880);
            this.integrityEdit.render(param0, param1, param2, param3);
            this.seedEdit.render(param0, param1, param2, param3);
            drawString(param0, this.font, SHOW_BOUNDING_BOX_LABEL, this.width / 2 + 154 - this.font.width(SHOW_BOUNDING_BOX_LABEL), 70, 10526880);
        }

        if (var0 == StructureMode.DATA) {
            drawString(param0, this.font, CUSTOM_DATA_LABEL, this.width / 2 - 153, 110, 10526880);
            this.dataEdit.render(param0, param1, param2, param3);
        }

        drawString(param0, this.font, var0.getDisplayName(), this.width / 2 - 153, 174, 10526880);
        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
