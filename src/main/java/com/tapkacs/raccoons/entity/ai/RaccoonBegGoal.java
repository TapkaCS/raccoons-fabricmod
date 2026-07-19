package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class RaccoonBegGoal extends Goal {

    private final RaccoonEntity raccoon;
    private final float lookDistance;
    private Player player;

    public RaccoonBegGoal(RaccoonEntity raccoon, float lookDistance) {
        this.raccoon = raccoon;
        this.lookDistance = lookDistance;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.raccoon.isTame()) {
            return false;
        }
        this.player = this.raccoon.level().getNearestPlayer(
                this.raccoon.getX(), this.raccoon.getEyeY(), this.raccoon.getZ(), this.lookDistance,
                entity -> entity instanceof Player player && this.isHoldingFood(player));
        return this.player != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.player != null && this.player.isAlive()
                && this.raccoon.distanceToSqr(this.player) <= (double) (this.lookDistance * this.lookDistance)
                && this.isHoldingFood(this.player);
    }

    @Override
    public void start() {
        this.raccoon.setBegging(true);
    }

    @Override
    public void stop() {
        this.raccoon.setBegging(false);
        this.player = null;
    }

    @Override
    public void tick() {
        this.raccoon.getLookControl().setLookAt(this.player, 30.0F, 30.0F);
    }

    private boolean isHoldingFood(Player player) {
        return this.raccoon.isInterestingFood(player.getMainHandItem()) || this.raccoon.isInterestingFood(player.getOffhandItem());
    }
}
