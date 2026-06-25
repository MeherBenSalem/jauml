package tn.naizo.jauml;

import net.fabricmc.api.ModInitializer;
import tn.naizo.jauml.api.JaumlInitializer;

public class Jauml implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world from Jauml (1.20.1)!");
        JaumlInitializer.initialize();
    }
}
