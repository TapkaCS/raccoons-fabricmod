package com.tapkacs.raccoons.command;

import com.mojang.brigadier.CommandDispatcher;
import com.tapkacs.raccoons.network.OpenConfigScreenPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/** Server command that tells the invoking player's client to open {@code RaccoonConfigScreen}. */
public final class RaccoonConfigCommand {

    private RaccoonConfigCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("raccoonconfig")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ServerPlayNetworking.send(player, new OpenConfigScreenPayload());
                    return 1;
                }));
    }
}
