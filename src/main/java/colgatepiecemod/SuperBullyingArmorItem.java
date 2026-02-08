package colgatepiecemod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public class SuperBullyingArmorItem extends ArmorItem {

    public SuperBullyingArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        // Only run on the server (logic) and if it's a player
        if (!world.isClient() && entity instanceof PlayerEntity player) {

            // Only run if this specific item is being worn in the Chest slot
            if (player.getEquippedStack(EquipmentSlot.CHEST) == stack) {
                ArmorAbilityHandler.tickFullSetAbility(stack, player);
            }
        }
    }
}