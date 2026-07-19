package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

public class RaccoonFollowOwnerGoal extends FollowOwnerGoal {

    private final RaccoonEntity raccoon;

    public RaccoonFollowOwnerGoal(RaccoonEntity raccoon, double speedModifier, float startDistance, float stopDistance) {
        super(raccoon, speedModifier, startDistance, stopDistance);
        this.raccoon = raccoon;
    }

    @Override
    public boolean canUse() {
        return this.raccoon.getBehaviorMode() == RaccoonEntity.BehaviorMode.FOLLOW && super.canUse();
    }
}
