package hu.montlikadani.tablist.bukkit.tablist;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.tablist.bukkit.TabList;

public class TabManager {

	public static final Map<UUID, Boolean> TABENABLED = new HashMap<>();

	private TabList plugin;

	private final Set<TabHandler> tabPlayers = new HashSet<>();

	public TabManager(TabList plugin) {
		this.plugin = plugin;
	}

	public Set<TabHandler> getTabPlayers() {
		return tabPlayers;
	}

	public void addPlayer(Player p) {
		if (isPlayerInTab(p)) {
			return;
		}

		TabHandler tabHandler = new TabHandler(plugin, p);
		tabHandler.updateTab();
		tabPlayers.add(tabHandler);
	}

	public void removePlayer(Player player) {
		getPlayerTab(player).ifPresent(tabHandler -> {
			tabHandler.unregisterTab();
			tabPlayers.remove(tabHandler);
		});
	}

	public void removePlayer() {
		tabPlayers.forEach(pl -> {
			pl.unregisterTab();

			if (pl.getTask() != null) {
				plugin.getServer().getScheduler().cancelTask(pl.getTask().getTaskId());
			}
		});

		tabPlayers.clear();
	}

	public boolean isPlayerInTab(Player player) {
		return getPlayerTab(player).isPresent();
	}

	public Optional<TabHandler> getPlayerTab(final Player player) {
		return Optional
				.ofNullable(tabPlayers.stream().filter(tab -> tab.getPlayer().equals(player)).findFirst().orElse(null));
	}

	public void loadToggledTabs() {
		if (plugin.getTabC() == null || !plugin.getTabC().getBoolean("remember-toggled-tablist-to-file", true)) {
			return;
		}

		TABENABLED.clear();

		File f = new File(plugin.getFolder(), "toggledtablists.yml");
		if (!f.exists()) {
			return;
		}

		FileConfiguration t = YamlConfiguration.loadConfiguration(f);
		if (!t.contains("tablists")) {
			return;
		}

		for (String uuid : t.getConfigurationSection("tablists").getKeys(false)) {
			TABENABLED.put(UUID.fromString(uuid), t.getConfigurationSection("tablists").getBoolean(uuid));
		}

		t.set("tablists", null);
		try {
			t.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveToggledTabs() {
		File f = new File(plugin.getFolder(), "toggledtablists.yml");
		if (plugin.getTabC() == null || !plugin.getTabC().getBoolean("remember-toggled-tablist-to-file", true)) {
			if (f.exists()) {
				f.delete();
			}

			return;
		}

		if (TABENABLED.isEmpty()) {
			return;
		}

		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FileConfiguration t = YamlConfiguration.loadConfiguration(f);
		t.set("tablists", null);

		for (Entry<UUID, Boolean> list : TABENABLED.entrySet()) {
			if (list.getValue()) {
				t.set("tablists." + list.getKey(), list.getValue());
			}
		}

		try {
			t.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		TABENABLED.clear();
	}
}
