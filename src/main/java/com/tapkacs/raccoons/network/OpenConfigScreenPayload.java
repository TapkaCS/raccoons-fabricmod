package com.tapkacs.raccoons.network;

import com.tapkacs.raccoons.Raccoons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Empty S2C signal: "open the config screen locally." Sent by {@link com.tapkacs.raccoons.command.RaccoonConfigCommand}
 * so {@code /raccoonconfig} can go through the ordinary, well-exercised server command dispatcher
 * (Fabric's client-only command dispatcher-build event turned out not to fire reliably) while the
 * actual GUI still only ever opens client-side.
 */
public record OpenConfigScreenPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenConfigScreenPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "open_config_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenConfigScreenPayload> CODEC =
            StreamCodec.unit(new OpenConfigScreenPayload());

    @Override
    public CustomPacketPayload.Type<OpenConfigScreenPayload> type() {
        return TYPE;
    }
}
