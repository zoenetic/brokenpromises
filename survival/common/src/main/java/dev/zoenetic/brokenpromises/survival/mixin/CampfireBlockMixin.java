package dev.zoenetic.brokenpromises.survival.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zoenetic.brokenpromises.survival.fire.CampfireInteractions;
import dev.zoenetic.brokenpromises.survival.fuel.Fuel;
import dev.zoenetic.brokenpromises.survival.fuel.FuelProperties;
import dev.zoenetic.brokenpromises.survival.fuel.FuelledBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
@Implements(@Interface(iface = FuelledBlock.class, prefix = "brokenpromisesfuelledblock$"))
public abstract class CampfireBlockMixin extends Block {
    private CampfireBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void brokenpromises$defaultUnlit(CallbackInfo ci) {
        registerDefaultState(defaultBlockState()
                .setValue(CampfireBlock.LIT, false)
                .setValue(FuelProperties.FUEL_LEVEL, 15));
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void brokenpromises$properties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(FuelProperties.FUEL_LEVEL);
    }

    public @NonNull Fuel brokenpromisesfuelledblock$getMaxFuel() {
        return new Fuel(15);
    }

    public @NonNull Fuel brokenpromisesfuelledblock$getFuel(@NonNull BlockState state) {
        return null; //TODO
    }

    public @NonNull BlockState brokenpromisesfuelledblock$setFuel(@NonNull BlockState state, @NonNull Fuel fuel) {
        return state; //TODO
    }

    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    private BlockState brokenpromises$placeUnlit(BlockState original, BlockPlaceContext context) {
        return original.setValue(CampfireBlock.LIT, false);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void brokenpromises$refuel(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (CampfireInteractions.maybeRefuel(state, itemStack, level, pos, player)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos,
            @NonNull Player player, @NonNull BlockHitResult hit
    ) {
        return CampfireInteractions.maybeLight(state, level, pos, player);
    }

}