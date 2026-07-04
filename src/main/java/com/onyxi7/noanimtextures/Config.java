package com.onyxi7.noanimtextures;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = "noanimtextures", name = "NoAnimTextures")
public class Config {
    
    @Config.Comment("Enable/disable texture animation disabling")
    public static boolean disableAnimations = true;
    
    @Mod.EventBusSubscriber(modid = "noanimtextures")
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals("noanimtextures")) {
                ConfigManager.sync("noanimtextures", Config.Type.INSTANCE);
            }
        }
    }
}