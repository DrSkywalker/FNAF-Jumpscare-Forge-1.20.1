package net.lee.fnafmod.neoforge;

import net.lee.fnafmod.FnafMod;
import net.lee.fnafmod.neoforge.network.NeoForgeNetworkHandler;
import net.lee.fnafmod.network.NetworkHandlerImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(FnafMod.MOD_ID)
public final class FnafModNeoForge {
    
    public FnafModNeoForge(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerPayloads);
        
        // Run common setup
        FnafMod.init();
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Set up platform-specific network handler
            NetworkHandlerImpl.setInstance(new NeoForgeNetworkHandler());
        });
    }
    
    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(FnafMod.MOD_ID);
        NeoForgeNetworkHandler.register(registrar);
    }
}
