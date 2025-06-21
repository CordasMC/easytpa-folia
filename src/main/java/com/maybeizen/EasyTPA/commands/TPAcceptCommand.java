package com.maybeizen.EasyTPA.commands;

import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPAcceptCommand implements CommandExecutor {
	private final EasyTPA plugin;

	public TPAcceptCommand(EasyTPA plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
			return true;
		}

		if (!player.hasPermission("easytpa.tpaccept")) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
			return true;
		}

		if (args.length > 0) {
			Player potentialRequester = plugin.getServer().getPlayer(args[0]);
			if (potentialRequester == null) {
				MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("player-not-found"));
				return true;
			}

			plugin.getTPAManager().acceptRequest(player, potentialRequester);
		} else {
			plugin.getTPAManager().acceptRequest(player);
		}

		return true;
	}
} 