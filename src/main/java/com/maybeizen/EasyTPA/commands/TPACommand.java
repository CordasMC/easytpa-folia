package com.maybeizen.EasyTPA.commands;

import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.model.TeleportRequest;
import com.maybeizen.EasyTPA.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {
	private final EasyTPA plugin;

	public TPACommand(EasyTPA plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
			return true;
		}

		if (!player.hasPermission("easytpa.tpa")) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
			return true;
		}

		if (args.length != 1) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("tpa-usage"));
			return true;
		}

		Player target = plugin.getServer().getPlayer(args[0]);

		if (target == null) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("player-not-found"));
			return true;
		}

		if (target.equals(player)) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("cannot-teleport-self"));
			return true;
		}

		if (!plugin.getToggleManager().isTPEnabled(target)) {
			MessageUtils.sendMessage(player,
					plugin.getConfigManager().getMessage("target-has-tp-disabled"),
					"player", target.getName()
			);
			return true;
		}

		if (plugin.getCooldownManager().hasCooldown(player.getUniqueId()) &&
				!player.hasPermission("easytpa.cooldown.bypass")) {
			String timeLeft = plugin.getCooldownManager().getRemainingTimeString(player.getUniqueId());
			MessageUtils.sendMessage(player,
					plugin.getConfigManager().getMessage("cooldown"),
					"time", timeLeft
			);
			return true;
		}

		TeleportRequest request = new TeleportRequest(player, target);
		plugin.getTPAManager().addRequest(request);


		return true;
	}
} 