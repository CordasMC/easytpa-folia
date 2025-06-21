package com.maybeizen.EasyTPA.commands;

import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPToggleCommand implements CommandExecutor {
	private final EasyTPA plugin;

	public TPToggleCommand(EasyTPA plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
			return true;
		}

		if (!player.hasPermission("easytpa.toggle")) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
			return true;
		}

		boolean newState = plugin.getToggleManager().toggleTP(player);
		String message = newState ?
				plugin.getConfigManager().getMessage("toggle-enabled") :
				plugin.getConfigManager().getMessage("toggle-disabled");

		MessageUtils.sendMessage(player, message);
		return true;
	}
} 