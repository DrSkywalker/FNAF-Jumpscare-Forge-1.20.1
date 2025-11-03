package net.lee.fnafmod.fabric;

import net.fabricmc.api.ModInitializer;
import net.lee.fnafmod.FnafMod;

public class FnafModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FnafMod.init();
    }
}
