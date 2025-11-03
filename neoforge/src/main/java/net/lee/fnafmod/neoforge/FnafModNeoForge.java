package net.lee.fnafmod.neoforge;

import net.lee.fnafmod.FnafMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(FnafMod.MOD_ID)
public class FnafModNeoForge {
    public FnafModNeoForge(IEventBus modBus) {
        FnafMod.init();
        
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FnafMod.initClient();
        }
    }
}
