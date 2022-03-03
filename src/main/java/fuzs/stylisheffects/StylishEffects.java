package fuzs.stylisheffects;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.stylisheffects.config.ClientConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StylishEffects implements ModInitializer {
    public static final String MOD_ID = "stylisheffects";
    public static final String MOD_NAME = "Stylish Effects";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<ClientConfig, AbstractConfig> CONFIG = ConfigHolder.client(() -> new ClientConfig());

    @Override
    public void onInitialize() {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
    }
}
