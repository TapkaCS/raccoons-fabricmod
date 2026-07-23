package com.tapkacs.raccoons.command;

import com.mojang.brigadier.CommandDispatcher;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Dev/preview command: forces an animation state on the nearest raccoon so the animator can
 * inspect each clip in-game without hunting for its natural trigger. The raccoon gets NoAI while
 * a pose is forced (otherwise its goals immediately fight the flags and it wanders off mid-clip);
 * {@code /raccoonanim off} hands it back to its normal AI.
 */
public final class RaccoonAnimCommand {

    private static final double SEARCH_RADIUS = 16.0;

    private RaccoonAnimCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("raccoonanim")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("sleeping").executes(ctx -> apply(ctx.getSource(), "sleeping")))
                .then(Commands.literal("washing").executes(ctx -> apply(ctx.getSource(), "washing")))
                .then(Commands.literal("begging").executes(ctx -> apply(ctx.getSource(), "begging")))
                .then(Commands.literal("jumping").executes(ctx -> apply(ctx.getSource(), "jumping")))
                .then(Commands.literal("cryin").executes(ctx -> apply(ctx.getSource(), "cryin")))
                .then(Commands.literal("depression").executes(ctx -> apply(ctx.getSource(), "depression")))
                .then(Commands.literal("off").executes(ctx -> apply(ctx.getSource(), "off"))));
    }

    private static int apply(CommandSourceStack source, String anim) {
        Vec3 pos = source.getPosition();
        List<RaccoonEntity> nearby = source.getLevel().getEntitiesOfClass(RaccoonEntity.class,
                AABB.ofSize(pos, SEARCH_RADIUS * 2, SEARCH_RADIUS * 2, SEARCH_RADIUS * 2));
        RaccoonEntity raccoon = nearby.stream()
                .min(Comparator.comparingDouble(r -> r.position().distanceToSqr(pos)))
                .orElse(null);
        if (raccoon == null) {
            source.sendFailure(Component.literal("No raccoon within " + (int) SEARCH_RADIUS + " blocks"));
            return 0;
        }

        raccoon.setSleepingPose(false);
        raccoon.setWashing(false);
        raccoon.setBegging(false);
        raccoon.setDoorJumping(false);
        raccoon.setCrying(false);
        raccoon.setDepressed(false);
        raccoon.setNoAi(!anim.equals("off"));

        switch (anim) {
            case "sleeping" -> raccoon.setSleepingPose(true);
            case "washing" -> raccoon.setWashing(true);
            case "begging" -> raccoon.setBegging(true);
            case "jumping" -> raccoon.setDoorJumping(true);
            case "cryin" -> raccoon.setCrying(true);
            case "depression" -> raccoon.setDepressed(true);
            default -> {
            }
        }

        source.sendSuccess(() -> Component.literal(anim.equals("off")
                ? "Raccoon back to normal AI"
                : "Nearest raccoon frozen in '" + anim + "' (use /raccoonanim off to release)"), false);
        return 1;
    }
}
