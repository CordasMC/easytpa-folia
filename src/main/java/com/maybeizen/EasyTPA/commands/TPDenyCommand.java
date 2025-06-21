package com.maybeizen.EasyTPA.commands;

import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPDenyCommand implements CommandExecutor {
	private final EasyTPA plugin;

	public TPDenyCommand(EasyTPA plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-only")));
			return true;
		}

		if (!player.hasPermission("easytpa.tpdeny")) {
			MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-permission"));
			return true;
		}

		if (args.length > 0) {
			Player requester = plugin.getServer().getPlayer(args[0]);
			if (requester == null) {
				MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("player-not-found"));
				return true;
			}

			plugin.getTPAManager().denyRequest(player, requester);
		} else {
			String requesterName = plugin.getTPAManager().denyRequest(player);
			if (requesterName != null) {
				MessageUtils.sendMessage(player,
						plugin.getConfigManager().getMessage("request-denied-target"),
						"player", requesterName
				);
			} else {
				MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("no-pending-request"));
			}
		}

		return true;
	}
} 