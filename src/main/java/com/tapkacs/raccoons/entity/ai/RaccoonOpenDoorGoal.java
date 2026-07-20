package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;

/**
 * Door opening with showmanship: the raccoon plays its 3-second "Jumping" animation at the door
 * (standing on hind legs, hopping at the handle), and the door only actually opens for the final
 * second of it. The vanilla goal would open the door instantly on {@link #start()}; here that
 * call - which sets the forget timer and flips the door - is deferred until the wind-up elapses.
 */
public class RaccoonOpenDoorGoal extends OpenDoorGoal {
    /** Jumping animation is 3s total; the door opens for its last second, i.e. after 2s (40 ticks). */
    private static final int JUMP_WINDUP_TICKS = 40;

    private final RaccoonEntity raccoon;
    private int windup;
    private boolean openedDoor;

    public RaccoonOpenDoorGoal(RaccoonEntity raccoon) {
        super(raccoon, true);
        this.raccoon = raccoon;
    }

    @Override
    public void start() {
        this.windup = JUMP_WINDUP_TICKS;
        this.openedDoor = false;
        this.raccoon.setDoorJumping(true);
    }

    @Override
    public boolean canContinueToUse() {
        return this.openedDoor ? super.canContinueToUse() : this.windup > 0;
    }

    @Override
    public void tick() {
        if (!this.openedDoor) {
            if (--this.windup <= 0) {
                this.openedDoor = true;
                super.start();
            }
            return;
        }
        super.tick();
    }

    @Override
    public void stop() {
        super.stop();
        this.raccoon.setDoorJumping(false);
    }
}
