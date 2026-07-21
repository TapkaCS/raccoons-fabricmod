package com.tapkacs.raccoons.entity.ai;

import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Door opening with showmanship: the raccoon plays its 3-second "Jumping" animation at the door
 * (standing on hind legs, hopping at the handle), and the door only actually opens for the final
 * second of it. The vanilla goal would open the door instantly on {@link #start()}; here that
 * call - which sets the forget timer and flips the door - is deferred until the wind-up elapses.
 *
 * <p>During a gang raid several raccoons can queue at the same door, each running its own instance
 * of this goal with its own independent forget timer. Vanilla {@link OpenDoorGoal#stop()} slams the
 * door shut unconditionally, so without coordination one raccoon's timer expiring would close the
 * door in another raccoon's face mid-use. {@link #OPEN_CLAIMS} tracks how many raccoons currently
 * have a given door claimed open (per level) so it only actually closes once nobody needs it.
 */
public class RaccoonOpenDoorGoal extends OpenDoorGoal {
    /** Jumping animation is 3s total; the door opens for its last second, i.e. after 2s (40 ticks). */
    private static final int JUMP_WINDUP_TICKS = 40;

    private static final Map<Level, Map<BlockPos, Integer>> OPEN_CLAIMS = new HashMap<>();

    private final RaccoonEntity raccoon;
    private int windup;
    private boolean openedDoor;
    private boolean doorClaimed;

    public RaccoonOpenDoorGoal(RaccoonEntity raccoon) {
        super(raccoon, true);
        this.raccoon = raccoon;
    }

    @Override
    public void start() {
        this.windup = JUMP_WINDUP_TICKS;
        this.openedDoor = false;
        this.raccoon.setDoorJumping(true);
        this.raccoon.getNavigation().stop();
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
                this.claimDoor();
            }
            return;
        }
        super.tick();
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
