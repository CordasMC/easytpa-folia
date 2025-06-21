package com.maybeizen.EasyTPA.model;

import org.bukkit.entity.Player;

public class TeleportRequest {
	private final Player sender;
	private final Player target;
	private boolean isCanceled;

	public TeleportRequest(Player sender, Player target) {
		this.sender = sender;
		this.target = target;
	}

	public Player getSender() {
		return this.sender;
	}

	public Player getTarget() {
		return this.target;
	}

	public boolean isCanceled() {
		return this.isCanceled;
	}

	public void setCanceled(boolean canceled) {
		this.isCanceled = canceled;
	}
}
