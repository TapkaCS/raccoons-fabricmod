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
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Sniffs out a nearby chest/barrel with food in it (or a composter with anything in it),
 * "steals" one item, carries it to the nearest water within range to "wash" it, then eats it.
 * Purely a flavor goal - stolen food is just removed from the container and deleted after the
 * wash/eat timers run out. Applies to tamed and untamed raccoons alike.
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
    // What a raccoon comes away with after rummaging through a partly-filled composter.
    private static final List<Item> COMPOST_SCRAPS = List.of(
            Items.APPLE, Items.CARROT, Items.POTATO, Items.BREAD, Items.MELON_SLICE);

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
        // Don't steal a second thing (food, or anything RaccoonStashGoal has it carrying) while already holding something -
        // this goal outranks RaccoonStashGoal and would otherwise interrupt it mid-carry every time the gang-raid roll hits.
        if (!this.raccoon.getCarriedItem().isEmpty()) {
            return false;
        }
        int chance = this.isNightGangRaid() ? NIGHT_GANG_CHANCE : NORMAL_CHANCE;
        if (this.raccoon.getRandom().nextInt(chance) != 0) {
            return false;
        }
        return this.findRaidTarget() != null;
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
        this.chestPos = this.findRaidTarget();
        this.phase = Phase.GOTO_CHEST;
        this.timer = 0;
    }

    @Override
    public void stop() {
        if (this.chestPos != null) {
            this.stopOpen(this.raccoon.level().getBlockEntity(this.chestPos));
        }
        this.raccoon.setOpenedChestPos(null);
        this.raccoon.setCarriedItem(ItemStack.EMPTY);
        this.raccoon.setWashing(false);
        this.chestPos = null;
        this.waterPos = null;
        this.phase = null;
        this.stolenStack = ItemStack.EMPTY;
    }

    private void startOpen(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity chest) {
            chest.startOpen(this.raccoon);
        } else if (blockEntity instanceof BarrelBlockEntity barrel) {
            barrel.startOpen(this.raccoon);
        }
    }

    private void stopOpen(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity chest) {
            chest.stopOpen(this.raccoon);
        } else if (blockEntity instanceof BarrelBlockEntity barrel) {
            barrel.stopOpen(this.raccoon);
        }
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
            this.startOpen(this.raccoon.level().getBlockEntity(this.chestPos));
            this.phase = Phase.STEAL;
            this.timer = 30;
        }
    }

    private void tickSteal() {
        if (this.timer-- > 0) {
            return;
        }

        BlockState targetState = this.raccoon.level().getBlockState(this.chestPos);
        if (targetState.is(Blocks.COMPOSTER)) {
            this.stolenStack = this.rummageComposter(targetState);
        } else if (this.raccoon.level().getBlockEntity(this.chestPos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (container.getItem(i).has(DataComponents.FOOD)) {
                    this.stolenStack = container.removeItem(i, 1);
                    break;
                }
            }
            this.stopOpen(this.raccoon.level().getBlockEntity(this.chestPos));
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

    /** A random organic snack pulled out of a partly-filled composter, lowering its fill level by one. */
    private ItemStack rummageComposter(BlockState state) {
        int level = state.getValue(ComposterBlock.LEVEL);
        if (level <= 0 || level > ComposterBlock.MAX_LEVEL) {
            return ItemStack.EMPTY;
        }
        this.raccoon.level().setBlockAndUpdate(this.chestPos, state.setValue(ComposterBlock.LEVEL, level - 1));
        if (level == ComposterBlock.MAX_LEVEL) {
            return new ItemStack(Items.BONE_MEAL);
        }
        return new ItemStack(COMPOST_SCRAPS.get(this.raccoon.getRandom().nextInt(COMPOST_SCRAPS.size())));
    }

    private BlockPos findRaidTarget() {
        BlockPos origin = this.raccoon.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-CHEST_SEARCH_RADIUS, -2, -CHEST_SEARCH_RADIUS),
                origin.offset(CHEST_SEARCH_RADIUS, 2, CHEST_SEARCH_RADIUS))) {
            if (!this.isRaidableAt(pos)) {
                continue;
            }
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    private boolean isRaidableAt(BlockPos pos) {
        BlockState state = this.raccoon.level().getBlockState(pos);
        if (state.is(Blocks.COMPOSTER)) {
            return state.getValue(ComposterBlock.LEVEL) > 0;
        }
        if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)) {
            return this.raccoon.level().getBlockEntity(pos) instanceof Container container && hasFood(container);
        }
        return false;
    }

    private static boolean hasFood(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).has(DataComponents.FOOD)) {
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
