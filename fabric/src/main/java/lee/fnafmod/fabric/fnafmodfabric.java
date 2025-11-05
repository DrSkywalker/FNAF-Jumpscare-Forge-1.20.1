package lee.fnafmod.fabric;

import lee.fnafmod.fabric.network.FabricNetworkHandler;
import lee.fnafmod.network.NetworkHandlerImpl;
import net.fabricmc.api.ModInitializer;

public final class fnafmodfabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NetworkHandlerImpl.setInstance(new FabricNetworkHandler());
        lee.fnafmod.fnafmod.init();
    }
}
