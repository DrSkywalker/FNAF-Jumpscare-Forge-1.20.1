package lee.fnafmod.neoforge.client;

import lee.fnafmod.platform.ClientEventHandlerImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = lee.fnafmod.fnafmod.MOD_ID, dist = Dist.CLIENT)
public class FnafModNeoForgeClient {

    public FnafModNeoForgeClient(IEventBus modBus) {
        NeoForgeClientEventHandler handler = new NeoForgeClientEventHandler(modBus);
        ClientEventHandlerImpl.setInstance(handler);
        lee.fnafmod.fnafmod.initClient();
    }
}
