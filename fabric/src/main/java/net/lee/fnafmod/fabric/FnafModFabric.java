package net.lee.fnafmod.fabric;

import net.fabricmc.api.ModInitializer;
import net.lee.fnafmod.FnafMod;
import net.lee.fnafmod.fabric.network.FabricNetworkHandler;
import net.lee.fnafmod.network.NetworkHandlerImpl;

public final class FnafModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Set up platform-specific network handler
        NetworkHandlerImpl.setInstance(new FabricNetworkHandler());
        
        // Run common setup
        FnafMod.init();
    }
}
