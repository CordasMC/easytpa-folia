package com.maybeizen.EasyTPA.utils;

import com.maybeizen.EasyTPA.EasyTPA;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;


public class DatabaseManager {
	private static final String CREATE_TOGGLE_TABLE =
			"CREATE TABLE IF NOT EXISTS toggle_states (" +
					"uuid VARCHAR(36) PRIMARY KEY, " +
					"enabled BOOLEAN NOT NULL DEFAULT 1)";
	private static final String INSERT_TOGGLE =
			"INSERT OR REPLACE INTO toggle_states (uuid, enabled) VALUES (?, ?)";
	private static final String SELECT_ALL_TOGGLES =
			"SELECT uuid, enabled FROM toggle_states";
	private static final String SELECT_TOGGLE =
			"SELECT enabled FROM toggle_states WHERE uuid = ?";
	private final EasyTPA plugin;
	private final String dbFile;
	private Connection connection;

	public DatabaseManager(EasyTPA plugin) {
		this.plugin = plugin;
		this.dbFile = new File(plugin.getDataFolder(), "easytpa.db").getAbsolutePath();

		initialize();
	}

	private synchronized void initialize() {
		try {
			if (!plugin.getDataFolder().exists()) {
				plugin.getDataFolder().mkdirs();
			}

			Class.forName("org.sqlite.JDBC");

			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);

			try (Statement statement = connection.createStatement()) {
				statement.execute(CREATE_TOGGLE_TABLE);
			}

			plugin.getLogger().info("Database connection established successfully");
		} catch (ClassNotFoundException e) {
			plugin.getLogger().log(Level.SEVERE, "SQLite JDBC driver not found", e);
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not connect to SQLite database", e);
		}
	}

	public synchronized void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				plugin.getLogger().info("Database connection closed");
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Error closing database connection", e);
		}
	}

	public synchronized void saveToggleState(UUID uuid, boolean enabled) {
		try (PreparedStatement statement = connection.prepareStatement(INSERT_TOGGLE)) {
			statement.setString(1, uuid.toString());
			statement.setBoolean(2, enabled);
			statement.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Error saving toggle state for " + uuid, e);
		}
	}

	public synchronized Map<UUID, Boolean> loadAllToggleStates() {
		Map<UUID, Boolean> toggleStates = new HashMap<>();

		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(SELECT_ALL_TOGGLES)) {

			while (resultSet.next()) {
				try {
					UUID uuid = UUID.fromString(resultSet.getString("uuid"));
					boolean enabled = resultSet.getBoolean("enabled");
					toggleStates.put(uuid, enabled);
				} catch (IllegalArgumentException e) {
					plugin.getLogger().warning("Invalid UUID in database: " + resultSet.getString("uuid"));
				}
			}

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Error loading toggle states", e);
		}

		return toggleStates;
	}

	public synchronized boolean getToggleState(UUID uuid) {
		try (PreparedStatement statement = connection.prepareStatement(SELECT_TOGGLE)) {
			statement.setString(1, uuid.toString());

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getBoolean("enabled");
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Error getting toggle state for " + uuid, e);
		}

		return true;
	}

	public synchronized boolean isConnectionValid() {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	public synchronized void reconnectIfNeeded() {
		try {
			if (connection == null || connection.isClosed()) {
				initialize();
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Error checking database connection", e);
			initialize();
		}
	}
}
