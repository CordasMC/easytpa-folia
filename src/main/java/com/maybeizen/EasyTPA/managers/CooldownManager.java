package com.maybeizen.EasyTPA.managers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CooldownManager {
	private final ConcurrentMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
	private final long cooldownTime; // Cooldown time in milliseconds

	public CooldownManager(long cooldownSeconds) {
		this.cooldownTime = cooldownSeconds * 1000;
	}

	public boolean hasCooldown(UUID playerId) {
		Long expire = cooldowns.get(playerId);
		if (expire == null) {
			return false;
		}
		long timeLeft = getRemainingTime(playerId);
		if (timeLeft <= 0) {
			cooldowns.remove(playerId);
			return false;
		}
		return true;
	}

	public long getRemainingTime(UUID playerId) {
		Long expire = cooldowns.get(playerId);
		return expire != null ? expire - System.currentTimeMillis() : 0;
	}

	public void setCooldown(UUID playerId) {
		cooldowns.put(playerId, System.currentTimeMillis() + cooldownTime);
	}

	public String getRemainingTimeString(UUID playerId) {
		long timeLeft = getRemainingTime(playerId) / 1000;
		return timeLeft + " seconds";
	}
}
