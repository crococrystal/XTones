package net.tobsend.xtonesreworked.scroll;

import com.mojang.authlib.GameProfile;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.tobsend.xtonesreworked.XtonesReworkedMod;
import net.tobsend.xtonesreworked.item.ModItems;

@GameTestHolder(XtonesReworkedMod.MODID)
@PrefixGameTestTemplate(false)
public final class BlockScrollGameTest {
    @GameTest(templateNamespace = "minecraft", template = "bastion/treasure/extensions/empty")
    public static void scrollPreservesStacksAndRejectsInvalidRequests(GameTestHelper helper) {
        var families = new HashSet<Item>();
        for (var entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.get();
            helper.assertTrue(BlockScrollCatalog.contains(item), "Missing mod block: " + entry.getId());
            String path = entry.getId().getPath();
            int separator = path.lastIndexOf("_block_");
            if (separator < 0) {
                families.add(item);
                helper.assertTrue(BlockScrollCatalog.next(item, 1, false) == item, "Singleton changed variant");
            } else {
                int variant = Integer.parseInt(path.substring(separator + 7));
                String prefix = path.substring(0, separator + 7);
                helper.assertTrue(BlockScrollCatalog.next(item, 1, false) == item(prefix + (variant + 1) % 16), "Forward variant order: " + path);
                helper.assertTrue(BlockScrollCatalog.next(item, -1, false) == item(prefix + Math.floorMod(variant - 1, 16)), "Reverse variant order: " + path);
                if (variant == 0) families.add(item);
            }
        }
        var visited = new HashSet<Item>();
        Item current = item("agon_block_0");
        for (int i = 0; i < families.size(); i++) {
            helper.assertTrue(visited.add(current), "Family cycle repeated early");
            Item next = BlockScrollCatalog.next(current, 1, true);
            helper.assertTrue(BlockScrollCatalog.next(next, -1, true) == current, "Reverse family order");
            current = next;
        }
        helper.assertTrue(current == item("agon_block_0") && visited.equals(families), "Family cycle misses blocks or fails to wrap");
        helper.assertTrue(BlockScrollCatalog.next(item("agon_block_15"), 1, true) == item("azur_block_15"), "Family switch lost variant number");
        helper.assertTrue(BlockScrollCatalog.next(item("fort_block_15"), -1, true) == item("flat_lamp"), "Missing singleton fallback");
        helper.assertTrue(!BlockScrollCatalog.contains(Items.STONE), "Vanilla block entered catalog");

        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "XTonesScrollTest"));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().selected = 0;
        var original = new ItemStack(item("agon_block_0"), 13);
        original.set(DataComponents.CUSTOM_NAME, Component.literal("Keep my name"));
        var data = new CompoundTag();
        data.putString("scroll_test", "keep my data");
        original.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        original.set(DataComponents.MAX_STACK_SIZE, 16);
        player.getInventory().setItem(0, original);
        ScrollPayload.apply(player, new ScrollPayload(1, false, 0));
        ItemStack changed = player.getMainHandItem();
        helper.assertTrue(changed.is(item("agon_block_1")) && changed.getCount() == 13, "Survival conversion changed count or failed");
        helper.assertTrue(changed.getComponentsPatch().equals(original.getComponentsPatch()), "Conversion lost components");
        helper.assertTrue(original.is(item("agon_block_0")) && original.getCount() == 13, "Conversion mutated source stack");
        ScrollPayload.apply(player, new ScrollPayload(1, true, 0));
        helper.assertTrue(player.getMainHandItem().is(item("azur_block_1")), "Survival family conversion failed");

        ItemStack before = player.getMainHandItem().copy();
        for (var invalid : List.of(new ScrollPayload(0, false, 0), new ScrollPayload(2, false, 0),
            new ScrollPayload(Integer.MIN_VALUE, true, 0), new ScrollPayload(1, false, -1),
            new ScrollPayload(1, false, 9), new ScrollPayload(1, false, 1))) {
            ScrollPayload.apply(player, invalid);
            helper.assertTrue(ItemStack.matches(before, player.getMainHandItem()), "Invalid or stale packet changed inventory");
        }
        player.setGameMode(GameType.SPECTATOR);
        ScrollPayload.apply(player, new ScrollPayload(1, false, 0));
        helper.assertTrue(ItemStack.matches(before, player.getMainHandItem()), "Spectator converted a block");
        player.setGameMode(GameType.SURVIVAL);
        player.setHealth(0);
        ScrollPayload.apply(player, new ScrollPayload(1, false, 0));
        helper.assertTrue(ItemStack.matches(before, player.getMainHandItem()), "Dead player converted a block");
        player.setHealth(20);
        player.containerMenu = new HopperMenu(7, player.getInventory());
        ScrollPayload.apply(player, new ScrollPayload(1, false, 0));
        helper.assertTrue(ItemStack.matches(before, player.getMainHandItem()), "Open menu allowed conversion");
        player.containerMenu = player.inventoryMenu;
        player.getInventory().setItem(0, new ItemStack(Items.STONE, 32));
        ScrollPayload.apply(player, new ScrollPayload(1, true, 0));
        helper.assertTrue(player.getMainHandItem().is(Items.STONE) && player.getMainHandItem().getCount() == 32, "Vanilla block converted");
        helper.succeed();
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(XtonesReworkedMod.MODID, path));
    }
}
