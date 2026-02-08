package colgatepiecemod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {


    // >>> WEAPONS <<<


    //mano manuel
    public static final Item MANO_MANUELS_KATANA = new KatanaItem(
            ToolMaterials.DIAMOND,
            new Item.Settings().attributeModifiers(
                    SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 4, -1.7f)
            )
    );

    //el roberto

    //lagarto

    //eduardo

    //superbullying

    //the bully

    //choromelas







    // >>> ARMOR <<<

    //mano manuel

    //el roberto

    //lagarto

    //eduardo

    //superbullying

    public static final Item SUPERBULLYING_HELMET = new ArmorItem(
            ModArmorMaterials.SUPERBULLYING,
            ArmorItem.Type.HELMET,
            new Item.Settings()
    );

    public static final Item SUPERBULLYING_CHESTPLATE = new SuperBullyingArmorItem(
            ModArmorMaterials.SUPERBULLYING,
            ArmorItem.Type.CHESTPLATE,
            new Item.Settings()
    );

    public static final Item SUPERBULLYING_LEGGINGS = new ArmorItem(
            ModArmorMaterials.SUPERBULLYING,
            ArmorItem.Type.LEGGINGS,
            new Item.Settings()
    );

    public static final Item SUPERBULLYING_BOOTS = new ArmorItem(
            ModArmorMaterials.SUPERBULLYING,
            ArmorItem.Type.BOOTS,
            new Item.Settings()
    );

    //the bully

    //choromelas






    public static void registerItems() {
        Registry.register(
                Registries.ITEM,
                Identifier.of(ColgatePieceMC.MOD_ID, "mano_manuels_katana"),
                MANO_MANUELS_KATANA
        );
        Registry.register(Registries.ITEM, Identifier.of(ColgatePieceMC.MOD_ID, "superbullying_helmet"), SUPERBULLYING_HELMET);
        Registry.register(Registries.ITEM, Identifier.of(ColgatePieceMC.MOD_ID, "superbullying_chestplate"), SUPERBULLYING_CHESTPLATE);
        Registry.register(Registries.ITEM, Identifier.of(ColgatePieceMC.MOD_ID, "superbullying_leggings"), SUPERBULLYING_LEGGINGS);
        Registry.register(Registries.ITEM, Identifier.of(ColgatePieceMC.MOD_ID, "superbullying_boots"), SUPERBULLYING_BOOTS);


        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(MANO_MANUELS_KATANA);
            content.add(SUPERBULLYING_HELMET);
            content.add(SUPERBULLYING_CHESTPLATE);
            content.add(SUPERBULLYING_LEGGINGS);
            content.add(SUPERBULLYING_BOOTS);
        });
    }
}
