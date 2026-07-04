package com.onyxi7.noanimtextures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "noanimtextures", name = "NoAnimTextures", version = "1.0.0")
public class NoAnimTextures {
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("[NoAnimTextures] Mod loaded successfully!");
    }
}