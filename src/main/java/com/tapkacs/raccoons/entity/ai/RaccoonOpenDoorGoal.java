package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Door opening with showmanship: the raccoon plays its 3-second "Jumping" animation at the door
 * (standing on hind legs, hopping at the handle twice), and the door swings open on the SECOND
 * hop - at its 2.5s peak, when the paws "hit" the handle. The raccoon stays frozen through the
 * landing (until 3.0s), and only then is released to walk through, with the door held open
 * behind it long enough to actually pass.
 *
 * <p>During a gang raid several raccoons can queue at the same door, each running its own
 * instance of this goal on its own schedule. Vanilla {@link OpenDoorGoal#stop()} slams the door
 * shut unconditionally, so without coordination one raccoon finishing would close the door in
 * another's face mid-use. {@link #OPEN_CLAIMS} counts how many raccoons currently claim a given
 * door open (per level); the door only actually closes once the last claim is released.
 */
public class RaccoonOpenDoorGoal extends OpenDoorGoal {
    /** Jumping animation timeline: the second hop peaks at 2.5s - that's when the door opens. */
    private static final int OPEN_DOOR_TICK = 50;
    /** The animation ends (landing finished) at 3.0s - only then does the raccoon move again. */
    private static final int RELEASE_TICK = 60;
    /** The door stays claimed open another 2.5s after release so the raccoon can walk through. */
    private static final int END_TICK = 110;

    private static final Map<Level, Map<BlockPos, Integer>> OPEN_CLAIMS = new HashMap<>();

    private final RaccoonEntity raccoon;
    private int ticks;
    private boolean doorClaimed;

    public RaccoonOpenDoorGoal(RaccoonEntity raccoon) {
        super(raccoon, true);
        this.raccoon = raccoon;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.doorClaimed = false;
        this.raccoon.setDoorJumping(true);
        this.raccoon.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < END_TICK;
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.ticks == OPEN_DOOR_TICK) {
            super.start(); // vanilla start() is what actually swings the door open
            this.claimDoor();
        }
        if (this.ticks == RELEASE_TICK) {
            this.raccoon.setDoorJumping(false);
        }
    }

    @Override
    public void stop() {
        this.releaseDoor();
        this.raccoon.setDoorJumping(false);
    }

    private void claimDoor() {
        if (!this.hasDoor) {
            return;
        }
        OPEN_CLAIMS.computeIfAbsent(this.raccoon.level(), level -> new HashMap<>())
                .merge(this.doorPos, 1, Integer::sum);
        this.doorClaimed = true;
    }

    /** Releases this raccoon's claim; only actually swings the door shut once no one else still holds one. */
    private void releaseDoor() {
        if (!this.doorClaimed) {
            return;
        }
        this.doorClaimed = false;

        Map<BlockPos, Integer> claimsInLevel = OPEN_CLAIMS.get(this.raccoon.level());
        if (claimsInLevel == null) {
            this.setOpen(false);
            return;
        }
        int remaining = claimsInLevel.merge(this.doorPos, -1, Integer::sum);
        if (remaining <= 0) {
            claimsInLevel.remove(this.doorPos);
            if (claimsInLevel.isEmpty()) {
                OPEN_CLAIMS.remove(this.raccoon.level());
            }
            this.setOpen(false);
        }
    }
}
