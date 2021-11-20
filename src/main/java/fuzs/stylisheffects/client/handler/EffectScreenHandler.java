package fuzs.stylisheffects.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.CompactEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.VanillaEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.AbstractContainerScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import org.jetbrains.annotations.Nullable;

public class EffectScreenHandler {
    public static final EffectScreenHandler INSTANCE = new EffectScreenHandler();

    @Nullable
    public AbstractEffectRenderer inventoryRenderer;
    @Nullable
    public AbstractEffectRenderer hudRenderer;

    private EffectScreenHandler() {

    }

    public void createEffectRenderers() {
        this.inventoryRenderer = createEffectRenderer(StylishEffects.CONFIG.client().inventoryRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.INVENTORY);
        this.hudRenderer = createEffectRenderer(StylishEffects.CONFIG.client().hudRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.HUD);
    }

    @Nullable
    private static AbstractEffectRenderer createEffectRenderer(ClientConfig.EffectRenderer rendererType, AbstractEffectRenderer.EffectRendererType effectRendererType) {
        return switch (rendererType) {
            case VANILLA -> new VanillaEffectRenderer(effectRendererType);
            case COMPACT -> new CompactEffectRenderer(effectRendererType);
            default -> null;
        };
    }

    public void onRenderGameOverlayText(PoseStack poseStack, int screenWidth, int screenHeight) {
        // use this event so potion icons are drawn behind debug menu as in vanilla
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer hudRenderer = this.hudRenderer;
        if (hudRenderer != null) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.player.getActiveEffects().isEmpty()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().hudRenderer().screenSide;
                hudRenderer.setScreenDimensions(minecraft.gui, screenWidth, screenHeight, screenSide.right() ? screenWidth : 0, 0, screenSide);
                hudRenderer.setActiveEffects(minecraft.player.getActiveEffects());
                hudRenderer.renderEffects(poseStack, minecraft);
            }
        }
    }

    public void onInitGuiPost(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = this.inventoryRenderer;
        if (inventoryRenderer != null && this.supportsEffectsDisplay(screen)) {
            ScreenEvents.afterRender(screen).register(this::onDrawScreenPost);
        }
    }

    public void onDrawScreenPost(Screen screen, PoseStack matrices, int mouseX, int mouseY, float tickDelta) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = this.inventoryRenderer;
        if (inventoryRenderer != null && this.supportsEffectsDisplay(screen)) {
            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            final Minecraft minecraft = Screens.getClient(containerScreen);
            if (!minecraft.player.getActiveEffects().isEmpty()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().inventoryRenderer().screenSide;
                // names same as Forge
                AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
                inventoryRenderer.setScreenDimensions(containerScreen, !screenSide.right() ? accessor.getGuiLeft() : containerScreen.width - (accessor.getGuiLeft() + accessor.getXSize()), accessor.getYSize(), !screenSide.right() ? accessor.getGuiLeft() : accessor.getGuiLeft() + accessor.getXSize(), accessor.getGuiTop(), screenSide);
                inventoryRenderer.setActiveEffects(minecraft.player.getActiveEffects());
                inventoryRenderer.renderEffects(matrices, minecraft);
                inventoryRenderer.getHoveredEffectTooltip(mouseX, mouseY).ifPresent(tooltip -> containerScreen.renderComponentTooltip(matrices, tooltip, mouseX, mouseY));
            }
        }
    }

    public boolean supportsEffectsDisplay(Screen screen) {
        if (screen instanceof EffectRenderingInventoryScreen) {
            return true;
        }
        if (StylishEffects.CONFIG.client().inventoryRenderer().effectsEverywhere && screen instanceof AbstractContainerScreen) {
            if (screen instanceof RecipeUpdateListener) {
                if (((RecipeUpdateListener) screen).getRecipeBookComponent().isVisible()) {
                    return StylishEffects.CONFIG.client().inventoryRenderer().screenSide == ClientConfig.ScreenSide.RIGHT;
                }
            }
            return true;
        }
        return false;
    }
}
