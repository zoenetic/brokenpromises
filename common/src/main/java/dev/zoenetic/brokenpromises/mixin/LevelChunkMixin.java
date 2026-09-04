package dev.zoenetic.brokenpromises.mixin;

import dev.zoenetic.brokenpromises.heat.SourcesKt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void brokenpromises$afterSetBlockState(BlockPos pos, BlockState state, int flags,
                                                   CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() == null) return;
        var chunk = (LevelChunk)(Object)this;
        if (chunk.getLevel().isClientSide()) return;
        SourcesKt.updateStateForSingleHeatSource(chunk, pos, state);
    }
}

