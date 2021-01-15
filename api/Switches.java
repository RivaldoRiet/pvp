package net.runelite.client.plugins.pvp.api;

import javax.inject.Inject;

import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.VarPlayer;
import net.runelite.client.plugins.pvp.PvpPlugin;

public class Switches {

	private final PvpPlugin plugin;

	@Inject
	public Switches(PvpPlugin plugin) {
		this.plugin = plugin;
	}

	private void switchToRobot() {
		/*
		 * Player p = plugin.getPlayers().getCurrPlayer();
		 * 
		 * if (p != null) { final ItemContainer e =
		 * plugin.getClient().getItemContainer(InventoryID.INVENTORY); final
		 * ItemContainer eq =
		 * plugin.getClient().getItemContainer(InventoryID.EQUIPMENT); final
		 * String name =
		 * plugin.getItems().getItemDefinition(plugin.getItems().getItem(2));
		 * int id = plugin.getItems().getItem(21); int handID =
		 * plugin.getClient().getLocalPlayer().getPlayerComposition().
		 * getEquipmentId(KitType.WEAPON); int spec =
		 * plugin.getClient().getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		 * 
		 * if (plugin.isFrozen() && !plugin.getPlayers().isOpponentReachable())
		 * { if (name.contains("crossbow") || name.contains("javelin") ||
		 * name.contains("ballista")) { plugin.useRigour();
		 * switchByName("crossbow"); switchByName("shield");
		 * switchByName("guthix"); switchByName("ballista"); }
		 * plugin.getPlayers().attackCurrentPlayer(); return; }
		 * 
		 * if (plugin.isSpecEquipped()) { if (name.contains("whip") ||
		 * name.contains("rapier")) { plugin.useRigour();
		 * switchByName("crossbow"); switchByName("shield");
		 * switchByName("guthix"); switchByName("ballista"); return; } }
		 * 
		 * if (plugin.isSpecEquipped()) { if (name.contains("crossbow") ||
		 * name.contains("javelin") || name.contains("ballista")) {
		 * plugin.usePiety(); switchByName("defender");
		 * switchByName("infernal"); switchByName("whip");
		 * switchByName("rapier"); return; } }
		 * 
		 * if (p.getOverheadIcon() != null &&
		 * p.getOverheadIcon().equals(HeadIcon.MELEE)) { if (spec >= 500) { if
		 * (plugin.onehandRangeWeapons.contains(id) ||
		 * plugin.onehandRangeWeapons.contains(handID)) { plugin.useRigour();
		 * plugin.invokeMenuAction("Wield", "Wield", id,
		 * MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		 * useSpecialAttack(); plugin.getPlayers().attackCurrentPlayer();
		 * return; } }
		 * 
		 * if (name.contains("crossbow") || name.contains("javelin")) {
		 * plugin.useRigour(); switchByName("crossbow"); switchByName("shield");
		 * switchByName("guthix"); }
		 * 
		 * if (name.contains("ballista")) { plugin.useRigour();
		 * switchByName("ballista"); switchByName("guthix"); }
		 * 
		 * plugin.getPlayers().attackCurrentPlayer(); }
		 * 
		 * if (p.getOverheadIcon() == null) { if
		 * (plugin.onehandMeleeWeapons.contains(id) ||
		 * plugin.onehandMeleeWeapons.contains(handID)) { if (spec >= 250) {
		 * useMeleeSpec(); return; } }
		 * 
		 * if (name.contains("whip")) { plugin.usePiety(); switchByName("whip");
		 * switchByName("defender"); switchByName("infernal"); }
		 * 
		 * if (name.contains("rapier")) { plugin.usePiety();
		 * switchByName("rapier"); switchByName("defender");
		 * switchByName("infernal"); }
		 * plugin.getPlayers().attackCurrentPlayer(); }
		 * 
		 * if (p.getOverheadIcon() != null &&
		 * p.getOverheadIcon().equals(HeadIcon.RANGED) ||
		 * p.getOverheadIcon().equals(HeadIcon.MAGIC)) { if
		 * (plugin.onehandMeleeWeapons.contains(id) ||
		 * plugin.onehandMeleeWeapons.contains(handID)) { if (spec >= 250) {
		 * useMeleeSpec(); return; } }
		 * 
		 * /* if (plugin.twohandMeleeWeapons.contains(id) ||
		 * plugin.twohandMeleeWeapons.contains(handID)) { if(spec >= 500) {
		 * useMeleeSpec(); return; } }
		 */

		/*
		 * if (name.contains("whip")) { plugin.usePiety(); switchByName("whip");
		 * switchByName("defender"); switchByName("infernal"); }
		 * 
		 * if (name.contains("rapier")) { plugin.usePiety();
		 * switchByName("rapier"); switchByName("defender");
		 * switchByName("infernal"); }
		 * plugin.getPlayers().attackCurrentPlayer(); } }
		 */
	}

	private void switchToWhip() {
		/*
		 * final ItemContainer e =
		 * plugin.getClient().getItemContainer(InventoryID.INVENTORY); final
		 * String name =
		 * plugin.getItems().getItemDefinition(plugin.getItems().getItem(2));
		 * 
		 * if (name.contains("whip")) { plugin.usePiety(); switchByName("whip");
		 * switchByName("defender"); switchByName("infernal"); }
		 * 
		 * if (name.contains("rapier")) { plugin.usePiety();
		 * switchByName("rapier"); switchByName("defender");
		 * switchByName("infernal"); }
		 * 
		 * if (name.contains("crossbow") || name.contains("javelin")) {
		 * plugin.useRigour(); switchByName("crossbow"); switchByName("shield");
		 * switchByName("guthix"); }
		 * 
		 * if (name.contains("ballista")) { plugin.useRigour();
		 * switchByName("ballista"); switchByName("guthix"); }
		 * 
		 * plugin.getPlayers().attackCurrentPlayer();
		 */
	}

	public void switchByItem(int id) {
		Item item = plugin.getItems().getItemById(id);
		switchByItem(item);
	}

	public void switchByName(String name) {
		switchByItem(plugin.getItems().getItemByName(name));
	}

	public void switchByItem(Item item) {
		if (item == null) return;
		int slot = plugin.getItems().getSlotById(item.getId());

		if (slot > -1) {
			switchItem(item.getId(), slot);
		}
	}

	private void useSpecialAttack() {
		int specEnabled = plugin.getClient().getVar(VarPlayer.SPECIAL_ATTACK_ENABLED);
		if (specEnabled == 0) {
			plugin.invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, MenuAction.CC_OP.getId(), -1, 38862884);
		}
	}

	private void switchItem(int itemId, int slot) {
		if (itemId > 0 && slot > -1) {
			plugin.invokeMenuAction("Wield", "Wield", itemId, MenuAction.ITEM_SECOND_OPTION.getId(), slot, 9764864);
		}
	}

	public void useMeleeSpec() {
		/*
		 * final ItemContainer e =
		 * plugin.getClient().getItemContainer(InventoryID.INVENTORY); int id =
		 * plugin.getItems().getItem(21); int spec =
		 * plugin.getClient().getVar(VarPlayer.SPECIAL_ATTACK_PERCENT); if (spec
		 * >= 250) { if (plugin.onehandMeleeWeapons.contains(id)) {
		 * plugin.usePiety(); switchByName("infernal");
		 * plugin.invokeMenuAction("Wield", "Wield", 23597,
		 * MenuAction.ITEM_SECOND_OPTION.getId(), 3, 9764864);
		 * plugin.invokeMenuAction("Wield", "Wield", id,
		 * MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		 * useSpecialAttack(); plugin.getPlayers().attackCurrentPlayer(); } } if
		 * (spec >= 500) { if (plugin.twohandMeleeWeapons.contains(id)) {
		 * plugin.usePiety(); switchByName("infernal");
		 * plugin.invokeMenuAction("Wield", "Wield", id,
		 * MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		 * useSpecialAttack(); plugin.getPlayers().attackCurrentPlayer(); } }
		 */
	}

	public void useRangeSpec() {
		/*
		 * final ItemContainer e =
		 * plugin.getClient().getItemContainer(InventoryID.INVENTORY); int id =
		 * plugin.getItems().getItem(21); int spec =
		 * plugin.getClient().getVar(VarPlayer.SPECIAL_ATTACK_PERCENT); if (spec
		 * >= 500) { if (plugin.onehandRangeWeapons.contains(id) ||
		 * plugin.twohandRangeWeapons.contains(id)) { plugin.useRigour();
		 * this.switchByName("arrow"); plugin.invokeMenuAction("Wield", "Wield",
		 * id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
		 * useSpecialAttack(); plugin.getPlayers().attackCurrentPlayer(); } }
		 */
	}

}
