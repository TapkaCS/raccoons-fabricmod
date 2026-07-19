package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;

/** Stops idle head-twitching once the raccoon has settled into its sleeping pose. */
public class RaccoonRandomLookAroundGoal extends RandomLookAroundGoal {
    private final RaccoonEntity raccoon;

    public RaccoonRandomLookAroundGoal(RaccoonEntity raccoon) {
        super(raccoon);
        this.raccoon = raccoon;
    }

    @Override
    public boolean canUse() {
        return !this.raccoon.isSleepingPose() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.raccoon.isSleepingPose() && super.canContinueToUse();
    }
}
