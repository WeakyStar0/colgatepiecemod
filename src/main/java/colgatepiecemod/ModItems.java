package colgatepiecemod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item MANO_MANUELS_KATANA = new SwordItem(
            ToolMaterials.DIAMOND,
            new Item.Settings().attributeModifiers(
                    SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 4, -1.7f)
            )
    );

    public static void registerItems() {
        Registry.register(
                Registries.ITEM,
                Identifier.of(ColgatePieceMC.MOD_ID, "mano_manuels_katana"),
                MANO_MANUELS_KATANA
        );

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(MANO_MANUELS_KATANA);
        });
    }
}
