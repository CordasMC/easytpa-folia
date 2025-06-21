package com.maybeizen.EasyTPA.listener;

import com.maybeizen.EasyTPA.managers.TPAManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

	private final TPAManager tpaManager;

	public PlayerDeathListener(TPAManager tpaManager) {
		this.tpaManager = tpaManager;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getPlayer();

		if (tpaManager.hasIncomingRequest(player)) {
			tpaManager.cancelRequest(player);
		}
	}
}
