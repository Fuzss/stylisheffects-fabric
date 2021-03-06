package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractEffectRenderer implements IEffectWidget, IHasRenderAreas {
    protected static final ResourceLocation EFFECT_BACKGROUND = new ResourceLocation(StylishEffects.MOD_ID,"textures/gui/mob_effect_background.png");

    private final EffectRendererType type;
    private GuiComponent screen;
    private int availableWidth;
    private int availableHeight;
    private int startX;
    private int startY;
    private ClientConfig.ScreenSide screenSide;
    protected List<MobEffectInstance> activeEffects;

    protected AbstractEffectRenderer(EffectRendererType type) {
        this.type = type;
    }

    public void setScreenDimensions(GuiComponent screen, int availableWidth, int availableHeight, int startX, int startY, ClientConfig.ScreenSide screenSide) {
        this.screen = screen;
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
        this.startX = startX;
        this.startY = startY;
        this.screenSide = screenSide;
        switch (this.type) {
            case HUD -> {
                this.screenSide = this.screenSide.inverse();
                this.availableWidth -= ((ClientConfig.HudRendererConfig) this.config()).offsetX;
                this.availableHeight -= ((ClientConfig.HudRendererConfig) this.config()).offsetY;
                this.startX += (this.screenSide.right() ? 1 : -1) * ((ClientConfig.HudRendererConfig) this.config()).offsetX;
                this.startY += ((ClientConfig.HudRendererConfig) this.config()).offsetY;
            }
            case INVENTORY -> this.availableWidth -= ((ClientConfig.InventoryRendererConfig) this.config()).screenBorderDistance;
        }
    }

    public final void setActiveEffects(Collection<MobEffectInstance> activeEffects) {
        if (activeEffects.isEmpty()) {
            this.activeEffects = null;
            return;
        }
        this.activeEffects = activeEffects.stream()
                .filter(e -> !this.config().respectHideParticles || e.showIcon())
                .sorted()
                .collect(Collectors.toList());
    }

    public final boolean isActive() {
        return this.activeEffects != null && !this.activeEffects.isEmpty();
    }

    public final boolean isValid() {
        return !this.config().allowFallback || this.getMaxRows() > 0 && this.getMaxColumns() > 0;
    }

    public Function<EffectRendererType, AbstractEffectRenderer> getFallbackRenderer() {
        return type -> null;
    }

    @Override
    public List<Rect2i> getRenderAreas() {
        if (this.isActive()) {
            return this.getEffectPositions(this.activeEffects).stream()
                    .map(Pair::getValue)
                    .map(pos -> new Rect2i(pos[0], pos[1], this.getWidth(), this.getHeight()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public abstract List<Pair<MobEffectInstance, int[]>> getEffectPositions(List<MobEffectInstance> activeEffects);

    protected abstract int getTopOffset();

    protected int[] coordsToEffectPosition(int coordX, int coordY) {
        int[] renderPositions = new int[2];
        switch (this.screenSide) {
            case LEFT -> {
                renderPositions[0] = this.startX - (this.getWidth() + 1) - (this.getWidth() + this.config().widgetSpaceX) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
            case RIGHT -> {
                renderPositions[0] = this.startX + 1 + (this.getWidth() + this.config().widgetSpaceX) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
        }
        return renderPositions;
    }

    public void renderEffects(PoseStack matrixStack, Minecraft minecraft) {
        for (Pair<MobEffectInstance, int[]> entry : this.getEffectPositions(this.activeEffects)) {
            this.renderWidget(matrixStack, entry.getValue()[0], entry.getValue()[1], minecraft, entry.getKey());
        }
    }

    protected float getBlinkingAlpha(MobEffectInstance effectinstance) {
        if (!effectinstance.isAmbient() && effectinstance.getDuration() <= 200) {
            int duration = 10 - effectinstance.getDuration() / 20;
            return Mth.clamp((float) effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float) effectinstance.getDuration() * (float)Math.PI / 5.0F) * Mth.clamp((float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
        }
        return 1.0F;
    }

    private int getAvailableWidth() {
        return Math.min(this.availableWidth, this.config().maxColumns * (this.getWidth() + this.config().widgetSpaceX));
    }

    private int getAvailableHeight() {
        return Math.min(this.availableHeight, this.config().maxRows * (this.getHeight() + this.config().widgetSpaceY));
    }

    private int getMaxColumns() {
        return this.getAvailableWidth() / (this.getWidth() + this.config().widgetSpaceX);
    }

    public int getMaxClampedColumns() {
        return Mth.clamp(this.getMaxColumns(), 1, this.config().maxColumns);
    }

    private int getAdjustedHeight() {
        if (this.config().overflowMode == ClientConfig.OverflowMode.CONDENSE && this.getRows() > this.getMaxClampedRows()) {
            return (this.getAvailableHeight() - this.getHeight()) / Math.max(1, this.getRows() - 1);
        }
        return this.getHeight() + this.config().widgetSpaceY;
    }

    private int getMaxRows() {
        return this.getAvailableHeight() / (this.getHeight() + this.config().widgetSpaceY);
    }

    public int getMaxClampedRows() {
        return Mth.clamp(this.getMaxRows(), 1, this.config().maxRows);
    }

    public int getRows() {
        return this.splitByColumns(this.activeEffects.size());
    }

    protected int splitByColumns(int amountToSplit) {
        return (int) Math.ceil(amountToSplit / (float) this.getMaxClampedColumns());
    }

    protected ClientConfig.EffectRendererConfig config() {
        return switch (this.type) {
            case INVENTORY -> StylishEffects.CONFIG.client().inventoryRenderer();
            case HUD -> StylishEffects.CONFIG.client().hudRenderer();
        };
    }

    protected Optional<MutableComponent> getEffectDuration(MobEffectInstance effectInstance, ClientConfig.LongDurationString longDurationString) {
        String effectDuration = MobEffectUtil.formatDuration(effectInstance, 1.0F);
        if (effectDuration.equals("**:**")) {
            switch (longDurationString) {
                case INFINITY:
                    // infinity char
                    return Optional.of(new TextComponent("\u221e"));
                case NONE:
                    return Optional.empty();
                case VANILLA:
            }
        }
        return Optional.of(new TextComponent(effectDuration));
    }

    public Optional<List<Component>> getHoveredEffectTooltip(int mouseX, int mouseY) {
        if (this.type == EffectRendererType.INVENTORY && StylishEffects.CONFIG.client().inventoryRenderer().hoveringTooltip) {
            return this.getHoveredEffect(mouseX, mouseY)
                    .map(effect -> this.makeEffectTooltip(effect, StylishEffects.CONFIG.client().inventoryRenderer().tooltipDuration));
        }
        return Optional.empty();
    }

    public Optional<MobEffectInstance> getHoveredEffect(int mouseX, int mouseY) {
        for (Map.Entry<MobEffectInstance, int[]> entry : Lists.reverse(this.getEffectPositions(this.activeEffects))) {
            if (this.isMouseOver(entry.getValue()[0], entry.getValue()[1], mouseX, mouseY)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private boolean isMouseOver(int posX, int posY, int mouseX, int mouseY) {
        return mouseX >= posX && mouseX <= posX + this.getWidth() && mouseY >= posY && mouseY <= posY + this.getHeight();
    }

    protected List<Component> makeEffectTooltip(MobEffectInstance effectInstance, boolean withDuration) {
        List<Component> tooltip = Lists.newArrayList();
        String potionName = effectInstance.getEffect().getDescriptionId();
        MutableComponent textComponent = new TranslatableComponent(potionName);
        if (effectInstance.getAmplifier() >= 1 && effectInstance.getAmplifier() <= 9) {
            textComponent.append(" ").append(new TranslatableComponent("enchantment.level." + (effectInstance.getAmplifier() + 1)));
        }
        // description may be provided by Potion Descriptions mod
        String descriptionKey = "description." + potionName;
        if (Language.getInstance().has(descriptionKey)) {
            if (withDuration) {
                // inline duration when there is a description
                textComponent.append(" ").append(new TextComponent("(").append(MobEffectUtil.formatDuration(effectInstance, 1.0F)).append(")").withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(textComponent);
            tooltip.add(new TranslatableComponent(descriptionKey).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(textComponent);
            if (withDuration) {
                tooltip.add(new TextComponent(MobEffectUtil.formatDuration(effectInstance, 1.0F)).withStyle(ChatFormatting.GRAY));
            }
        }
        return tooltip;
    }

    public enum EffectRendererType {
        INVENTORY, HUD
    }
}
