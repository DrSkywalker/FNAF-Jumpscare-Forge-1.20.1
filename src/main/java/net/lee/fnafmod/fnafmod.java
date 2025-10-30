package net.lee.fnafmod;

import net.lee.fnafmod.client.ClientEvents;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(fnafmod.MOD_ID)
public class fnafmod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "fnafmod";
    public fnafmod(FMLJavaModLoadingContext context) {
        ClientEvents.init();
    }
}
