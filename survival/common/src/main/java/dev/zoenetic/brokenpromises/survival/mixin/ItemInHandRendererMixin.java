package dev.zoenetic.brokenpromises.survival.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.zoenetic.brokenpromises.survival.client.ShiverRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void brokenpromises$shiverHands(float frameInterp, PoseStack poseStack,
                                            SubmitNodeCollector submitNodeCollector,
                                            LocalPlayer player, int lightCoords, CallbackInfo ci) {
        ShiverRenderer.INSTANCE.applyToHeldItems(player, frameInterp, poseStack);
    }
}
