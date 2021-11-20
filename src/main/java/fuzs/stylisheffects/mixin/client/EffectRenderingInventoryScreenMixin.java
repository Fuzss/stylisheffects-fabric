package fuzs.stylisheffects.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public EffectRenderingInventoryScreenMixin(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "checkEffectRendering", at = @At("HEAD"), cancellable = true)
    protected void checkEffectRendering(CallbackInfo callbackInfo) {
        // prevent screen from shifting when potion effects are active
        callbackInfo.cancel();
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void renderEffects(PoseStack poseStack, CallbackInfo callbackInfo) {
        // cancel vanilla effect rendering, we use screen effects for our rendering as we're not just targeting EffectRenderingInventoryScreen's
        callbackInfo.cancel();
    }
}
