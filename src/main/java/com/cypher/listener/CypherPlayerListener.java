package com.cypher.listener;

import com.cypher.LobbySwitch;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Derek on 8/5/2014.
 */
public class CypherPlayerListener implements Listener, PluginMessageListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().getInventory().contains(LobbySwitch.p.getFileConfig().getItemStack("ItemStack"))) {
            event.getPlayer().getInventory().addItem(LobbySwitch.p.getFileConfig().getItemStack("ItemStack"));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getType() == LobbySwitch.p.getFileConfig().getItemStack("ItemStack").getType()) {
            Inventory inventory = Bukkit.createInventory(null, 9, LobbySwitch.p.getFileConfig().getString("InventoryName"));

            for (String string : (ArrayList<String>) LobbySwitch.p.getFileConfig().getList("Servers")) {
                String[] split = string.split(":");
                ArrayList<String> itemLore = new ArrayList<String>();
                ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

                byteArrayDataOutput.writeUTF("PlayerCount");
                byteArrayDataOutput.writeUTF(split[3]);
                event.getPlayer().sendPluginMessage(LobbySwitch.p, "BungeeCord", byteArrayDataOutput.toByteArray());

                ItemStack itemStack = new ItemStack(Material.valueOf(split[0]), Integer.valueOf(split[1]));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("\247" + split[4] + split[2] + ":" + split[3]);
                itemStack.setItemMeta(itemMeta);

                inventory.addItem(itemStack);
            }
            event.getPlayer().openInventory(inventory);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        String server = in.readUTF();
        int playerCount = in.readInt();

        Inventory inventory = player.getOpenInventory().getTopInventory();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta.getDisplayName().split(":").length > 1) {
                    if (itemMeta.getDisplayName().split(":")[1].equals(server)) {
                        itemMeta.setDisplayName(itemMeta.getDisplayName().split(":")[0]);
                        itemMeta.setLore(Arrays.asList(String.valueOf(playerCount) + " Online"));
                        itemStack.setItemMeta(itemMeta);
                    }
                }
            }
        }
    }
}