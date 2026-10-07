package net.tobsend.xtonesreworked.scroll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tobsend.xtonesreworked.item.ModItems;

public final class BlockScrollCatalog {
    private BlockScrollCatalog() {}

    public static boolean contains(Item item) {
        return Catalog.POSITIONS.containsKey(item);
    }

    public static Item next(Item item, int direction, boolean families) {
        Position position = Catalog.POSITIONS.get(item);
        if (position == null || (direction != -1 && direction != 1)) return item;
        if (families) {
            var variants = Catalog.FAMILIES.get(Math.floorMod(position.family + direction, Catalog.FAMILIES.size()));
            return variants.getOrDefault(position.variant, variants.firstEntry().getValue());
        }
        var variants = Catalog.FAMILIES.get(position.family);
        var next = direction > 0 ? variants.higherEntry(position.variant) : variants.lowerEntry(position.variant);
        return (next != null ? next : direction > 0 ? variants.firstEntry() : variants.lastEntry()).getValue();
    }

    public static ItemStack convert(ItemStack stack, int direction, boolean families) {
        Item target = next(stack.getItem(), direction, families);
        return target == stack.getItem() ? stack : stack.transmuteCopy(target);
    }

    private record Position(int family, int variant) {}

    // Deferred items are resolved only when scrolling is first used, after registration.
    private static final class Catalog {
        private static final List<NavigableMap<Integer, Item>> FAMILIES = new ArrayList<>();
        private static final Map<Item, Position> POSITIONS = new HashMap<>();

        static {
            var grouped = new TreeMap<String, NavigableMap<Integer, Item>>();
            var pattern = Pattern.compile("(.+)_block_(\\d+)");
            for (var entry : ModItems.ITEMS.getEntries()) {
                Item item = entry.get();
                if (!(item instanceof BlockItem)) continue;
                String name = entry.getId().getPath();
                var match = pattern.matcher(name);
                boolean numbered = match.matches();
                String family = numbered ? match.group(1) : name;
                int variant = numbered ? Integer.parseInt(match.group(2)) : 0;
                grouped.computeIfAbsent(family, ignored -> new TreeMap<>()).put(variant, item);
            }
            for (var variants : grouped.values()) {
                int family = FAMILIES.size();
                variants.forEach((variant, item) -> POSITIONS.put(item, new Position(family, variant)));
                FAMILIES.add(variants);
            }
        }
    }
}
