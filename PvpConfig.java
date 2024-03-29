
package net.runelite.client.plugins.pvp;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("pvp")
public interface PvpConfig extends Config {
	@ConfigItem(position = 0, keyName = "Use manual mode", name = "Use manual mode", description = "This will switch f10 and f3")
	default boolean useManual() {
		return true;
	}

	@ConfigItem(position = 0, keyName = "attackerPlayerColor", name = "Attacker color", description = "This is the color that will be used to highlight attackers.")
	default Color attackerPlayerColor() {
		return new Color(0xFF0006);
	}

	@ConfigItem(position = 1, keyName = "potentialPlayerColor", name = "Potential Attacker color", description = "This is the color that will be used to highlight potential attackers.")
	default Color potentialPlayerColor() {
		return new Color(0xFFFF00);
	}

	@ConfigItem(position = 2, keyName = "attackerTargetTimeout", name = "Attacker Timeout", description = "Seconds until attacker is no longer highlighted.")
	default int attackerTargetTimeout() {
		return 10;
	}

	@ConfigItem(position = 3, keyName = "potentialTargetTimeout", name = "Potential Attacker Timeout", description = "Seconds until potential attacker is no longer highlighted.")
	default int potentialTargetTimeout() {
		return 10;
	}

	@ConfigItem(position = 4, keyName = "newSpawnTimeout", name = "New Player Timeout", description = "Seconds until logged in/spawned player is no longer highlighted.")
	default int newSpawnTimeout() {
		return 5;
	}

	@ConfigItem(position = 5, keyName = "ignoreFriends", name = "Ignore Friends", description = "This lets you decide whether you want friends to be highlighted by this plugin.")
	default boolean ignoreFriends() {
		return true;
	}

	@ConfigItem(position = 6, keyName = "ignoreClanMates", name = "Ignore Clan Mates", description = "This lets you decide whether you want clan mates to be highlighted by this plugin.")
	default boolean ignoreClanMates() {
		return true;
	}

	@ConfigItem(position = 7, keyName = "markNewPlayer", name = "Mark new player as potential attacker", description = "Marks someone that logged in or teleported as a potential attacker for your safety\nDO NOT RUN THIS IN WORLD 1-2 GRAND EXCHANGE!")
	default boolean markNewPlayer() {
		return false;
	}

	@ConfigItem(position = 8, keyName = "drawTargetPrayAgainst", name = "Draw what to pray on attacker", description = "Tells you what to pray from what weapon the attacker is holding")
	default boolean drawTargetPrayAgainst() {
		return true;
	}

	@ConfigItem(position = 9, keyName = "drawPotentialTargetPrayAgainst", name = "Draw what to pray on potential attacker", description = "Tells you what to pray from what weapon the potential attacker is holding")
	default boolean drawPotentialTargetPrayAgainst() {
		return true;
	}

	@ConfigItem(position = 10, keyName = "drawTargetPrayAgainstPrayerTab", name = "Draw what to pray from prayer tab", description = "Tells you what to pray from what weapon the attacker is holding from the prayer tab")
	default boolean drawTargetPrayAgainstPrayerTab() {
		return false;
	}

	@ConfigItem(position = 11, keyName = "drawTargetsName", name = "Draw name on attacker", description = "Configures whether or not the attacker\'s name should be shown")
	default boolean drawTargetsName() {
		return true;
	}

	@ConfigItem(position = 12, keyName = "drawPotentialTargetsName", name = "Draw name on potential attacker", description = "Configures whether or not the potential attacker\'s name should be shown")
	default boolean drawPotentialTargetsName() {
		return true;
	}

	@ConfigItem(position = 13, keyName = "drawTargetHighlight", name = "Draw highlight around attacker", description = "Configures whether or not the attacker should be highlighted")
	default boolean drawTargetHighlight() {
		return true;
	}

	@ConfigItem(position = 14, keyName = "drawPotentialTargetHighlight", name = "Draw highlight around potential attacker", description = "Configures whether or not the potential attacker should be highlighted")
	default boolean drawPotentialTargetHighlight() {
		return true;
	}

	@ConfigItem(position = 15, keyName = "drawTargetTile", name = "Draw tile under attacker", description = "Configures whether or not the attacker\'s tile be highlighted")
	default boolean drawTargetTile() {
		return false;
	}

	@ConfigItem(position = 16, keyName = "drawPotentialTargetTile", name = "Draw tile under potential attacker", description = "Configures whether or not the potential attacker\'s tile be highlighted")
	default boolean drawPotentialTargetTile() {
		return false;
	}

	@ConfigItem(position = 17, keyName = "drawUnknownWeapons", name = "Draw unknown weapons", description = "Configures whether or not the unknown weapons should be shown when a player equips one")
	default boolean drawUnknownWeapons() {
		return false;
	}
}
