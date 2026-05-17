package com.artem.artemmod.item;

import com.artem.artemmod.ArtemMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    public static final Item ZEUS_HAND = register(
            "zeus_hand",
            ZeusHandItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .durability(128)
    );

    public static final Item CURSED_ELIXIR = register(
            "cursed_elixir",
            CursedElixirItem::new,
            new Item.Properties()
                    .stacksTo(16)
    );

    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties properties) {
        ResourceKey<Item> itemKey = ResourceKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ArtemMod.MOD_ID, name)
        );

        T item = itemFactory.apply(properties.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
                .register(entries -> entries.accept(ZEUS_HAND));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.accept(CURSED_ELIXIR));
    }
}
