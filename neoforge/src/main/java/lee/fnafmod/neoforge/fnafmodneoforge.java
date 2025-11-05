package lee.fnafmod.neoforge;

import lee.fnafmod.neoforge.network.NeoForgeNetworkHandler;
import lee.fnafmod.network.NetworkHandlerImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(lee.fnafmod.fnafmod.MOD_ID)
public final class fnafmodneoforge {
    public fnafmodneoforge(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerPayloads);
        lee.fnafmod.fnafmod.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandlerImpl.setInstance(new NeoForgeNetworkHandler());
        });
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(lee.fnafmod.fnafmod.MOD_ID);
        NeoForgeNetworkHandler.register(registrar);
    }
}
