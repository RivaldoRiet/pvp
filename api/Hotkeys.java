package net.runelite.client.plugins.pvp.api;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import api.MenuActions;
import api.Tasks;
import api.Variables;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.Keybind;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.pvp.PvpPlugin;
import net.runelite.client.util.HotkeyListener;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.simplebot.Magic.SpellBook;
import simple.robot.api.ClientContext;

public class Hotkeys {

private final PvpPlugin plugin;
private final Client client;
private ItemManager itemManager;
private PlayerGetter players;

public ArrayList<Integer> onehandMeleeWeapons = new ArrayList<Integer>(Arrays.asList(20407, 23620, 23615, 1215));
public ArrayList<Integer> twohandMeleeWeapons = new ArrayList<Integer>(Arrays.asList(20784, 20557, 20593));
public ArrayList<Integer> onehandRangeWeapons = new ArrayList<Integer>(23619);
public ArrayList<Integer> twohandRangeWeapons = new ArrayList<Integer>(20408);

@Inject
public Hotkeys(PvpPlugin plugin, ItemManager itemManager, PlayerGetter players) {
	this.plugin = plugin;
	client = plugin.getClient();
	this.itemManager = itemManager;
	this.players = players;
}

public HotkeyListener[] get() {

	return new HotkeyListener[] { spell, switcher, speccer, attackplayer };
}

/*
 * private final HotkeyListener melee = new HotkeyListener(() -> new
 * Keybind(KeyEvent.VK_F2, 0)) {
 * 
 * @Override public void hotkeyPressed() { Item[] items =
 * plugin.getItems().getItemsByName("whip", "dragonfire", "oid melee helm",
 * "void top", "void robe", "ragon boots", "arrows glove");
 * Arrays.stream(items).forEach(plugin.getSwitchs()::switchByItem); } };
 * 
 * private final HotkeyListener mage = new HotkeyListener(() -> new
 * Keybind(KeyEvent.VK_F1, 0)) {
 * 
 * @Override public void hotkeyPressed() { Item[] items =
 * plugin.getItems().getItemsByName("ystic hat", "robe top", "robe bottom",
 * "staff", "ystic boot", "ystic glove");
 * Arrays.stream(items).forEach(plugin.getSwitchs()::switchByItem); } };
 */

private final HotkeyListener spell = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F4, 0)) {
@Override
public void hotkeyPressed() {
	System.out.println("Ermh1");
	if (ClientContext.instance().magic.spellBook() == SpellBook.MODERN) {
		castEntangle();
	} else if (ClientContext.instance().magic.spellBook() == SpellBook.ANCIENT) {
		castBarrage();
	} else if (ClientContext.instance().magic.spellBook() == SpellBook.LUNAR) {
		castVenge();
	}
}
};

private void castVenge() {
	plugin.invokeMenuAction("Cast", "<col=00ff00>Vengeance</col>", 1, 57, -1, 14286986);
}

private void castEntangle() {
	useOffensiveMagePray();
	plugin.invokeMenuAction("Cast", "<col=00ff00>Entangle</col>", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286910);
}

private void castBarrage() {
	useOffensiveMagePray();
	plugin.invokeMenuAction("Cast", "<col=00ff00>Ice Barrage</col>", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286926);
}

private int getPrayerLevel() {
	return client.getRealSkillLevel(Skill.PRAYER);
}

public void useOffensiveMeleePray() {
	if (!Variables.USE_PRAYER && !client.isPrayerActive(Prayer.PIETY)) {
		if (getPrayerLevel() >= 70) {
			if (!client.isPrayerActive(Prayer.PIETY)) {
				plugin.invokeMenuAction("Activate", "<col=ff9040>Piety</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_PIETY.getId());
			}
		} else if (getPrayerLevel() >= 31 && getPrayerLevel() < 34) {
			if (!client.isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
				plugin.invokeMenuAction("Activate", "<col=ff9040>Ultimate Strength</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_ULTIMATE_STRENGTH.getId());
			}
		} else if (getPrayerLevel() >= 34) {
			if (!client.isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
				plugin.invokeMenuAction("Activate", "<col=ff9040>Incredible Reflexes</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_INCREDIBLE_REFLEXES.getId());
				plugin.invokeMenuAction("Activate", "<col=ff9040>Ultimate Strength</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_ULTIMATE_STRENGTH.getId());
			}
		} else if (getPrayerLevel() >= 13) {
			if (!client.isPrayerActive(Prayer.SUPERHUMAN_STRENGTH)) {
				plugin.invokeMenuAction("Activate", "<col=ff9040>Clarity of Thought</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_CLARITY_OF_THOUGHT.getId());
				plugin.invokeMenuAction("Activate", "<col=ff9040>Superhuman Strength</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_SUPERHUMAN_STRENGTH.getId());
			}
		}
		return;
	}

	if (ClientContext.instance().skills.realLevel(Skills.PRAYER) < 70) {
		if (!plugin.getClient().isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
			Tasks.getSkill().addPrayer(Prayers.ULTIMATE_STRENGTH);
			Tasks.getSkill().addPrayer(Prayers.INCREDIBLE_REFLEXES);
			// Tasks.getSkill().removePrayer(Prayers.STEEL_SKIN);
			Tasks.getSkill().removePrayer(Prayers.MYSTIC_MIGHT);
			Tasks.getSkill().removePrayer(Prayers.EAGLE_EYE);
		}
	} else {

		if (!plugin.getClient().isPrayerActive(Prayer.PIETY)) {
			Tasks.getSkill().addPrayer(Prayers.PIETY);
			// Tasks.getSkill().removePrayer(Prayers.STEEL_SKIN);
			Tasks.getSkill().removePrayer(Prayers.MYSTIC_MIGHT);
			Tasks.getSkill().removePrayer(Prayers.EAGLE_EYE);
		}
	}
}

public void useOffensiveRangePray() {

	if (!Variables.USE_PRAYER) {
		if (getPrayerLevel() >= 44) {
			if (!client.isPrayerActive(Prayer.EAGLE_EYE)) {
				plugin.invokeMenuAction("Activate", "<col=ff9040>Eagle Eye</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_EAGLE_EYE.getId());
			}
		} else if (getPrayerLevel() >= 8) {
			if (!client.isPrayerActive(Prayer.SHARP_EYE)) {
				plugin.invokeMenuAction("Activate", "<col=ff9040>Sharp Eye</col>", 1, MenuAction.CC_OP.getId(), -1,
						WidgetInfo.PRAYER_SHARP_EYE.getId());
			}
		}
		return;
	}

	if (!plugin.getClient().isPrayerActive(Prayer.EAGLE_EYE)) {
		Tasks.getSkill().addPrayer(Prayers.EAGLE_EYE);
		Tasks.getSkill().removePrayer(Prayers.ULTIMATE_STRENGTH);
		Tasks.getSkill().removePrayer(Prayers.INCREDIBLE_REFLEXES);
		Tasks.getSkill().removePrayer(Prayers.PIETY);
		// Tasks.getSkill().removePrayer(Prayers.STEEL_SKIN);
		Tasks.getSkill().removePrayer(Prayers.MYSTIC_MIGHT);
	}

	/*
	 * if (!plugin.getClient().isPrayerActive(Prayer.STEEL_SKIN)) {
	 * Tasks.getSkill().addPrayer(Prayers.STEEL_SKIN); }
	 */
}

public void useOffensiveMagePray() {
	if (!Variables.USE_PRAYER) {
		int praylvl = getPrayerLevel();
		if (praylvl >= 45) {
			if (!client.isPrayerActive(Prayer.MYSTIC_MIGHT))
			{
				plugin.invokeMenuAction("Activate", "<col=ff9040>Mystic MIGHT</col>", 1 , MenuAction.CC_OP.getId(), -1, WidgetInfo.PRAYER_MYSTIC_MIGHT.getId() );
			}
		}else if (praylvl >= 27) {
			if (!client.isPrayerActive(Prayer.MYSTIC_LORE))
			{
				plugin.invokeMenuAction("Activate", "<col=ff9040>Mystic Lore</col>", 1 , MenuAction.CC_OP.getId(), -1, WidgetInfo.PRAYER_MYSTIC_LORE.getId() );
			}
		}else if(praylvl >= 9){
			if (!client.isPrayerActive(Prayer.MYSTIC_WILL))
			{
				plugin.invokeMenuAction("Activate", "<col=ff9040>Mystic Will</col>", 1 , MenuAction.CC_OP.getId(), -1, WidgetInfo.PRAYER_MYSTIC_WILL.getId() );
			}
		}
		return;
	}
	
	if (!plugin.getClient().isPrayerActive(Prayer.MYSTIC_MIGHT)) {
		Tasks.getSkill().addPrayer(Prayers.MYSTIC_MIGHT);
		Tasks.getSkill().removePrayer(Prayers.EAGLE_EYE);
		Tasks.getSkill().removePrayer(Prayers.PIETY);
		Tasks.getSkill().removePrayer(Prayers.ULTIMATE_STRENGTH);
		Tasks.getSkill().removePrayer(Prayers.INCREDIBLE_REFLEXES);
		// Tasks.getSkill().removePrayer(Prayers.STEEL_SKIN);
	}

	/*
	 * if (!plugin.getClient().isPrayerActive(Prayer.STEEL_SKIN)) {
	 * Tasks.getSkill().addPrayer(Prayers.STEEL_SKIN); }
	 */
}

private final HotkeyListener switcher = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F6, 0)) {

@Override
public void hotkeyPressed() {
	int id = getItem(21);
	ClientContext.instance().getClient().setSpellSelected(false);
	final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
	final String name = getItemDefinition(getItem(1));
	final String amulet = getItemDefinition(getItem(5));
	final String book = getItemDefinition(getItem(8));

	if (isSpecEquipped()) {
		useOffensiveRangePray();
		plugin.invokeMenuAction("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		switchByName("shield");
		return;
	}

	if (name.contains("scimitar") || name.contains("whip") || name.contains("rapier")) { // turn
																							// on
																							// piety
		useOffensiveMeleePray();
		switchItem(getItem(0), 0);
		switchItem(getItem(4), 4);
		switchItem(getItem(1), 1);
		switchItem(getItem(5), 5);
		// attackCurrentPlayer(); }
	}
	if (name.contains("crossbow") || name.contains("ballista") || name.contains("javelin")) {
		System.out.println("YES"); // turn
									// on
									// rigour
		useOffensiveRangePray();
		switchItem(getItem(0), 0);
		switchItem(getItem(4), 4);
		switchItem(getItem(1), 1);
		switchItem(getItem(5), 5);

		// attackCurrentPlayer(); }

	}
	if (name.contains("staff") || name.contains("wand")) {
		switchItem(getItem(0), 0);
		switchItem(getItem(4), 4);
		switchItem(getItem(1), 1);
		switchItem(getItem(5), 5);
	}

	if (amulet.contains("amulet") || amulet.contains("occult") || amulet.contains("book") || amulet.contains("shield")) {
		switchItem(getItem(5), 5);
	}

	if (book.contains("amulet") || book.contains("occult") || book.contains("book") || amulet.contains("shield")) {
		switchItem(getItem(8), 8);

	}
}
};

private String getItemDefinition(int itemId) {
	ItemComposition id = itemManager.getItemComposition(itemId);
	if (id != null) { return id.getName().toLowerCase(); }
	return "";
}

private int getItem(int id) {
	final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
	if (e != null) {
		Item it = e.getItem(id);
		if (it != null) { return it.getId(); }
	}
	return -1;
}

private void switchItem(int itemId, int slot) {
	if (itemId > 0 && slot > -1) {
		plugin.invokeMenuAction("Wield", "Wield", itemId, MenuAction.ITEM_SECOND_OPTION.getId(), slot, 9764864);
	}
}

private final HotkeyListener speccer = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F7, 0)) {

@Override
public void hotkeyPressed() {
	int id = getItem(21);
	ClientContext.instance().getClient().setSpellSelected(false);

	if (isSpecEquipped()) {
		useOffensiveRangePray();
		plugin.invokeMenuAction("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		switchByName("shield");
		return;
	}

	if (onehandMeleeWeapons.contains(id)) {
		System.out.println("SPECCING HARD");
		useOffensiveMeleePray();
		switchByName("infernal");
		switchByName("defender");
		plugin.invokeMenuAction("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		useSpecialAttack();
		players.attackCurrentPlayer();
	}

	if (twohandMeleeWeapons.contains(id)) {
		useOffensiveMeleePray();
		switchByName("infernal");
		switchByName("fire cape");
		plugin.invokeMenuAction("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		useSpecialAttack();
		players.attackCurrentPlayer();
	}

	if (onehandRangeWeapons.contains(id) || twohandRangeWeapons.contains(id)) {
		useOffensiveRangePray();
		plugin.invokeMenuAction("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		switchByName("arrow");
		useSpecialAttack();
		players.attackCurrentPlayer();
	}

	System.out.println("hotkey ayy pressed: " + id);

}
};

private void useSpecialAttack() {
	int specEnabled = client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED);
	if (specEnabled == 0) {
		plugin.invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, MenuAction.CC_OP.getId(), -1, 38862884);
	}
}

private void switchByName(String name) {
	final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
	Item item = getItemByName(name);
	if (item != null) {
		if (e.contains(item.getId())) {
			int slot = getItemBySlot(name);
			switchItem(item.getId(), slot);
		}
	}
}

private int getItemBySlot(String name) {
	final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
	if (e != null && e.getItems().length > 0) {
		for (int i = 0; i < e.getItems().length; i++) {
			final String in = getItemDefinition(e.getItems()[i].getId());
			if (in.contains(name)) { return i; }
		}
	}
	return -1;
}

private Item getItemByName(String name) {
	final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
	for (Item i : e.getItems()) {
		final String in = getItemDefinition(i.getId());
		if (in.contains(name)) { return i; }
	}
	return null;
}

public boolean isSpecEquipped() {
	int handID = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
	if (onehandRangeWeapons.contains(handID)) { return true; }
	if (onehandMeleeWeapons.contains(handID)) { return true; }
	if (twohandMeleeWeapons.contains(handID)) { return true; }

	return false;
}

public void enablePrayer(Prayers prayer) {
	if (ClientContext.instance().skills.level(Skills.PRAYER) == 0) return;
	if (!ClientContext.instance().prayers.prayerActive(prayer))
		MenuActions.invoke("Activate", "<col=ff9040>" + prayer.name().toString().replaceAll(" ", "_").toLowerCase() + "</col>",
				-1, MenuAction.CC_OP.getId(), 1, prayer.getWidgetInfo().getId());

}

private final HotkeyListener attackplayer = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F10, 0)) {

@Override
public void hotkeyPressed() {
	if (isSpecEquipped()) {
		useOffensiveRangePray();
		int id = getItem(21);
		plugin.invokeMenuAction("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		switchByName("shield");
		return;
	}

	switchToWhip();
}
};

private void switchToWhip() {
	final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
	final String name = getItemDefinition(getItem(2));

	if (name.contains("whip")) {
		useOffensiveMeleePray();
		switchByName("whip");
		switchByName("defender");
		switchByName("infernal");
	}

	if (name.contains("rapier")) {
		useOffensiveMeleePray();
		switchByName("rapier");
		switchByName("defender");
		switchByName("infernal");
	}

	if (name.contains("crossbow") || name.contains("javelin")) {
		useOffensiveRangePray();
		switchByName("crossbow");
		switchByName("shield");
		switchByName("guthix");
	}

	if (name.contains("ballista")) {
		useOffensiveRangePray();
		switchByName("ballista");
		switchByName("guthix");
	}

	players.attackCurrentPlayer();
}

}
