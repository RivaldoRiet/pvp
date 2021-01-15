package net.runelite.client.plugins.pvp;

import lombok.Getter;
import net.runelite.api.Player;

/**
 * Contains a player object When they attacked me And (in milliseconds) when to
 * expire the overlay around them
 */

@Getter
public class PlayerContainer {

	private final Player player;
	private final long whenTheyAttackedMe;
	private final int millisToExpireHighlight;

	public PlayerContainer(final Player player, final long whenTheyAttackedMe, final int millisToExpireHighlight) {
		this.player = player;
		this.whenTheyAttackedMe = whenTheyAttackedMe;
		this.millisToExpireHighlight = millisToExpireHighlight;
	}

}
