package net.runelite.client.plugins.pvp.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.pvp.PlayerContainer;
import net.runelite.client.plugins.pvp.PvpPlugin;

public class PlayerGetter {

	private final PvpPlugin plugin;
	private Client client;
	@Inject
	public PlayerGetter(PvpPlugin plugin) {
		this.plugin = plugin;
		this.client = plugin.getClient();
	}

	@Getter
	public List<PlayerContainer> potentialPlayersAttackingMe = new ArrayList<PlayerContainer>();
	@Getter
	public List<PlayerContainer> playersAttackingMe = new ArrayList<PlayerContainer>();

	public Player getPlayerByName(String name) {
		if (plugin.getClient().getLocalPlayer().getName().contains(name)) { return null; }
		for (Player p : plugin.getClient().getPlayers()) {
			if (p != null && p.getName() != null && p.getName().contains(name)) { return p; }
		}
		return null;
	}

	public PlayerContainer findPlayerInAttackerList(Player player) {
		if (playersAttackingMe.isEmpty()) { return null; }
		for (PlayerContainer container : playersAttackingMe) {
			if (container.getPlayer() == player) { return container; }
		}
		return null;
	}

	public PlayerContainer findPlayerInPotentialList(Player player) {
		if (potentialPlayersAttackingMe.isEmpty()) { return null; }
		for (PlayerContainer container : potentialPlayersAttackingMe) {
			if (container.getPlayer() == player) { return container; }
		}
		return null;
	}

	public void resetPlayerFromAttackerContainerTimer(PlayerContainer container) {
		removePlayerFromAttackerContainer(container);
		PlayerContainer newContainer = new PlayerContainer(container.getPlayer(), System.currentTimeMillis(),
				(plugin.getConfig().attackerTargetTimeout() * 1000));
		playersAttackingMe.add(newContainer);
	}

	public void removePlayerFromPotentialContainer(PlayerContainer container) {
		if ((potentialPlayersAttackingMe != null) && (!potentialPlayersAttackingMe.isEmpty())) {
			potentialPlayersAttackingMe.remove(container);
		}
	}

	public void removePlayerFromAttackerContainer(PlayerContainer container) {
		if ((playersAttackingMe != null) && (!playersAttackingMe.isEmpty())) {
			playersAttackingMe.remove(container);
		}
	}

	public void attackCurrentPlayerBarrage() {
		String name = "";
		int level = 0;
		int idx = 0;
		if (playersAttackingMe != null && playersAttackingMe.size() > 0) {
			name = playersAttackingMe.get(0).getPlayer().getName();
			level = playersAttackingMe.get(0).getPlayer().getCombatLevel();
			// idx = playersAttackingMe.get(0).getPlayer().getHash()t
		}
		if (playersAttackingMe != null && playersAttackingMe.size() <= 0 && potentialPlayersAttackingMe != null
				&& potentialPlayersAttackingMe.size() > 0) {
			name = potentialPlayersAttackingMe.get(0).getPlayer().getName();
			level = potentialPlayersAttackingMe.get(0).getPlayer().getCombatLevel();
			// idx =
			// potentialPlayersAttackingMe.get(0).getPlayer().getPlayerId();
		}
		if (level > 0) {
			plugin.invokeMenuAction("Cast",
					"<col=00ff00>Ice Barrage</col><col=ffffff> -> <col=ffffff>" + name + "<col=ff0000>  (level-" + level + ")",
					idx, MenuAction.SPELL_CAST_ON_PLAYER.getId(), 0, 0);
		}
	}

	public Player getCurrPlayer() {
		if (playersAttackingMe != null && playersAttackingMe.size() > 0) { return playersAttackingMe.get(0).getPlayer(); }
		if (playersAttackingMe != null && playersAttackingMe.size() <= 0 && potentialPlayersAttackingMe != null
				&& potentialPlayersAttackingMe.size() > 0) {
			return potentialPlayersAttackingMe.get(0).getPlayer();
		}
		return null;
	}

	public void attackCurrentPlayer() {
		String name = "";
		int level = 0;
		int idx = 0;
		if (playersAttackingMe != null && playersAttackingMe.size() > 0) {
			name = playersAttackingMe.get(0).getPlayer().getName();
			level = playersAttackingMe.get(0).getPlayer().getCombatLevel();
			idx = getIndex(playersAttackingMe.get(0).getPlayer());
		}
		if (playersAttackingMe != null && playersAttackingMe.size() <= 0 && potentialPlayersAttackingMe != null
				&& potentialPlayersAttackingMe.size() > 0) {
			name = potentialPlayersAttackingMe.get(0).getPlayer().getName();
			level = potentialPlayersAttackingMe.get(0).getPlayer().getCombatLevel();
			 idx = getIndex(potentialPlayersAttackingMe.get(0).getPlayer());
		}
		if (level > 0) {
			System.out.println("Attacking player: " + "<col=ffffff>" + name + "<col=ff00>  (level-" + level + " ------ idx: " + idx );
			plugin.invokeMenuAction("Fight", "<col=ffffff>" + name + "<col=ff00>  (level-" + level + ")", idx,
					MenuAction.PLAYER_FIRST_OPTION.getId(), 0, 0);
		}
	}
	
	private int getIndex(Player p)
	{
		for (int i = 0; i < client.getCachedPlayers().length; i++) {
			if (client.getCachedPlayers()[i] != null && client.getCachedPlayers()[i].equals(p)) {
				return i;
			}
		}
		return -1;
	}

	public int enemyDistance() {
		Player p = getCurrPlayer();
		if (p == null) {
			return 0;
		} else {
			return p.getWorldLocation().distanceTo(plugin.getClient().getLocalPlayer().getWorldLocation());
		}
	}

	public boolean isOpponentReachable() {
		Player p = getCurrPlayer();
		if (p == null) { return false; }

		WorldPoint enemy = p.getWorldLocation();
		WorldPoint local = plugin.getClient().getLocalPlayer().getWorldLocation();
		if (local.getX() + 1 == enemy.getX() && local.getY() + 1 == enemy.getY()) { return false; }
		if (local.getX() - 1 == enemy.getX() && local.getY() + 1 == enemy.getY()) { return false; }
		if (local.getX() + 1 == enemy.getX() && local.getY() - 1 == enemy.getY()) { return false; }
		if (local.getX() - 1 == enemy.getX() && local.getY() - 1 == enemy.getY()) { return false; }

		return p.getWorldLocation().distanceTo(plugin.getClient().getLocalPlayer().getWorldLocation()) < 2;
	}

}
