package fuzs.stylisheffects.client;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class StylishEffectsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register(EffectScreenHandler.INSTANCE::onInitGuiPost);
        EffectScreenHandler.INSTANCE.createEffectRenderers();
        StylishEffects.CONFIG.addClientCallback(EffectScreenHandler.INSTANCE::createEffectRenderers);
    }
}
