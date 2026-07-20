package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.Raccoons;
import com.tapkacs.raccoons.block.ModBlocks;
import com.tapkacs.raccoons.block.RaccoonStashBlockEntity;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Wild raccoons occasionally pick up items lying on the ground (preferring anything above
 * "common" rarity) and carry them off to a nearby stash - a hollowed-out tree trunk - to hoard.
 * If no stash exists nearby yet, one gets carved into a nearby log on the spot.
 */
public class RaccoonStashGoal extends Goal {

    private enum Phase { GOTO_ITEM, PICKUP, GOTO_STASH, DEPOSIT }

    private static final int ITEM_SEARCH_RADIUS = 10;
    private static final int STASH_SEARCH_RADIUS = 16;
    private static final int STASH_SEARCH_VERTICAL = 4;
    private static final int LOG_SEARCH_RADIUS = 8;
    private static final int MAX_GOTO_TICKS = 200;
    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "blocks/raccoon_stash"));

    private final RaccoonEntity raccoon;
    private ItemEntity targetItem;
    private BlockPos stashPos;
    private Phase phase;
    private int timer;
    private int gotoTicks;

    public RaccoonStashGoal(RaccoonEntity raccoon) {
        this.raccoon = raccoon;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.level().isClientSide() || this.raccoon.isTame()) {
            return false;
        }
        if (!this.raccoon.getCarriedItem().isEmpty()) {
            return false;
        }
        if (this.raccoon.getRandom().nextInt(600) != 0) {
            return false;
        }
        this.targetItem = this.findGroundItem();
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != null;
    }

    @Override
    public void start() {
        this.phase = Phase.GOTO_ITEM;
        this.timer = 0;
        this.gotoTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.stashPos = null;
        this.phase = null;
        // Don't just vanish a stolen item if we're abandoning mid-carry - drop it where we stand.
        if (!this.raccoon.getCarriedItem().isEmpty() && this.raccoon.level() instanceof ServerLevel serverLevel) {
            this.raccoon.spawnAtLocation(serverLevel, this.raccoon.getCarriedItem());
        }
        this.raccoon.setCarriedItem(ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        switch (this.phase) {
            case GOTO_ITEM -> this.tickGotoItem();
            case PICKUP -> this.tickPickup();
            case GOTO_STASH -> this.tickGotoStash();
            case DEPOSIT -> this.tickDeposit();
        }
    }

    private void tickGotoItem() {
        if (this.targetItem == null || !this.targetItem.isAlive() || ++this.gotoTicks > MAX_GOTO_TICKS) {
            this.phase = null;
            return;
        }
        this.raccoon.getNavigation().moveTo(this.targetItem, 1.0);
        this.raccoon.getLookControl().setLookAt(this.targetItem);

        if (this.raccoon.distanceToSqr(this.targetItem) < 2.25) {
            this.raccoon.getNavigation().stop();
            this.phase = Phase.PICKUP;
            this.timer = 15;
        }
    }

    private void tickPickup() {
        if (this.timer-- > 0) {
            return;
        }
        if (this.targetItem == null || !this.targetItem.isAlive()) {
            this.phase = null;
            return;
        }

        ItemStack stack = this.targetItem.getItem().split(1);
        if (this.targetItem.getItem().isEmpty()) {
            this.targetItem.discard();
        }
        this.targetItem = null;
        this.raccoon.setCarriedItem(stack);

        this.stashPos = this.findOrCreateStash();
        if (this.stashPos == null) {
            this.phase = null;
            return;
        }
        this.gotoTicks = 0;
        this.phase = Phase.GOTO_STASH;
    }

    private void tickGotoStash() {
        if (++this.gotoTicks > MAX_GOTO_TICKS) {
            // Can't reach it (blocked by water, terrain, etc.) - drop the loot here rather than carry it forever.
            this.phase = null;
            return;
        }

        this.raccoon.getNavigation().moveTo(this.stashPos.getX() + 0.5, this.stashPos.getY(), this.stashPos.getZ() + 0.5, 1.0);
        this.raccoon.getLookControl().setLookAt(this.stashPos.getX() + 0.5, this.stashPos.getY() + 0.5, this.stashPos.getZ() + 0.5);

        if (this.raccoon.blockPosition().closerThan(this.stashPos, 2.5)) {
            this.raccoon.getNavigation().stop();
            this.phase = Phase.DEPOSIT;
            this.timer = 20;
        }
    }

    private void tickDeposit() {
        if (this.timer-- > 0) {
            return;
        }

        ItemStack carried = this.raccoon.getCarriedItem();
        if (!carried.isEmpty() && this.raccoon.level().getBlockEntity(this.stashPos) instanceof RaccoonStashBlockEntity stash) {
            for (int i = 0; i < stash.getContainerSize(); i++) {
                if (stash.getItem(i).isEmpty()) {
                    stash.setItem(i, carried.copy());
                    break;
                }
            }
        }

        this.raccoon.setCarriedItem(ItemStack.EMPTY);
        this.phase = null;
    }

    private ItemEntity findGroundItem() {
        AABB area = this.raccoon.getBoundingBox().inflate(ITEM_SEARCH_RADIUS);
        List<ItemEntity> items = this.raccoon.level().getEntitiesOfClass(ItemEntity.class, area,
                item -> item.isAlive() && !item.hasPickUpDelay() && !item.getItem().isEmpty());

        ItemEntity best = null;
        ItemEntity fallback = null;
        double bestDist = Double.MAX_VALUE;
        double fallbackDist = Double.MAX_VALUE;

        for (ItemEntity item : items) {
            double dist = item.distanceToSqr(this.raccoon);
            if (item.getItem().getRarity() != Rarity.COMMON) {
                if (dist < bestDist) {
                    bestDist = dist;
                    best = item;
                }
            } else if (dist < fallbackDist) {
                fallbackDist = dist;
                fallback = item;
            }
        }
        return best != null ? best : fallback;
    }

    private BlockPos findOrCreateStash() {
        BlockPos existing = this.findNearbyStash();
        if (existing != null) {
            return existing;
        }
        return this.createStash();
    }

    private BlockPos findNearbyStash() {
        BlockPos origin = this.raccoon.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-STASH_SEARCH_RADIUS, -STASH_SEARCH_VERTICAL, -STASH_SEARCH_RADIUS),
                origin.offset(STASH_SEARCH_RADIUS, STASH_SEARCH_VERTICAL, STASH_SEARCH_RADIUS))) {
            if (this.raccoon.level().getBlockState(pos).is(ModBlocks.RACCOON_STASH)) {
                double dist = pos.distSqr(origin);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    private BlockPos createStash() {
        BlockPos origin = this.raccoon.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-LOG_SEARCH_RADIUS, -4, -LOG_SEARCH_RADIUS),
                origin.offset(LOG_SEARCH_RADIUS, 4, LOG_SEARCH_RADIUS))) {
            if (this.raccoon.level().getBlockState(pos).is(BlockTags.LOGS)) {
                double dist = pos.distSqr(origin);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                }
            }
        }

        if (best == null) {
            return null;
        }

        this.raccoon.level().setBlockAndUpdate(best, ModBlocks.RACCOON_STASH.defaultBlockState());
        if (this.raccoon.level().getBlockEntity(best) instanceof RaccoonStashBlockEntity stash) {
            stash.setLootTable(LOOT_TABLE);
            stash.setLootTableSeed(this.raccoon.getRandom().nextLong());
        }
        return best;
    }
}
