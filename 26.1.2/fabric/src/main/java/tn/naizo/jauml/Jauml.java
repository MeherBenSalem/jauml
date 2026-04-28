package tn.naizo.jauml;

import net.fabricmc.api.ModInitializer;

public class Jauml implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world from Jauml!");
        // JaumlConfigLib is static, so it's ready to use.
        // We can add specific initialization here if needed.
    }
}
