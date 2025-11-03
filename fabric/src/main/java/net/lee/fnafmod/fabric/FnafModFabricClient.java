package net.lee.fnafmod.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.lee.fnafmod.FnafMod;

public class FnafModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FnafMod.initClient();
    }
}
