package net.runelite.client.plugins.pvp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provides;

import api.Tasks;
import api.Variables;
import api.threads.PrayerObserver;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.SpriteID;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.FriendChatManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.pvp.api.Hotkeys;
import net.runelite.client.plugins.pvp.api.ItemGetter;
import net.runelite.client.plugins.pvp.api.PlayerGetter;
import net.runelite.client.plugins.pvp.api.Switches;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.robot.api.ClientContext;

@PluginDescriptor(name = "Pvp", description = "Use plugin in PvP situations for best results!!", tags = { "highlight", "pvp",
		"overlay", "players" }, enabledByDefault = false)

@Singleton
public class PvpPlugin extends Plugin {

	private static final int[] PROTECTION_ICONS = { SpriteID.PRAYER_PROTECT_FROM_MISSILES, SpriteID.PRAYER_PROTECT_FROM_MELEE,
			SpriteID.PRAYER_PROTECT_FROM_MAGIC };
	private static final Dimension PROTECTION_ICON_DIMENSION = new Dimension(33, 33);
	private static final Color PROTECTION_ICON_OUTLINE_COLOR = new Color(33, 33, 33);
	private final BufferedImage[] ProtectionIcons = new BufferedImage[PROTECTION_ICONS.length];

	private long lastFreeze = 0;

	@Inject
	@Getter
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PvpPlayerOverlay overlay;

	@Inject
	private PvpOverlay overlayPrayerTab;

	@Inject
	@Getter
	private ItemManager itemManager;
	@Inject
	@Getter
	private PvpConfig config;

	@Inject
	private FriendChatManager friendChatManager;

	@Inject
	private KeyManager keyManager;

	@Getter
	private ItemGetter items;
	@Getter
	private PlayerGetter players;
	@Getter
	private Switches switchs;
	@Getter
	private Hotkeys hotkeys;
	
	private PrayerObserver prayerObserver = null;
	private int lastId = 0;
	// @Inject
	// private BotUtils utils;

	@Provides
	PvpConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(PvpConfig.class);
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked m) {
		System.out.println(m.toString());

/*		System.out.println("Cached amount: " + client.getCachedPlayers().length);
		for (Player p : client.getCachedPlayers()) {
			if (p != null) {
				System.out.println("index: " + getIndex(p));
			}
		} */
	}
	/*
	private int getIndex(Player p)
	{
		for (int i = 0; i < client.getCachedPlayers().length; i++) {
			if (client.getCachedPlayers()[i] != null && client.getCachedPlayers()[i].equals(p)) {
				return i;
			}
		}
		return -1;
	}*/

	
	@Subscribe
	private void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			loadProtectionIcons();
			Arrays.stream(hotkeys.get()).forEach(keyManager::registerKeyListener);
			return;
		}
		Arrays.stream(hotkeys.get()).forEach(keyManager::unregisterKeyListener);
	}

	Method action;
	Class<?> _class;

	@Override
	protected void startUp() {
		try {
			_class = Class.forName("client");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		action = Arrays.stream(_class.getMethods()).filter(v -> v.toString().contains("menuAction")).findFirst().orElse(null);
		action.setAccessible(true);

		items = new ItemGetter(this);
		players = new PlayerGetter(this);
		switchs = new Switches(this);
		hotkeys = new Hotkeys(this, itemManager, players);

		overlayManager.add(overlay);
		overlayManager.add(overlayPrayerTab);

		if (client.getGameState() == GameState.LOGGED_IN) {
			Arrays.stream(hotkeys.get()).forEach(keyManager::registerKeyListener);
		}
		
		Tasks.init(ClientContext.instance());
		prayerObserver = new PrayerObserver(ClientContext.instance(), new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return Variables.USE_PRAYER;
			}
		});
		prayerObserver.start();
		Variables.USE_PRAYER = true;
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
		overlayManager.remove(overlayPrayerTab);
		Arrays.stream(hotkeys.get()).forEach(keyManager::unregisterKeyListener);
		Variables.USE_PRAYER = false;
	}

	public void invokeMenuAction(String op, String target, int action, int menuAction, int invSlot, int widgetId) {

		MenuOptionClicked option = new MenuOptionClicked();
		option.setActionParam(invSlot);
		option.setMenuOption(op);
		option.setMenuTarget(target);
		option.setMenuAction(MenuAction.of(menuAction));
		option.setId(action);
		option.setWidgetId(widgetId);
		invoke(option);
	}

	public void invoke(MenuOptionClicked option) {
		try {
			action.invoke(_class.newInstance(), option.getActionParam(), option.getWidgetId(), option.getMenuAction().getId(),
					option.getId(), option.getMenuOption(), option.getMenuTarget(), -1, -1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private Player getCurrPlayer() {
		if (getPlayers().getPlayersAttackingMe() != null && getPlayers().getPlayersAttackingMe().size() > 0) {
			return getPlayers().getPlayersAttackingMe().get(0).getPlayer();
		}
		if (getPlayers().getPlayersAttackingMe() != null && getPlayers().getPlayersAttackingMe().size() <= 0
				&& getPlayers().getPotentialPlayersAttackingMe() != null
				&& getPlayers().getPotentialPlayersAttackingMe().size() > 0) {
			return getPlayers().getPotentialPlayersAttackingMe().get(0).getPlayer();
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
			lastId = id;
		}

		if (id == 2 && lastId != id) {
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
			lastId = id;
		}

		if (id == 3 && lastId != id) {
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MISSILES);
			lastId = id;
		}
	}
	
	@Subscribe
	private void onGameTick(GameTick Event) {
		readPrayer();
	}
	
	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getMessage().equals("<col=ef1020>You have been frozen!</col>")) {
			lastFreeze = System.currentTimeMillis();
		}
	}

	// assumes ice barrage
	public boolean isFrozen() {
		if (System.currentTimeMillis() - lastFreeze < 20000) { return true; }

		return false;
	}
	


	public void castBarrage() {
		clientThread.invoke(() -> {
			invokeMenuAction("Cast", "<col=00ff00>Ice Barrage</col>", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286926);
		});
	}
	@Subscribe
	private void onAnimationChanged(AnimationChanged animationChanged) throws ClassCastException {
		// newly added 
		try {
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getInteracting() != null) {
			if (client.getLocalPlayer().getInteracting() != null) {
				Player sourcePlayer = (Player) client.getLocalPlayer().getInteracting();
				Player p = players.getPlayerByName(sourcePlayer.getName());
				if (p != null && players.findPlayerInAttackerList(p) != null) {
					players.resetPlayerFromAttackerContainerTimer(players.findPlayerInAttackerList(p));
				}
				if (p != null && !players.potentialPlayersAttackingMe.isEmpty()
						&& players.potentialPlayersAttackingMe.contains(players.findPlayerInPotentialList(p))) {
					players.removePlayerFromPotentialContainer(players.findPlayerInPotentialList(p));
				}
				if (p != null && players.findPlayerInAttackerList(p) == null) {
					// playersAttackingMe.Clear();
					players.playersAttackingMe = new ArrayList<>();
					PlayerContainer container = new PlayerContainer(p, System.currentTimeMillis(),
							(config.attackerTargetTimeout() * 1000));
					players.playersAttackingMe.add(container);
				}
			}
		}
		if (animationChanged.getActor() != null) {
			@SuppressWarnings("unchecked")  Player sourcePlayer = (Player) animationChanged.getActor();
			/*
			 * animation 4230 = crossbow 1979 = barrage 1658 = whip 8145 =
			 * rapier 1062 = dds 7514 - dclaws 7218 - ballista 7515 - vls
			*/
			if (animationChanged.getActor() != null && client.getLocalPlayer().equals(animationChanged.getActor())
					&& !isFrozen()) {
				if (sourcePlayer.getAnimation() == 4230 || sourcePlayer.getAnimation() == 1979
						|| sourcePlayer.getAnimation() == 1658 || sourcePlayer.getAnimation() == 8145
						|| sourcePlayer.getAnimation() == 1062 || sourcePlayer.getAnimation() == 7514
						|| sourcePlayer.getAnimation() == 7644 || sourcePlayer.getAnimation() == 7218
						|| sourcePlayer.getAnimation() == 7515) {
					// attack animation
					// walk();
				}
			}

			if ((animationChanged.getActor() instanceof Player)
					&& (animationChanged.getActor().getInteracting() instanceof Player)
					&& (animationChanged.getActor().getInteracting() == client.getLocalPlayer())) {
				sourcePlayer = (Player) animationChanged.getActor();

				// is the client is a friend/clan and the config is set to
				// ignore friends/clan dont add them to list
				if (client.isFriended(sourcePlayer.getName(), true) && config.ignoreFriends()) { return; }
				if (friendChatManager.isMember(sourcePlayer.getName()) && config.ignoreClanMates()) { return; }

				if ((sourcePlayer.getAnimation() != -1) && (!isBlockAnimation(sourcePlayer.getAnimation()))) {

					// if attacker attacks again, reset his timer so overlay
					// doesn't go away
					if (players.findPlayerInAttackerList(sourcePlayer) != null) {
						players.resetPlayerFromAttackerContainerTimer(players.findPlayerInAttackerList(sourcePlayer));
					}
					// if he attacks and he was in the potential attackers list,
					// remove him
					if (!players.potentialPlayersAttackingMe.isEmpty()
							&& players.potentialPlayersAttackingMe.contains(players.findPlayerInPotentialList(sourcePlayer))) {
						players.removePlayerFromPotentialContainer(players.findPlayerInPotentialList(sourcePlayer));
					}
					// if he's not in the attackers list, add him
					if (players.findPlayerInAttackerList(sourcePlayer) == null) {
						PlayerContainer container = new PlayerContainer(sourcePlayer, System.currentTimeMillis(),
								(config.attackerTargetTimeout() * 1000));
						players.playersAttackingMe.add(container);
					}
				}
			}
		}
		
		} catch (ClassCastException exc) {
			
		}
	}

	@Subscribe
	private void onInteractingChanged(InteractingChanged interactingChanged) {
		// if someone interacts with you, add them to the potential attackers
		// list
		if ((interactingChanged.getSource() instanceof Player) && (interactingChanged.getTarget() instanceof Player)) {
			Player sourcePlayer = (Player) interactingChanged.getSource();
			Player targetPlayer = (Player) interactingChanged.getTarget();
			if ((targetPlayer == client.getLocalPlayer()) && (players.findPlayerInPotentialList(sourcePlayer) == null)) { // we're
				// being
				// interacted
				// with

				// is the client is a friend/clan and the config is set to
				// ignore friends/clan dont add them to list
				if (client.isFriended(sourcePlayer.getName(), true) && config.ignoreFriends()) { return; }
				if (friendChatManager.isMember(sourcePlayer.getName()) && config.ignoreClanMates()) { return; }

				PlayerContainer container = new PlayerContainer(sourcePlayer, System.currentTimeMillis(),
						(config.potentialTargetTimeout() * 1000));
				if (players.potentialPlayersAttackingMe.size() == 0) {
					players.potentialPlayersAttackingMe.add(container);
				}
			}
		}
	}

	@Subscribe
	private void onPlayerDespawned(PlayerDespawned playerDespawned) {
		PlayerContainer container = players.findPlayerInAttackerList(playerDespawned.getPlayer());
		PlayerContainer container2 = players.findPlayerInPotentialList(playerDespawned.getPlayer());
		if (container != null) {
			players.playersAttackingMe.remove(container);
		}
		if (container2 != null) {
			players.potentialPlayersAttackingMe.remove(container2);
		}
	}

	@Subscribe
	private void onPlayerSpawned(PlayerSpawned playerSpawned) {
		if (config.markNewPlayer()) {
			Player p = playerSpawned.getPlayer();

			if (client.isFriended(p.getName(), true) && config.ignoreFriends()) { return; }
			if (friendChatManager.isMember(p.getName()) && config.ignoreClanMates()) { return; }

			PlayerContainer container = players.findPlayerInPotentialList(p);
			if (container == null) {
				container = new PlayerContainer(p, System.currentTimeMillis(), (config.newSpawnTimeout() * 1000));
				players.potentialPlayersAttackingMe.add(container);
			}
		}
	}

	@Subscribe
	private void onStatChanged(StatChanged c) {
		// walking on player
		// client.getLogger().debug("does this even go off?");
		// walk();
	}

	private boolean isBlockAnimation(int anim) {
		switch (anim) {
			case 4177:
			case 420:
			case 1156:
			case 388:
			case 424:
				return true;
			default:
				return false;
		}
	}

	private void loadProtectionIcons() {
		for (int i = 0; i < PROTECTION_ICONS.length; i++) {
			final int resource = PROTECTION_ICONS[i];
			ProtectionIcons[i] = rgbaToIndexedBufferedImage(ProtectionIconFromSprite(spriteManager.getSprite(resource, 0)));
		}
	}

	private static BufferedImage rgbaToIndexedBufferedImage(final BufferedImage sourceBufferedImage) {
		final BufferedImage indexedImage = new BufferedImage(sourceBufferedImage.getWidth(), sourceBufferedImage.getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED);

		final ColorModel cm = indexedImage.getColorModel();
		final IndexColorModel icm = (IndexColorModel) cm;

		final int size = icm.getMapSize();
		final byte[] reds = new byte[size];
		final byte[] greens = new byte[size];
		final byte[] blues = new byte[size];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);

		final WritableRaster raster = indexedImage.getRaster();
		final int pixel = raster.getSample(0, 0, 0);
		final IndexColorModel resultIcm = new IndexColorModel(8, size, reds, greens, blues, pixel);
		final BufferedImage resultIndexedImage = new BufferedImage(resultIcm, raster, sourceBufferedImage.isAlphaPremultiplied(),
				null);
		resultIndexedImage.getGraphics().drawImage(sourceBufferedImage, 0, 0, null);
		return resultIndexedImage;
	}

	private static BufferedImage ProtectionIconFromSprite(final BufferedImage freezeSprite) {
		final BufferedImage freezeCanvas = ImageUtil.resizeCanvas(freezeSprite, PROTECTION_ICON_DIMENSION.width,
				PROTECTION_ICON_DIMENSION.height);
		return ImageUtil.outlineImage(freezeCanvas, PROTECTION_ICON_OUTLINE_COLOR);
	}

	BufferedImage getProtectionIcon(WeaponType weaponType) {
		switch (weaponType) {
			case WEAPON_RANGED:
				return ProtectionIcons[0];
			case WEAPON_MELEE:
				return ProtectionIcons[1];
			case WEAPON_MAGIC:
				return ProtectionIcons[2];
		}
		return null;
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("prayagainstplayer")) { return; }
	}
}