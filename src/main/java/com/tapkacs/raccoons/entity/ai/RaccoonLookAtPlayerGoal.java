package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

/** Stops tracking the player once the raccoon has settled into its sleeping pose, so it doesn't keep turning its head mid-nap. */
public class RaccoonLookAtPlayerGoal extends LookAtPlayerGoal {
    private final RaccoonEntity raccoon;

    public RaccoonLookAtPlayerGoal(RaccoonEntity raccoon, float lookDistance) {
        super(raccoon, Player.class, lookDistance);
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
