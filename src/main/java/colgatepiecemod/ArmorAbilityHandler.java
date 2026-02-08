package colgatepiecemod;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ArmorAbilityHandler {

    private static final int MAX_ENERGY = 60; // 60 ticks = 3 seconds
    private static final String ENERGY_KEY = "FlightEnergy";
    private static final String RECHARGE_KEY = "IsRecharging";

    public static void tickFullSetAbility(ItemStack chestStack, PlayerEntity player) {
        // 1. Check if wearing full set
        if (!isWearingFullSet(player)) {
            disableFlight(player);
            return;
        }

        // 2. Load Data (Energy & State)
        NbtCompound nbt = getOrInitNbt(chestStack);
        int energy = nbt.getInt(ENERGY_KEY);
        boolean isRecharging = nbt.getBoolean(RECHARGE_KEY);

        // 3. Logic Flow
        if (isRecharging) {
            disableFlight(player);
            // Charge up
            if (energy < MAX_ENERGY) {
                energy++;
                showActionBar(player, energy);
            } else {
                // Fully charged!
                isRecharging = false;
                player.sendMessage(Text.translatable("tooltip.colgatepiecemod.flight_ready").formatted(Formatting.GREEN), true);
            }
        } else {
            // Flight Mode Available
            player.getAbilities().allowFlying = true;

            if (player.getAbilities().flying) {
                // Drain energy while flying
                energy--;
                showActionBar(player, energy);

                if (energy <= 0) {
                    // Out of energy! Crash land and start recharge
                    isRecharging = true;
                    disableFlight(player);
                    player.sendMessage(Text.translatable("tooltip.colgatepiecemod.energy_depleted").formatted(Formatting.RED), true);
                }
            } else if (energy < MAX_ENERGY) {
                // Passive recharge if not flying, but not empty yet
                energy++;
            }
            player.sendAbilitiesUpdate(); // Sync with client
        }

        // 4. Save Data
        saveNbt(chestStack, nbt, energy, isRecharging);
    }

    private static void disableFlight(PlayerEntity player) {
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().allowFlying = false;
            player.getAbilities().flying = false;
            player.sendAbilitiesUpdate();
        }
    }

    private static void showActionBar(PlayerEntity player, int currentEnergy) {

        int barsFilled = (int) (((float) currentEnergy / MAX_ENERGY) * 30);

        String bar = "§a" + // green
                "|".repeat(Math.max(0, barsFilled)) +
                "§7" + // gray
                "¦".repeat(Math.max(0, 30 - barsFilled));

        player.sendMessage(Text.literal(bar), true);
    }

    private static boolean isWearingFullSet(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.SUPERBULLYING_HELMET) &&
                player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.SUPERBULLYING_CHESTPLATE) &&
                player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.SUPERBULLYING_LEGGINGS) &&
                player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.SUPERBULLYING_BOOTS);
    }

    // --- NBT Helpers for 1.21.1 ---
    private static NbtCompound getOrInitNbt(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = (component != null) ? component.copyNbt() : new NbtCompound();

        if (!nbt.contains(ENERGY_KEY)) {
            nbt.putInt(ENERGY_KEY, MAX_ENERGY);
            nbt.putBoolean(RECHARGE_KEY, false);
        }
        return nbt;
    }

    private static void saveNbt(ItemStack stack, NbtCompound nbt, int energy, boolean recharging) {
        nbt.putInt(ENERGY_KEY, energy);
        nbt.putBoolean(RECHARGE_KEY, recharging);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}