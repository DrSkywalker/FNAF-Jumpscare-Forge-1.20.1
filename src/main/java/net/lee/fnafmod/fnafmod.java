package net.lee.fnafmod;

import net.lee.fnafmod.client.ClientEvents;
import net.lee.fnafmod.network.FnafNet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(fnafmod.MOD_ID)
public class fnafmod {
    public static final String MOD_ID = "fnafmod";

    // ✅ 1.20.1 needs a NO-ARG constructor
    public fnafmod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(this::commonSetup);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEvents::init);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(FnafNet::register);
    }
}
