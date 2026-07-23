package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

/** Stops tracking the player while the raccoon is asleep or moping, so it doesn't keep turning its head mid-nap or mid-sulk. */
public class RaccoonLookAtPlayerGoal extends LookAtPlayerGoal {
    private final RaccoonEntity raccoon;

    public RaccoonLookAtPlayerGoal(RaccoonEntity raccoon, float lookDistance) {
        super(raccoon, Player.class, lookDistance);
        this.raccoon = raccoon;
    }

    @Override
    public boolean canUse() {
        return !this.raccoon.isSleepingPose() && !this.raccoon.isDepressed() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.raccoon.isSleepingPose() && !this.raccoon.isDepressed() && super.canContinueToUse();
    }
}
