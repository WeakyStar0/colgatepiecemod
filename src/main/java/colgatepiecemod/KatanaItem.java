package colgatepiecemod;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.tooltip.TooltipType;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;

import java.util.List;

public class KatanaItem extends SwordItem {

    public KatanaItem(ToolMaterial toolMaterial, Item.Settings settings) {
        super(toolMaterial, settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(user.getStackInHand(hand));
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        // Calculate hold time
        int heldTime = this.getMaxUseTime(stack, user) - remainingUseTicks;

        // Minimum threshold: Must hold for at least 5 ticks (0.25 seconds)
        if (heldTime >= 5) {
            // Calculate Power (0.0 to 1.0) based on 20 ticks (1 second) max
            float power = Math.min((float) heldTime / 20f, 1.0f);

            Vec3d lookVec = player.getRotationVec(1.0F);

            // 1. Velocity Scales (Min 0.8 speed, Max 2.5 speed)
            double velocity = 0.2 + (1.3 * power);
            player.setVelocity(lookVec.x * velocity, 0.1, lookVec.z * velocity);
            player.velocityModified = true;

            // 2. Sound Pitch Scales (Low pitch = weak dash, High pitch = strong dash)
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5f, 0.5f + power);

            // 3. Damage Scales (Min 5.0 damage, Max 12.0 damage)
            float damage = 5.0f + (7.0f * power);

            // 4. Start Dash with specific duration and damage
            int dashDuration = (int) (5 + (5 * power)); // Between 5 and 10 ticks duration
            startNewDash(stack, dashDuration, damage);

            stack.damage(3, player, EquipmentSlot.MAINHAND);

            // 5. Start Cooldown immediately
            player.getItemCooldownManager().set(this, 30);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof PlayerEntity player)) return;

        int timer = getDashTimer(stack);

        if (timer > 0) {
            damageEntitiesInPath(world, player, stack);

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.CLOUD,
                        player.getX(), player.getY() + 1, player.getZ(),
                        3, 0.1, 0.1, 0.1, 0.02);
            }

            // Decrease timer
            setDashTimer(stack, timer - 1);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        // 1. Lore line (Gray)
        tooltip.add(Text.translatable("item.colgatepiecemod.mano_manuels_katana.tooltip.1").formatted(Formatting.GRAY));

        // 2. Empty line
        tooltip.add(Text.empty());

        // 3. Ability Title
        tooltip.add(Text.translatable("tooltip.colgatepiecemod.ability").formatted(Formatting.GRAY)
                .append(Text.translatable("tooltip.colgatepiecemod.super_dash").formatted(Formatting.GOLD)));

        // 4. Instructions
        tooltip.add(Text.translatable("tooltip.colgatepiecemod.hold_charge").formatted(Formatting.DARK_GRAY));

        super.appendTooltip(stack, context, tooltip, type);
    }

    private void damageEntitiesInPath(World world, PlayerEntity player, ItemStack stack) {
        Box dashArea = player.getBoundingBox().expand(1.2, 1.0, 1.2);
        List<Entity> entities = world.getOtherEntities(player, dashArea);
        Vec3d lookVec = player.getRotationVec(1.0F);

        // Retrieve the damage we calculated when the dash started
        float damageAmount = getDashDamage(stack);

        for (Entity target : entities) {
            if (target instanceof LivingEntity livingTarget && target.isAlive()) {
                if (!hasAlreadyHit(stack, target)) {

                    // Deal the dynamic damage
                    livingTarget.damage(world.getDamageSources().playerAttack(player), damageAmount);
                    livingTarget.takeKnockback(0.5, -lookVec.x, -lookVec.z);

                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.CRIT,
                                target.getX(), target.getY() + 1, target.getZ(),
                                30, 0.5, 0.5, 0.5, 0.2);
                    }

                    world.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 1.0f);

                    markAsHit(stack, target);
                }
            }
        }
    }

    // --- NBT HELPERS (Modified to store Damage) ---

    private void startNewDash(ItemStack stack, int ticks, float damage) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putInt("DashTimer", ticks);
        nbt.putFloat("DashDamage", damage); // Save the calculated damage
        nbt.put("HitEntities", new NbtList());
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private float getDashDamage(ItemStack stack) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent != null) {
            return nbtComponent.copyNbt().getFloat("DashDamage");
        }
        return 5.0f; // Default fallback
    }

    private void markAsHit(ItemStack stack, Entity target) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtList hitList = nbt.getList("HitEntities", NbtElement.INT_TYPE);
        hitList.add(net.minecraft.nbt.NbtInt.of(target.getId()));
        nbt.put("HitEntities", hitList);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private boolean hasAlreadyHit(ItemStack stack, Entity target) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent != null) {
            NbtCompound nbt = nbtComponent.copyNbt();
            NbtList hitList = nbt.getList("HitEntities", NbtElement.INT_TYPE);
            for (int i = 0; i < hitList.size(); i++) {
                if (hitList.getInt(i) == target.getId()) return true;
            }
        }
        return false;
    }

    private void setDashTimer(ItemStack stack, int ticks) {
        // We only want to update the timer, not wipe the other data (like damage or hit list)
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putInt("DashTimer", ticks);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private int getDashTimer(ItemStack stack) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent != null) {
            return nbtComponent.copyNbt().getInt("DashTimer");
        }
        return 0;
    }


}