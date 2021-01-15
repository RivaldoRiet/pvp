package net.runelite.client.plugins.pvp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;

import javax.inject.Inject;
import javax.inject.Singleton;

import api.Tasks;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.VarPlayer;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.robot.utils.ScriptUtils;

@Singleton
class PvpPlayerOverlay extends Overlay {

	private final PvpPlugin plugin;
	private final PvpConfig config;
	private final Client client;
	private int lastId = 0;
	private int lastWep = 0;

	@Inject
	private PvpPlayerOverlay(final PvpPlugin plugin, final PvpConfig config, final Client client) {
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D g) {
		readPrayer();
		renderPotentialPlayers(g);
		renderAttackingPlayers(g);
		g.drawString("Debug", 7, 245);
		g.drawString("Isreachable: " + plugin.getPlayers().isOpponentReachable() + " - frozen: " + plugin.isFrozen()
				+ " - distance: " + plugin.getPlayers().enemyDistance(), 7, 260);
		// handleSpec();
		return null;
	}

	private void renderPotentialPlayers(Graphics2D graphics) {
		if (plugin.getPlayers().getPotentialPlayersAttackingMe() == null
				|| !plugin.getPlayers().getPotentialPlayersAttackingMe().isEmpty()) {
			try {
				if (plugin.getPlayers().getPotentialPlayersAttackingMe() != null) {
					for (PlayerContainer container : plugin.getPlayers().getPotentialPlayersAttackingMe()) {
						if ((System.currentTimeMillis() > (container.getWhenTheyAttackedMe()
								+ container.getMillisToExpireHighlight()))
								&& (container.getPlayer().getInteracting() != client.getLocalPlayer())) {
							plugin.getPlayers().removePlayerFromPotentialContainer(container);
						}
						if (config.drawPotentialTargetsName()) {
							renderNameAboveHead(graphics, container.getPlayer(), config.potentialPlayerColor());
						}
						if (config.drawPotentialTargetHighlight()) {
							renderHighlightedPlayer(graphics, container.getPlayer(), config.potentialPlayerColor());
						}
						if (config.drawPotentialTargetTile()) {
							// renderTileUnderPlayer(graphics,
							// container.getPlayer(),
							// config.potentialPlayerColor());
						}
						if (config.drawPotentialTargetPrayAgainst()) {
							if (plugin.getPlayers().getPlayersAttackingMe() == null
									|| plugin.getPlayers().getPlayersAttackingMe().isEmpty()) {
								renderPrayAgainstOnPlayer(graphics, container.getPlayer(), config.potentialPlayerColor());
							}
						}
					}
				}
			} catch (ConcurrentModificationException ignored) {
			}
		}
	}

	private void renderAttackingPlayers(Graphics2D graphics) {
		if (plugin.getPlayers().getPlayersAttackingMe() == null || !plugin.getPlayers().getPlayersAttackingMe().isEmpty()) {
			try {
				if (plugin.getPlayers().getPlayersAttackingMe() != null) {
					for (PlayerContainer container : plugin.getPlayers().getPlayersAttackingMe()) {
						if ((System.currentTimeMillis() > (container.getWhenTheyAttackedMe()
								+ container.getMillisToExpireHighlight()))
								&& (container.getPlayer().getInteracting() != client.getLocalPlayer())) {
							plugin.getPlayers().removePlayerFromAttackerContainer(container);
						}

						if (config.drawTargetsName()) {
							renderNameAboveHead(graphics, container.getPlayer(), config.attackerPlayerColor());
						}
						if (config.drawTargetHighlight()) {
							renderHighlightedPlayer(graphics, container.getPlayer(), config.attackerPlayerColor());
						}
						if (config.drawTargetTile()) {
							// renderTileUnderPlayer(graphics,
							// container.getPlayer(),
							// config.attackerPlayerColor());
						}
						if (config.drawTargetPrayAgainst()) {
							renderPrayAgainstOnPlayer(graphics, container.getPlayer(), config.attackerPlayerColor());
						}
					}
				}
			} catch (ConcurrentModificationException ignored) {
			}
		}
	}

	private void renderNameAboveHead(Graphics2D graphics, Player player, Color color) {
		final String name = ScriptUtils.stripHtml(player.getName());
		final int offset = player.getLogicalHeight() + 40;
		Point textLocation = player.getCanvasTextLocation(graphics, name, offset);
		if (textLocation != null) {
			OverlayUtil.renderTextLocation(graphics, textLocation, name, color);
		}
	}

	private void renderHighlightedPlayer(Graphics2D graphics, Player player, Color color) {
		try {
			OverlayUtil.renderPolygon(graphics, player.getConvexHull(), color);
		} catch (NullPointerException ignored) {
		}
	}

	private void renderTileUnderPlayer(Graphics2D graphics, Player player, Color color) {
		Polygon poly = player.getCanvasTilePoly();
		OverlayUtil.renderPolygon(graphics, poly, color);
	}

	private Player getCurrPlayer() {
		if (plugin.getPlayers().getPlayersAttackingMe() != null && plugin.getPlayers().getPlayersAttackingMe().size() > 0) {
			return plugin.getPlayers().getPlayersAttackingMe().get(0).getPlayer();
		}
		if (plugin.getPlayers().getPlayersAttackingMe() != null && plugin.getPlayers().getPlayersAttackingMe().size() <= 0
				&& plugin.getPlayers().getPotentialPlayersAttackingMe() != null
				&& plugin.getPlayers().getPotentialPlayersAttackingMe().size() > 0) {
			return plugin.getPlayers().getPotentialPlayersAttackingMe().get(0).getPlayer();
		}
		return null;
	}

	private void readPrayer() {
		Player player = getCurrPlayer();
		if (player != null) {
			switch (WeaponType.checkWeaponOnPlayer(client, player)) {
				case WEAPON_MELEE:
					if (!client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
						handlePray(1);
					}
					break;
				case WEAPON_MAGIC:
					if (!client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
						handlePray(2);
					}
					break;
				case WEAPON_RANGED:
					if (!client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)) {
						handlePray(3);
					}
					break;
				default:
					break;
			}
		}
	}

	private void handlePray(int id) {
		if (id == 1 && lastId != id) {
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MELEE);
			//plugin.invokeMenuAction("Activate", "<col=ff9040>Protect from Melee</col>", 1, MenuAction.CC_OP.getId(), -1,
			//		WidgetInfo.PRAYER_PROTECT_FROM_MELEE.getId());
			lastId = id;
		}

		if (id == 2 && lastId != id) {
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
			//plugin.invokeMenuAction("Activate", "<col=ff9040>Protect from Magic</col>", 1, MenuAction.CC_OP.getId(), -1,
			//		WidgetInfo.PRAYER_PROTECT_FROM_MAGIC.getId());
			lastId = id;
		}

		if (id == 3 && lastId != id) {
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MISSILES);
			//plugin.invokeMenuAction("Activate", "<col=ff9040>Protect from Missiles</col>", 1, MenuAction.CC_OP.getId(), -1,
			//		WidgetInfo.PRAYER_PROTECT_FROM_MISSILES.getId());
			lastId = id;
		}
	}

	private void renderPrayAgainstOnPlayer(Graphics2D graphics, Player player, Color color) {
		final int offset = (player.getLogicalHeight() / 2) + 75;
		BufferedImage icon;

		switch (WeaponType.checkWeaponOnPlayer(client, player)) {
			case WEAPON_MELEE:
				// handlePray(1);
				icon = plugin.getProtectionIcon(WeaponType.WEAPON_MELEE);
				graphics.setColor(Color.RED);
				graphics.fillRect(0, 230, 15, 15);
				break;
			case WEAPON_MAGIC:
				// handlePray(2);
				icon = plugin.getProtectionIcon(WeaponType.WEAPON_MAGIC);
				graphics.setColor(Color.BLUE);
				graphics.fillRect(0, 230, 15, 15);
				break;
			case WEAPON_RANGED:
				// handlePray(3);
				icon = plugin.getProtectionIcon(WeaponType.WEAPON_RANGED);
				graphics.setColor(Color.GREEN);
				graphics.fillRect(0, 230, 15, 15);
				break;
			default:
				icon = null;
				break;
		}
		try {
			if (icon != null) {
				Point point = player.getCanvasImageLocation(icon, offset);
				OverlayUtil.renderImageLocation(graphics, point, icon);
			} else {
				if (config.drawUnknownWeapons()) {
					int itemId = player.getPlayerComposition().getEquipmentId(KitType.WEAPON);
					ItemComposition itemComposition = client.getItemDefinition(itemId);

					final String str = itemComposition.getName().toUpperCase();
					Point point = player.getCanvasTextLocation(graphics, str, offset);
					OverlayUtil.renderTextLocation(graphics, point, str, color);
				}
			}
		} catch (Exception ignored) {
		}
	}

	private void handleSpec() {
		final ItemContainer ic = client.getItemContainer(InventoryID.EQUIPMENT);
		int weaponId = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
		final String name = client.getItemDefinition(weaponId).getName().toLowerCase();
		if (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && name.contains("godsword") || name.contains("claws")
				|| name.contains("dagger") || name.contains("javelin") || name.contains("longsword")) {
			if (lastWep != weaponId) {
				click(717, 4);
				plugin.invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, MenuAction.CC_OP.getId(), -1, 38862884);
				click(717, 4);
			}
		}
		lastWep = weaponId;
	}

	private void click(int x, int y) {
		// MouseEvent mousePressed = new MouseEvent(client.getCanvas(), 501,
		// System.currentTimeMillis(), 0, (int) (x), (int) (y), 1, false, 1);
		// client.getCanvas().dispatchEvent(mousePressed);

		MouseEvent mouseReleased = new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, (int) (x), (int) (y), 1,
				false, 1);
		client.getCanvas().dispatchEvent(mouseReleased);

		// MouseEvent mouseClicked = new MouseEvent(client.getCanvas(), 500,
		// System.currentTimeMillis(), 0, (int) (x), (int) (y), 1, false, 1);
		// client.getCanvas().dispatchEvent(mouseClicked);
	}

}
