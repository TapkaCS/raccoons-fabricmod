package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.advancement.ModTriggers;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Sniffs out a nearby chest with food in it, "steals" one item, carries it to the nearest
 * water within range to "wash" it, then eats it. Purely a flavor goal - stolen food is just
 * removed from the chest and deleted after the wash/eat timers run out. Applies to tamed and
 * untamed raccoons alike.
 *
 * <p>At night, wild raccoons that have at least one other wild raccoon nearby (a "gang") roll
 * this check far more often, so groups that spawned together tend to converge on the same
 * chests around the same time - reads as a coordinated nighttime raid without needing any
 * actual inter-raccoon coordination logic.
 */
public class RaccoonStealFoodGoal extends Goal {

    private enum Phase { GOTO_CHEST, STEAL, GOTO_WATER, WASH, EAT }

    private static final int CHEST_SEARCH_RADIUS = 12;
    private static final int WATER_SEARCH_RADIUS = 7;
    private static final int NORMAL_CHANCE = 400;
    private static final int NIGHT_GANG_CHANCE = 30;
    private static final double GANG_CHECK_RADIUS = 16.0;
    private static final int GANG_MIN_OTHERS = 1;

    private final RaccoonEntity raccoon;
    private BlockPos chestPos;
    private BlockPos waterPos;
    private Phase phase;
    private int timer;
    private ItemStack stolenStack = ItemStack.EMPTY;

    public RaccoonStealFoodGoal(RaccoonEntity raccoon) {
        this.raccoon = raccoon;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.level().isClientSide()) {
            return false;
        }
        int chance = this.isNightGangRaid() ? NIGHT_GANG_CHANCE : NORMAL_CHANCE;
        if (this.raccoon.getRandom().nextInt(chance) != 0) {
            return false;
        }
        return this.findChestWithFood() != null;
    }

    /** Wild, and at night, and not alone - i.e. part of a raiding gang rather than a lone raccoon passing through. */
    private boolean isNightGangRaid() {
        if (this.raccoon.isTame() || !this.raccoon.level().isDarkOutside()) {
            return false;
        }
        AABB area = this.raccoon.getBoundingBox().inflate(GANG_CHECK_RADIUS);
        long nearbyWildRaccoons = this.raccoon.level().getEntitiesOfClass(RaccoonEntity.class, area,
                other -> other != this.raccoon && !other.isTame()).size();
        return nearbyWildRaccoons >= GANG_MIN_OTHERS;
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != null;
    }

    @Override
    public void start() {
        this.chestPos = this.findChestWithFood();
        this.phase = Phase.GOTO_CHEST;
        this.timer = 0;
    }

    @Override
    public void stop() {
        if (this.chestPos != null && this.raccoon.level().getBlockEntity(this.chestPos) instanceof ChestBlockEntity chest) {
            chest.stopOpen(this.raccoon);
        }
        this.raccoon.setOpenedChestPos(null);
        this.raccoon.setCarriedItem(ItemStack.EMPTY);
        this.raccoon.setWashing(false);
        this.chestPos = null;
        this.waterPos = null;
        this.phase = null;
        this.stolenStack = ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        switch (this.phase) {
            case GOTO_CHEST -> this.tickGotoChest();
            case STEAL -> this.tickSteal();
            case GOTO_WATER -> this.tickGotoWater();
            case WASH -> this.tickWash();
            case EAT -> this.tickEat();
        }
    }

    private void tickGotoChest() {
        this.raccoon.getNavigation().moveTo(this.chestPos.getX() + 0.5, this.chestPos.getY(), this.chestPos.getZ() + 0.5, 1.0);
        this.raccoon.getLookControl().setLookAt(this.chestPos.getX() + 0.5, this.chestPos.getY() + 0.5, this.chestPos.getZ() + 0.5);

        if (this.raccoon.blockPosition().closerThan(this.chestPos, 2.0)) {
            this.raccoon.getNavigation().stop();
            this.raccoon.setOpenedChestPos(this.chestPos);
            if (this.raccoon.level().getBlockEntity(this.chestPos) instanceof ChestBlockEntity chest) {
                chest.startOpen(this.raccoon);
            }
            this.phase = Phase.STEAL;
            this.timer = 30;
        }
    }

    private void tickSteal() {
        if (this.timer-- > 0) {
            return;
        }

        if (this.raccoon.level().getBlockEntity(this.chestPos) instanceof ChestBlockEntity chest) {
            for (int i = 0; i < chest.getContainerSize(); i++) {
                if (chest.getItem(i).has(DataComponents.FOOD)) {
                    this.stolenStack = chest.removeItem(i, 1);
                    break;
                }
            }
            chest.stopOpen(this.raccoon);
        }
        this.raccoon.setOpenedChestPos(null);

        if (this.stolenStack.isEmpty()) {
            this.phase = null;
            return;
        }
        this.raccoon.setCarriedItem(this.stolenStack.copy());

        if (this.stolenStack.is(Items.ENCHANTED_GOLDEN_APPLE) && this.raccoon.getOwner() instanceof ServerPlayer owner) {
            ModTriggers.STOLE_GOLDEN_APPLE.trigger(owner);
        }

        this.waterPos = this.findNearbyWater();
        this.phase = this.waterPos != null ? Phase.GOTO_WATER : Phase.EAT;
        this.timer = 20;
    }

    private void tickGotoWater() {
        this.raccoon.getNavigation().moveTo(this.waterPos.getX() + 0.5, this.waterPos.getY() + 1, this.waterPos.getZ() + 0.5, 1.0);

        if (this.raccoon.blockPosition().closerThan(this.waterPos, 1.5)) {
            this.raccoon.getNavigation().stop();
            this.raccoon.setWashing(true);
            this.phase = Phase.WASH;
            this.timer = 60;
        }
    }

    private void tickWash() {
        this.raccoon.getLookControl().setLookAt(this.waterPos.getX() + 0.5, this.waterPos.getY(), this.waterPos.getZ() + 0.5);
        if (this.timer-- <= 0) {
            this.raccoon.setWashing(false);
            this.phase = Phase.EAT;
            this.timer = 20;
        }
    }

    private void tickEat() {
        if (this.timer-- <= 0) {
            this.spawnEatingEffects();
            this.raccoon.heal(1.0F);
            this.raccoon.setCarriedItem(ItemStack.EMPTY);
            this.stolenStack = ItemStack.EMPTY;
            this.phase = null;
        }
    }

    private void spawnEatingEffects() {
        this.raccoon.level().playSound(null, this.raccoon.getX(), this.raccoon.getY(), this.raccoon.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 0.8F + this.raccoon.getRandom().nextFloat() * 0.4F);

        if (this.raccoon.level() instanceof ServerLevel serverLevel && !this.stolenStack.isEmpty()) {
            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, this.stolenStack.getItem());
            serverLevel.sendParticles(particle,
                    this.raccoon.getX(), this.raccoon.getEyeY() - 0.3, this.raccoon.getZ(),
                    6, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private BlockPos findChestWithFood() {
        BlockPos origin = this.raccoon.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-CHEST_SEARCH_RADIUS, -2, -CHEST_SEARCH_RADIUS),
                origin.offset(CHEST_SEARCH_RADIUS, 2, CHEST_SEARCH_RADIUS))) {
            if (!this.raccoon.level().getBlockState(pos).is(Blocks.CHEST)
                    && !this.raccoon.level().getBlockState(pos).is(Blocks.TRAPPED_CHEST)) {
                continue;
            }
            if (this.raccoon.level().getBlockEntity(pos) instanceof ChestBlockEntity chest && hasFood(chest)) {
                double dist = pos.distSqr(origin);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    private static boolean hasFood(ChestBlockEntity chest) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).has(DataComponents.FOOD)) {
                return true;
            }
        }
        return false;
    }

    private BlockPos findNearbyWater() {
        BlockPos origin = this.chestPos;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-WATER_SEARCH_RADIUS, -2, -WATER_SEARCH_RADIUS),
                origin.offset(WATER_SEARCH_RADIUS, 2, WATER_SEARCH_RADIUS))) {
            if (this.raccoon.level().getFluidState(pos).is(FluidTags.WATER)) {
                double dist = pos.distSqr(origin);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }
}
