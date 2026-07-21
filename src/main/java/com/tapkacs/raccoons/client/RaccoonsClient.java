package com.tapkacs.raccoons.client;

import com.tapkacs.raccoons.client.command.RaccoonConfigCommand;
import com.tapkacs.raccoons.client.entity.RaccoonRenderer;
import com.tapkacs.raccoons.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RaccoonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntityTypes.RACCOON, RaccoonRenderer::new);
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> RaccoonConfigCommand.register(dispatcher));
    }
}
