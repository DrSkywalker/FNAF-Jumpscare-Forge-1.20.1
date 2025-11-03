package net.lee.fnafmod.neoforge.client;

import net.lee.fnafmod.FnafMod;
import net.lee.fnafmod.neoforge.client.NeoForgeClientEventHandler;
import net.lee.fnafmod.platform.ClientEventHandlerImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = FnafMod.MOD_ID, dist = Dist.CLIENT)
public class FnafModNeoForgeClient {
    
    public FnafModNeoForgeClient(IEventBus modBus) {
        // Set up platform-specific client event handler  
        NeoForgeClientEventHandler handler = new NeoForgeClientEventHandler(modBus);
        ClientEventHandlerImpl.setInstance(handler);
        
        // Run common client setup
        FnafMod.initClient();
    }
}
