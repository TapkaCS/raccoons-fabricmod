package com.tapkacs.raccoons.client;

import com.tapkacs.raccoons.client.entity.RaccoonRenderer;
import com.tapkacs.raccoons.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RaccoonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntityTypes.RACCOON, RaccoonRenderer::new);
    }
}
