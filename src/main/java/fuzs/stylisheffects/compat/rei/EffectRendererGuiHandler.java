package fuzs.stylisheffects.compat.rei;

import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collections;
import java.util.List;

public class EffectRendererGuiHandler {
    public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = EffectScreenHandler.createRendererOrFallback(screen);
        if (inventoryRenderer != null) {
            return inventoryRenderer.getRenderAreas();
        }
        return Collections.emptyList();
    }
}
