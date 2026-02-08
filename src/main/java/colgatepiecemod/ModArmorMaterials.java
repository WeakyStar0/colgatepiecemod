package colgatepiecemod;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModArmorMaterials {

    public static final RegistryEntry<ArmorMaterial> SUPERBULLYING = register(
            "superbullying",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
            }),
            15, // enchantability
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, // equip sound
            3.0f, // toughness
            0.05f, // knockback resistance
            () -> Ingredient.ofItems(Items.NETHERITE_INGOT) // repair material
    );



    private static RegistryEntry<ArmorMaterial> register(String id, EnumMap<ArmorItem.Type, Integer> defense, int enchantability, RegistryEntry<net.minecraft.sound.SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        // This line tells the game to look for "superbullying_layer_1.png" and "superbullying_layer_2.png"
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Identifier.of(ColgatePieceMC.MOD_ID, id)));

        ArmorMaterial material = new ArmorMaterial(defense, enchantability, equipSound, repairIngredient, layers, toughness, knockbackResistance);
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(ColgatePieceMC.MOD_ID, id), material);
    }
}