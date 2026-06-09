package com.example.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import com.example.addon.modules.AutoResell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sl1wedAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("sl1wed-addon");
    
    @Override
    public void onInitialize() {
        LOG.info("Initializing Sl1wed's Addon");
        
        // REGISTER THE MODULE HERE
        Modules.get().add(new AutoResell());
        
        LOG.info("AutoResell module registered successfully!");
    }
    
    @Override
    public void onRegisterCategories() {
        // Register your custom tab/category
        Modules.registerCategory(Categories.SL1WED_ADDON);
    }
    
    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
