package com.tapkacs.raccoons.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.tapkacs.raccoons.client.gui.RaccoonConfigScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class RaccoonConfigCommand {

    private RaccoonConfigCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("raccoonconfig")
                .executes(ctx -> {
                    ctx.getSource().getClient().setScreen(new RaccoonConfigScreen(null));
                    return 1;
                }));
    }
}
