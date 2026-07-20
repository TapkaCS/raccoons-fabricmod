package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Normal pathfinding (Wander/FollowOwner) routes around logs/leaves/fences since they're not
 * flagged walkable, so a raccoon would never organically bump into one for
 * {@link RaccoonEntity#tick()}'s climb-assist to kick in. This goal periodically picks a nearby
 * climbable-looking obstacle and actively shoves the raccoon into it, letting the physics-level
 * climb-assist take over from there.
 */
public class RaccoonClimbGoal extends Goal {

    private static final int MAX_CLIMB_TICKS = 100; // 5s safety cap
    private static final double PUSH_STRENGTH = 0.2;

    private final RaccoonEntity raccoon;
    private Direction climbDirection;
    private int climbTicks;

    public RaccoonClimbGoal(RaccoonEntity raccoon) {
        this.raccoon = raccoon;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.level().isClientSide() || this.raccoon.getRandom().nextInt(200) != 0) {
            return false;
        }
        this.climbDirection = this.findClimbableDirection();
        return this.climbDirection != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.climbDirection != null && this.climbTicks < MAX_CLIMB_TICKS && this.isClimbableAhead();
    }

    @Override
    public void start() {
        this.climbTicks = 0;
        this.raccoon.setClimbIntent(true);
    }

    @Override
    public void stop() {
        this.climbDirection = null;
        this.raccoon.setClimbIntent(false);
    }

    @Override
    public void tick() {
        this.climbTicks++;

        BlockPos ahead = this.raccoon.blockPosition().relative(this.climbDirection);
        this.raccoon.getLookControl().setLookAt(ahead.getX() + 0.5, ahead.getY() + 0.5, ahead.getZ() + 0.5);

        double pushX = this.climbDirection.getStepX() * PUSH_STRENGTH;
        double pushZ = this.climbDirection.getStepZ() * PUSH_STRENGTH;
        this.raccoon.setDeltaMovement(this.raccoon.getDeltaMovement().add(pushX, 0, pushZ));
    }

    private boolean isClimbableAhead() {
        BlockState state = this.raccoon.level().getBlockState(this.raccoon.blockPosition().relative(this.climbDirection));
        return isClimbableBlock(state);
    }

    private Direction findClimbableDirection() {
        BlockPos pos = this.raccoon.blockPosition();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState state = this.raccoon.level().getBlockState(pos.relative(direction));
            if (isClimbableBlock(state) && this.hasAirAbove(pos.relative(direction))) {
                return direction;
            }
        }
        return null;
    }

    /** Avoids trying to "climb" into a solid mass with no way out (e.g. the middle of a wall). */
    private boolean hasAirAbove(BlockPos obstaclePos) {
        for (int i = 1; i <= 3; i++) {
            if (this.raccoon.level().getBlockState(obstaclePos.above(i)).isAir()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isClimbableBlock(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.FENCES);
    }
}
