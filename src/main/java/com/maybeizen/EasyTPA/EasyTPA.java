package com.maybeizen.EasyTPA;

import com.maybeizen.EasyTPA.commands.*;
import com.maybeizen.EasyTPA.listener.PlayerDeathListener;
import com.maybeizen.EasyTPA.managers.CooldownManager;
import com.maybeizen.EasyTPA.managers.TPAManager;
import com.maybeizen.EasyTPA.managers.ToggleManager;
import com.maybeizen.EasyTPA.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyTPA extends JavaPlugin {
	private ConfigManager configManager;
	private DatabaseManager databaseManager;
	private TPAManager tpaManager;
	private CooldownManager cooldownManager;
	private ToggleManager toggleManager;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.configManager = ConfigManager.getInstance(this);
		this.databaseManager = new DatabaseManager(this);

		runDataMigration();

		MessageUtils.initialize(this);

		this.tpaManager = new TPAManager(this);
		this.cooldownManager = new CooldownManager(configManager.getCooldown());
		this.toggleManager = new ToggleManager(this);

		getCommand("tpa").setExecutor(new TPACommand(this));
		getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
		getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
		getCommand("tptoggle").setExecutor(new TPToggleCommand(this));

		EasyTPACommand adminCommand = new EasyTPACommand(this);
		getCommand("easytpa").setExecutor(adminCommand);
		getCommand("easytpa").setTabCompleter(adminCommand);

		registerListeners();

		Bukkit.getLogger().info("EasyTPA has been enabled!");
		Bukkit.getLogger().info("Running on server version: " + VersionAdapter.getServerVersion());
	}

	@Override
	public void onDisable() {
		if (tpaManager != null) {
			tpaManager.clearAllRequests();
		}
		if (toggleManager != null) {
			toggleManager.cleanup();
		}
		if (databaseManager != null) {
			databaseManager.closeConnection();
		}
		Bukkit.getLogger().info("EasyTPA has been disabled!");
	}

	public void reloadConfiguration() {
		if (toggleManager != null) {
			toggleManager.saveData();
		}

		reloadConfig();

		configManager.reloadConfig();

		this.cooldownManager = new CooldownManager(configManager.getCooldown());

		if (toggleManager != null) {
			toggleManager.reload();
		} else {
			this.toggleManager = new ToggleManager(this);
		}

		MessageUtils.initialize(this);

	}

	private void runDataMigration() {
		DataMigrationUtil migrationUtil = new DataMigrationUtil(this);
		int migratedEntries = migrationUtil.migrateToggleData();

		if (migratedEntries > 0) {
			getLogger().info("Data migration complete: " + migratedEntries + " entries migrated to SQLite database");
		}
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public TPAManager getTPAManager() {
		return tpaManager;
	}

	public CooldownManager getCooldownManager() {
		return cooldownManager;
	}

	public ToggleManager getToggleManager() {
		return toggleManager;
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new PlayerDeathListener(tpaManager), this);
	}
}

