package com.tapkacs.raccoons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Small (1-row) loot-table-backed container, matching how vanilla chests/barrels auto-fill on first open. */
public class RaccoonStashBlockEntity extends RandomizableContainerBlockEntity {
    private static final int CONTAINER_SIZE = 9;
    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public RaccoonStashBlockEntity(BlockPos pos, BlockState state) {
        super(com.tapkacs.raccoons.block.ModBlocks.RACCOON_STASH_BLOCK_ENTITY, pos, state);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.raccoons.raccoon_stash");
    }

    // BaseContainerBlockEntity's default load/save only handles the lock code and custom name - neither
    // items nor the loot table get persisted unless a subclass wires that in itself (this is what
    // vanilla's ChestBlockEntity/BarrelBlockEntity do). Without this override, a raccoon-created stash's
    // contents (and its un-popped loot table) vanish the moment the chunk saves and reloads.
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, this.items);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.items);
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new ChestMenu(MenuType.GENERIC_9x1, containerId, playerInventory, this, 1);
    }
}
