package net.runelite.client.plugins.pvp.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import javax.inject.Inject;

import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.client.plugins.pvp.PvpPlugin;

public class ItemGetter {

	private final PvpPlugin plugin;

	@Inject
	public ItemGetter(PvpPlugin plugin) {
		this.plugin = plugin;
	}

	public Item[] getItems() {
		return plugin.getClient().getItemContainer(InventoryID.INVENTORY).getItems();
	}

	public int getSlotById(int id) {
		final ItemContainer e = plugin.getClient().getItemContainer(InventoryID.INVENTORY);
		return IntStream.range(0, getItems().length).filter(val -> {
			Item item = e.getItem(val);
			return item != null && item.getId() == id;
		}).findFirst().orElse(-1);
	}

	public int getSlotByName(String name) {
		Item item = getItemByName(name);
		return getSlotById(item.getId());
	}

	public Item getItemById(int id) {
		return Arrays.stream(getItems()).filter(val -> val.getId() == id).findFirst().orElse(null);
	}

	public Item getItemByName(String name) {
		return Arrays.stream(getItems()).filter(val -> getItemDef(val.getId()).getName().toLowerCase().contains(name)).findFirst()
				.orElse(null);
	}

	public Item[] getItemsByName(String... name) {
		return Arrays.stream(name).map(String::toLowerCase).map(this::getItemByName).filter(Objects::nonNull)
				.toArray(Item[]::new);
	}

	public ItemComposition getItemDef(int itemId) {
		ItemComposition def = plugin.getItemManager().getItemComposition(itemId);
		return def;
	}

}
