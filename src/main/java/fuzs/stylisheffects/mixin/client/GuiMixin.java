package fuzs.stylisheffects.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// lower priority for bedrockify mod compat, otherwise our renderer doesn't work at all as they cancel the method too
@Mixin(value = Gui.class, priority = 900)
public abstract class GuiMixin extends GuiComponent {
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    protected void renderEffects(PoseStack poseStack, CallbackInfo callbackInfo) {
        // use our own effect renderer instead of vanilla
        EffectScreenHandler.INSTANCE.onRenderGameOverlayText(poseStack, this.screenWidth, this.screenHeight);
        callbackInfo.cancel();
    }
}
