package net.lee.fnafmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class FnafNet {
    public static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath("fnafmod", "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private static int id = 0;

    public static void register() {
        CHANNEL.messageBuilder(SpawnMobAfterScareC2S.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SpawnMobAfterScareC2S::encode)
                .decoder(SpawnMobAfterScareC2S::decode)
                .consumerMainThread(SpawnMobAfterScareC2S::handle)
                .add();
    }
}
