package com.tapkacs.raccoons.client;

import com.tapkacs.raccoons.client.entity.RaccoonRenderer;
import com.tapkacs.raccoons.client.gui.RaccoonConfigScreen;
import com.tapkacs.raccoons.entity.ModEntityTypes;
import com.tapkacs.raccoons.network.OpenConfigScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RaccoonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntityTypes.RACCOON, RaccoonRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(OpenConfigScreenPayload.TYPE,
                (payload, context) -> context.client().setScreen(new RaccoonConfigScreen(null)));
    }
}
