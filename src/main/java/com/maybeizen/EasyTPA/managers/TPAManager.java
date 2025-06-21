package com.maybeizen.EasyTPA.managers;

import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.model.TeleportRequest;
import com.maybeizen.EasyTPA.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class TPAManager {
	private final EasyTPA plugin;
	private final CopyOnWriteArrayList<TeleportRequest> tpaRequests;
	private final CooldownManager cooldownManager;

	public TPAManager(EasyTPA plugin) {
		this.plugin = plugin;
		this.tpaRequests = new CopyOnWriteArrayList<>();
		this.cooldownManager = plugin.getCooldownManager();
	}

	public void addRequest(TeleportRequest request) {
		Player target = request.getTarget();
		Player sender = request.getSender();

		if (!plugin.getToggleManager().isTPEnabled(target) && !sender.hasPermission("easytpa.bypass")) {
			MessageUtils.sendMessage(sender,
					plugin.getConfigManager().getMessage("target-has-tp-disabled"),
					"player", target.getName()
			);
			return;
		}

		if (!sender.hasPermission("easytpa.cooldown.bypass") && cooldownManager.hasCooldown(sender.getUniqueId())) {
			long remainingTime = (cooldownManager.getRemainingTime(sender.getUniqueId()));
			MessageUtils.sendMessage(sender,
					plugin.getConfigManager().getMessage("cooldown"),
					"time", String.valueOf(remainingTime)
			);
			return;
		}

		if (hasIncomingRequest(target)) {
			MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("already-has-request"));
			return;
		}

		// Add the new request
		tpaRequests.add(request);

		// Send the actual messages
		MessageUtils.sendMessage(sender,
				plugin.getConfigManager().getMessage("request-sent", sender,
						"player", target.getName())
		);
		MessageUtils.sendTeleportRequest(sender, target);

		if (!sender.hasPermission("easytpa.cooldown.bypass")) {
			cooldownManager.setCooldown(sender.getUniqueId());
		}

		// schedule expiration using Folia scheduler
		plugin.getServer().getAsyncScheduler().runDelayed(plugin, task -> {
			// Is that same request still active? If yes, nuke that thing after the expiry time
			if (getIncomingRequestOf(target) != null && getIncomingRequestOf(target).equals(request)) {
				tpaRequests.remove(request);
				MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("request-expired"));
				if (target.isOnline()) {
					MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("request-expired-target"));
				}
			}
		}, plugin.getConfigManager().getRequestTimeout(), TimeUnit.SECONDS);

	}

	public void acceptRequest(Player target) {
		UUID targetUUID = target.getUniqueId();

		// Do they have an incoming request that they could accept?
		if (!hasIncomingRequest(target)) {
			MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("no-pending-request"));
			return;
		}

		TeleportRequest requestToAccept = getIncomingRequestOf(target);
		Player sender = requestToAccept.getSender();

		// The sender left or is invalid - nuke that request
		if (sender == null || !sender.isOnline()) {
			MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("player-offline"));
			tpaRequests.remove(requestToAccept);
			return;
		}

		// Schedule teleport with delay and cancellation support
		requestToAccept.setCanceled(false);
		scheduleTeleport(requestToAccept);
	}

	public void acceptRequest(Player player, Player potentialRequester) {
		if (!hasIncomingRequest(player) || !getIncomingRequestOf(player).getSender().equals(potentialRequester)) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-pending-request"));
			return;
		}

		TeleportRequest request = getIncomingRequestOf(player);
		request.setCanceled(false);
		scheduleTeleport(request);
	}

	private void scheduleTeleport(TeleportRequest request) {
		Player sender = request.getSender();
		Player target = request.getTarget();
		long delayTicks = plugin.getConfigManager().getTeleportDelay() * 20L;
		MessageUtils.sendMessage(sender,
				plugin.getConfigManager().getMessage("request-accepted"),
				"player", target.getName()
		);
		MessageUtils.sendMessage(target,
				plugin.getConfigManager().getMessage("request-accepted-target"),
				"player", sender.getName()
		);

		sender.getScheduler().runDelayed(plugin, task -> {
			if (!request.isCanceled()) {
				sender.teleportAsync(target.getLocation());
				MessageUtils.sendMessage(sender,
						plugin.getConfigManager().getMessage("teleport-success"),
						"player", target.getName()
				);
				MessageUtils.playTeleportEffect(sender);
			}
			tpaRequests.remove(request);
		}, null, delayTicks);
	}

	public String denyRequest(Player player) {
		if (!hasIncomingRequest(player)) {
			return null;
		}

		Player sender = getIncomingRequestOf(player).getSender();
		String senderName = sender != null && sender.isOnline() ? sender.getName() : "Unknown";

		tpaRequests.remove(getIncomingRequestOf(player));

		if (sender != null && sender.isOnline()) {
			MessageUtils.sendMessage(sender,
					plugin.getConfigManager().getMessage("request-denied"),
					"player", player.getName()
			);
		}

		return senderName;
	}

	public void denyRequest(Player player, Player requester) {
		if (!hasIncomingRequest(player) || !getIncomingRequestOf(player).getSender().equals(requester)) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-pending-request"));
			return;
		}

		tpaRequests.remove(getIncomingRequestOf(player));

		MessageUtils.sendMessage(requester,
				plugin.getConfigManager().getMessage("request-denied"),
				"player", player.getName()
		);

		MessageUtils.sendMessage(player,
				plugin.getConfigManager().getMessage("request-denied-target"),
				"player", requester.getName()
		);

	}

	public void cancelRequest(Player player) {
		TeleportRequest req = getIncomingRequestOf(player);
		if (req != null) {
			req.setCanceled(true);
			tpaRequests.remove(req);
		}
	}

	public void clearAllRequests() {
		tpaRequests.clear();
	}

	public boolean hasIncomingRequest(Player player) {
		for (TeleportRequest request : tpaRequests) {
			if (request.getTarget().equals(player)) return true;
		}

		return false;
	}

	public TeleportRequest getIncomingRequestOf(Player player) {
		for (TeleportRequest request : tpaRequests) {
			if (request.getTarget().equals(player)) return request;
		}

		return null;
	}
}
