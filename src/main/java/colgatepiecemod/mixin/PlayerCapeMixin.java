package colgatepiecemod.mixin;

import colgatepiecemod.ModItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class PlayerCapeMixin {

    // The location of your cape texture
    private static final Identifier WHITE_CAPE = Identifier.of("colgatepiecemod", "textures/entity/white_cape.png");

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void applyCustomCape(CallbackInfoReturnable<SkinTextures> cir) {
        // "this" refers to the player
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;

        // Check what is in the chestplate slot
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);

        // If it is our SuperBullying Chestplate...
        if (chest.isOf(ModItems.SUPERBULLYING_CHESTPLATE)) {
            SkinTextures old = cir.getReturnValue();

            // We give them back their normal skin, but inject our custom cape!
            SkinTextures newTextures = new SkinTextures(
                    old.texture(),
                    old.textureUrl(),
                    WHITE_CAPE, // Cape Texture
                    WHITE_CAPE, // Elytra Texture (optional, makes elytra match cape)
                    old.model(),
                    old.secure()
            );

            cir.setReturnValue(newTextures);
        }
    }
}