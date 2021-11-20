package fuzs.stylisheffects.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.stream.Collectors;

public class StylishEffectsPlugin implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        final EffectRendererGuiHandler handler = new EffectRendererGuiHandler();
        zones.register(AbstractContainerScreen.class, screen -> {
            return handler.getGuiExtraAreas(screen).stream()
                    .map(rect2i -> new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight()))
                    .collect(Collectors.toList());
        });
    }
}
