package com.artem.artemmod;

import com.artem.artemmod.item.ModItems;
import net.fabricmc.api.ModInitializer;

public class ArtemMod implements ModInitializer {
    public static final String MOD_ID = "artemmod";

    @Override
    public void onInitialize() {
        ModItems.initialize();
    }
}
