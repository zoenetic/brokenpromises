package dev.zoenetic.brokenpromises.survival.mixin;

import dev.zoenetic.brokenpromises.survival.emission.EmittingBlock;
import dev.zoenetic.brokenpromises.survival.emission.Emitters;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Final
    @Mutable
    @Shadow
    private int lightEmission;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void brokenpromises$emitterLight(CallbackInfo ci) {
        BlockState state = (BlockState) (Object) this;
        if (state.getBlock() instanceof EmittingBlock) {
            lightEmission = Emitters.lightEmission(state);
        }
    }

}
