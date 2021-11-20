package fuzs.stylisheffects.compat.rei;

import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class EffectRendererGuiHandler {

    public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = EffectScreenHandler.INSTANCE.inventoryRenderer;
        if (inventoryRenderer != null && EffectScreenHandler.INSTANCE.supportsEffectsDisplay(screen)) {
            Minecraft minecraft = Screens.getClient(screen);
            Collection<MobEffectInstance> activePotionEffects = minecraft.player.getActiveEffects();
            if (!activePotionEffects.isEmpty()) {
                return EffectScreenHandler.INSTANCE.inventoryRenderer.getRenderAreas();
            }
        }
        return Collections.emptyList();
    }
}
