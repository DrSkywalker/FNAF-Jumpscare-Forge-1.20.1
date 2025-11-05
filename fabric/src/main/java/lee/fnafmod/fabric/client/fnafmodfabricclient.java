package lee.fnafmod.fabric.client;

import lee.fnafmod.platform.ClientEventHandlerImpl;
import net.fabricmc.api.ClientModInitializer;

import static lee.fnafmod.fnafmod.initClient;

public final class fnafmodfabricclient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEventHandlerImpl.setInstance(new FabricClientEventHandler());
        initClient();
    }
}