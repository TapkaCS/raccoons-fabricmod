package com.tapkacs.raccoons.client.gui;

import com.tapkacs.raccoons.config.ModConfigManager;
import com.tapkacs.raccoons.config.RaccoonsConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * A hand-rolled, semi-transparent options-style screen for {@link RaccoonsConfig} - opened via the
 * {@code /raccoonconfig} client command instead of hand-editing the JSON. Edits live-update a local
 * working copy (so switching tabs never loses unsaved changes); "Apply"/"Done" write that copy into
 * the shared config instance and persist it to disk. Takes effect immediately for anything a goal
 * reads fresh each tick (theft, stashing) - spawn-registration values still need a game restart,
 * since biome spawn lists are only built once at mod init.
 */
public class RaccoonConfigScreen extends Screen {
    private enum Category { THEFT, STASHING, SPAWN_RATES, COLORING, BEHAVIOR }

    private static final int ROW_HEIGHT = 22;
    private static final int SIDEBAR_X = 20;
    private static final int SIDEBAR_WIDTH = 110;
    private static final int CONTENT_X = 145;
    private static final int LABEL_WIDTH = 230;
    private static final int FIELD_WIDTH = 70;
    private static final int PANEL_TOP = 34;
    private static final int PANEL_BOTTOM_MARGIN = 34;

    @Nullable
    private final Screen parent;
    private Category category = Category.THEFT;
    private final List<LabelEntry> labels = new ArrayList<>();

    // Working copy - one field per RaccoonsConfig leaf, so edits on any tab survive switching tabs.
    private boolean theftEnabled;
    private int chestStealChance;
    private int nightGangStealChance;
    private boolean stashingEnabled;
    private int stashChance;
    private int forestTaigaWeight;
    private int forestTaigaMinGroupSize;
    private int forestTaigaMaxGroupSize;
    private int villageBiomeWeight;
    private int villageBiomeMinGroupSize;
    private int villageBiomeMaxGroupSize;
    private int mountainBadlandsWeight;
    private int mountainBadlandsMinGroupSize;
    private int mountainBadlandsMaxGroupSize;
    private float chunkySpawnChance;
    private float albinoSpawnChance;
    private float melanisticSpawnChance;
    private int overfeedThreshold;
    private int tamedRaccoonsAchievementThreshold;

    private record LabelEntry(String text, int x, int y) {
    }

    public RaccoonConfigScreen(@Nullable Screen parent) {
        super(Component.literal("Raccoons Config"));
        this.parent = parent;

        RaccoonsConfig config = ModConfigManager.get();
        this.theftEnabled = config.theft.enabled;
        this.chestStealChance = config.theft.chestStealChance;
        this.nightGangStealChance = config.theft.nightGangStealChance;
        this.stashingEnabled = config.stashing.enabled;
        this.stashChance = config.stashing.stashChance;
        this.forestTaigaWeight = config.spawning.forestTaigaWeight;
        this.forestTaigaMinGroupSize = config.spawning.forestTaigaMinGroupSize;
        this.forestTaigaMaxGroupSize = config.spawning.forestTaigaMaxGroupSize;
        this.villageBiomeWeight = config.spawning.villageBiomeWeight;
        this.villageBiomeMinGroupSize = config.spawning.villageBiomeMinGroupSize;
        this.villageBiomeMaxGroupSize = config.spawning.villageBiomeMaxGroupSize;
        this.mountainBadlandsWeight = config.spawning.mountainBadlandsWeight;
        this.mountainBadlandsMinGroupSize = config.spawning.mountainBadlandsMinGroupSize;
        this.mountainBadlandsMaxGroupSize = config.spawning.mountainBadlandsMaxGroupSize;
        this.chunkySpawnChance = config.spawning.chunkySpawnChance;
        this.albinoSpawnChance = config.spawning.albinoSpawnChance;
        this.melanisticSpawnChance = config.spawning.melanisticSpawnChance;
        this.overfeedThreshold = config.behavior.overfeedThreshold;
        this.tamedRaccoonsAchievementThreshold = config.behavior.tamedRaccoonsAchievementThreshold;
    }

    @Override
    protected void init() {
        this.labels.clear();
        this.addSidebar();

        int y = PANEL_TOP + 8;
        switch (this.category) {
            case THEFT -> {
                y = this.addCheckboxRow(y, "Enabled", this.theftEnabled, v -> this.theftEnabled = v);
                y = this.addIntRow(y, "Chest/barrel/composter chance (1 in N)", this.chestStealChance, 1, 100000, v -> this.chestStealChance = v);
                this.addIntRow(y, "Night gang chance (1 in N)", this.nightGangStealChance, 1, 100000, v -> this.nightGangStealChance = v);
            }
            case STASHING -> {
                y = this.addCheckboxRow(y, "Enabled", this.stashingEnabled, v -> this.stashingEnabled = v);
                this.addIntRow(y, "Ground-item pickup chance (1 in N)", this.stashChance, 1, 100000, v -> this.stashChance = v);
            }
            case SPAWN_RATES -> {
                y = this.addIntRow(y, "Forest/taiga weight", this.forestTaigaWeight, 0, 1000, v -> this.forestTaigaWeight = v);
                y = this.addIntRow(y, "Forest/taiga min group", this.forestTaigaMinGroupSize, 1, 20, v -> this.forestTaigaMinGroupSize = v);
                y = this.addIntRow(y, "Forest/taiga max group", this.forestTaigaMaxGroupSize, 1, 20, v -> this.forestTaigaMaxGroupSize = v);
                y = this.addIntRow(y, "Village biome weight", this.villageBiomeWeight, 0, 1000, v -> this.villageBiomeWeight = v);
                y = this.addIntRow(y, "Village biome min group", this.villageBiomeMinGroupSize, 1, 20, v -> this.villageBiomeMinGroupSize = v);
                y = this.addIntRow(y, "Village biome max group", this.villageBiomeMaxGroupSize, 1, 20, v -> this.villageBiomeMaxGroupSize = v);
                y = this.addIntRow(y, "Mountain/badlands weight", this.mountainBadlandsWeight, 0, 1000, v -> this.mountainBadlandsWeight = v);
                y = this.addIntRow(y, "Mountain/badlands min group", this.mountainBadlandsMinGroupSize, 1, 20, v -> this.mountainBadlandsMinGroupSize = v);
                this.addIntRow(y, "Mountain/badlands max group", this.mountainBadlandsMaxGroupSize, 1, 20, v -> this.mountainBadlandsMaxGroupSize = v);
            }
            case COLORING -> {
                y = this.addFloatRow(y, "Chunky spawn chance (0-1)", this.chunkySpawnChance, 0f, 1f, v -> this.chunkySpawnChance = v);
                y = this.addFloatRow(y, "Albino spawn chance (0-1)", this.albinoSpawnChance, 0f, 1f, v -> this.albinoSpawnChance = v);
                this.addFloatRow(y, "Melanistic spawn chance (0-1)", this.melanisticSpawnChance, 0f, 1f, v -> this.melanisticSpawnChance = v);
            }
            case BEHAVIOR -> {
                y = this.addIntRow(y, "Overfeed threshold (-> chunky)", this.overfeedThreshold, 1, 10000, v -> this.overfeedThreshold = v);
                this.addIntRow(y, "Tames needed for advancement", this.tamedRaccoonsAchievementThreshold, 1, 10000, v -> this.tamedRaccoonsAchievementThreshold = v);
            }
        }

        int buttonY = this.height - PANEL_BOTTOM_MARGIN + 8;
        this.addRenderableWidget(Button.builder(Component.literal("Apply"), b -> this.apply())
                .pos(this.width - 210, buttonY).size(90, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            this.apply();
            this.onClose();
        }).pos(this.width - 110, buttonY).size(90, 20).build());
    }

    private void addSidebar() {
        int y = PANEL_TOP + 8;
        for (Category cat : Category.values()) {
            Button button = Button.builder(Component.literal(this.sidebarLabel(cat)), b -> {
                        this.category = cat;
                        this.clearWidgets();
                        this.init();
                    })
                    .pos(SIDEBAR_X, y).size(SIDEBAR_WIDTH, 20).build();
            button.active = cat != this.category;
            this.addRenderableWidget(button);
            y += ROW_HEIGHT;
        }
    }

    private String sidebarLabel(Category category) {
        return switch (category) {
            case THEFT -> "Theft";
            case STASHING -> "Stashing";
            case SPAWN_RATES -> "Spawn Rates";
            case COLORING -> "Coloring";
            case BEHAVIOR -> "Behavior";
        };
    }

    private int addCheckboxRow(int y, String label, boolean initial, Consumer<Boolean> onChange) {
        this.addRenderableWidget(Checkbox.builder(Component.literal(label), this.font)
                .pos(CONTENT_X, y)
                .selected(initial)
                .onValueChange((checkbox, selected) -> onChange.accept(selected))
                .build());
        return y + ROW_HEIGHT;
    }

    private int addIntRow(int y, String label, int initial, int min, int max, IntConsumer onChange) {
        this.labels.add(new LabelEntry(label, CONTENT_X, y + 6));
        EditBox editBox = new EditBox(this.font, CONTENT_X + LABEL_WIDTH, y, FIELD_WIDTH, 18, Component.empty());
        editBox.setValue(Integer.toString(initial));
        editBox.setResponder(text -> {
            try {
                onChange.accept(Math.clamp(Integer.parseInt(text.trim()), min, max));
            } catch (NumberFormatException ignored) {
                // Leave the working value alone until the text becomes a valid number again.
            }
        });
        this.addRenderableWidget(editBox);
        return y + ROW_HEIGHT;
    }

    private int addFloatRow(int y, String label, float initial, float min, float max, Consumer<Float> onChange) {
        this.labels.add(new LabelEntry(label, CONTENT_X, y + 6));
        EditBox editBox = new EditBox(this.font, CONTENT_X + LABEL_WIDTH, y, FIELD_WIDTH, 18, Component.empty());
        editBox.setValue(Float.toString(initial));
        editBox.setResponder(text -> {
            try {
                onChange.accept(Math.clamp(Float.parseFloat(text.trim()), min, max));
            } catch (NumberFormatException ignored) {
                // Leave the working value alone until the text becomes a valid number again.
            }
        });
        this.addRenderableWidget(editBox);
        return y + ROW_HEIGHT;
    }

    private void apply() {
        RaccoonsConfig config = ModConfigManager.get();
        config.theft.enabled = this.theftEnabled;
        config.theft.chestStealChance = this.chestStealChance;
        config.theft.nightGangStealChance = this.nightGangStealChance;
        config.stashing.enabled = this.stashingEnabled;
        config.stashing.stashChance = this.stashChance;
        config.spawning.forestTaigaWeight = this.forestTaigaWeight;
        config.spawning.forestTaigaMinGroupSize = this.forestTaigaMinGroupSize;
        config.spawning.forestTaigaMaxGroupSize = this.forestTaigaMaxGroupSize;
        config.spawning.villageBiomeWeight = this.villageBiomeWeight;
        config.spawning.villageBiomeMinGroupSize = this.villageBiomeMinGroupSize;
        config.spawning.villageBiomeMaxGroupSize = this.villageBiomeMaxGroupSize;
        config.spawning.mountainBadlandsWeight = this.mountainBadlandsWeight;
        config.spawning.mountainBadlandsMinGroupSize = this.mountainBadlandsMinGroupSize;
        config.spawning.mountainBadlandsMaxGroupSize = this.mountainBadlandsMaxGroupSize;
        config.spawning.chunkySpawnChance = this.chunkySpawnChance;
        config.spawning.albinoSpawnChance = this.albinoSpawnChance;
        config.spawning.melanisticSpawnChance = this.melanisticSpawnChance;
        config.behavior.overfeedThreshold = this.overfeedThreshold;
        config.behavior.tamedRaccoonsAchievementThreshold = this.tamedRaccoonsAchievementThreshold;
        ModConfigManager.save();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);
        guiGraphics.fill(SIDEBAR_X - 6, PANEL_TOP, SIDEBAR_X + SIDEBAR_WIDTH + 6, this.height - PANEL_BOTTOM_MARGIN, 0xB0141414);
        guiGraphics.fill(CONTENT_X - 10, PANEL_TOP, this.width - 14, this.height - PANEL_BOTTOM_MARGIN, 0xA01E1E1E);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        Font font = this.font;
        for (LabelEntry entry : this.labels) {
            guiGraphics.text(font, entry.text(), entry.x(), entry.y(), 0xE0E0E0);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
