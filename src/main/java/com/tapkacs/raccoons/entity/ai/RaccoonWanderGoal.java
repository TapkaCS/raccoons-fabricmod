package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class RaccoonWanderGoal extends WaterAvoidingRandomStrollGoal {

    private final RaccoonEntity raccoon;

    public RaccoonWanderGoal(RaccoonEntity raccoon, double speedModifier) {
        super(raccoon, speedModifier);
        this.raccoon = raccoon;
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.isTame() && this.raccoon.getBehaviorMode() != RaccoonEntity.BehaviorMode.WANDER) {
            return false;
        }
        return super.canUse();
    }
}
