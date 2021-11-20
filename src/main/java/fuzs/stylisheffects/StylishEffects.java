package fuzs.stylisheffects;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.stylisheffects.config.ClientConfig;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StylishEffects implements ModInitializer {
    public static final String MODID = "stylisheffects";
    public static final String NAME = "Stylish Effects";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<ClientConfig, AbstractConfig> CONFIG = ConfigHolder.client(() -> new ClientConfig());

    @Override
    public void onInitialize() {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MODID);
    }
}
