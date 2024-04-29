package org.example.spigot.antixray;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.Random;
import org.example.spigot.antixray.Antixray;
import java.util.HashMap;
import java.util.UUID;

public class StoneMiningListener implements Listener {
    private JavaPlugin plugin;
    private HashMap<UUID, Integer> tag = new HashMap<>();
    private final HashMap<UUID, Material> lastBrokenBlock = new HashMap<>();
    private HashMap<UUID, Long> lastMineTime = new HashMap<>();
    private HashMap<UUID, HashMap<Material, Integer>> blockCounts = new HashMap<>();
    private HashMap<UUID, HashMap<Material, Integer>> stoneCount = new HashMap<>();



    public StoneMiningListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /*
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        stoneCount.remove(playerId);
        blockCounts.remove(playerId);
        lastMineTime.remove(playerId); // 确保也移除了挖掘时间记录
        lastBrokenBlock.remove(playerId); //
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        stoneCount.remove(playerId);
        blockCounts.remove(playerId);
        lastMineTime.remove(playerId); // 确保也移除了挖掘时间记录
        lastBrokenBlock.remove(playerId); //
    }

     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        blockCounts.putIfAbsent(playerId, new HashMap<>());
        stoneCount.putIfAbsent(playerId, new HashMap<>());
        lastMineTime.putIfAbsent(playerId, 0L);
        lastBrokenBlock.putIfAbsent(playerId, Material.AIR); // 使用默认值，如Material.AIR表示没有挖掘
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        clearPlayerData(event.getPlayer().getUniqueId());
    }

    private void clearPlayerData(UUID playerId) {
        blockCounts.remove(playerId);
        stoneCount.remove(playerId);
        lastMineTime.remove(playerId);
        lastBrokenBlock.remove(playerId);
        tag.putIfAbsent(playerId, 0);
    }



    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Material brokenBlock = event.getBlock().getType();
        long currentTime = System.currentTimeMillis();
        tag.putIfAbsent(playerId, 0); // 确保 tag 已初始化
        HashMap<Material, Integer> counts = blockCounts.get(playerId);
        // 更新和检查玩家是否更换了挖掘的矿物类
        //if (brokenBlock != lastBrokenBlock.getOrDefault(playerId, brokenBlock) && plugin.getConfig().getConfigurationSection("trackedBlocks").getKeys(false).contains(brokenBlock)) {
        if (lastBrokenBlock.getOrDefault(playerId, brokenBlock) != Material.STONE && brokenBlock != Material.STONE && brokenBlock != lastBrokenBlock.getOrDefault(playerId, brokenBlock) && plugin.getConfig().getConfigurationSection("trackedBlocks").getKeys(true).contains(brokenBlock.toString())) {
                // 如果更换了矿物类型，则重置之前矿物的计数
            if (plugin.getConfig().getConfigurationSection("trackedBlocks").getKeys(false).contains(counts) && tag.get(playerId)>0) {
                counts.put(brokenBlock,0);
                tag.put(playerId, 0);
            } else {
                blockCounts.put(playerId, new HashMap<>());
            }
            //player.sendMessage("You have switched to a new type of block. Starting new count.");
        }
        // 追踪石头连续挖掘
        trackContinuousStoneMining(player, playerId, brokenBlock, currentTime);

        /*if(counts != null){
            player.sendMessage(brokenBlock + counts.getOrDefault(brokenBlock,0).toString());
        }*///调试模式

        // 处理配置文件中的方块
        handleTrackedBlocks(player, playerId, brokenBlock, counts);
        lastBrokenBlock.put(playerId, brokenBlock); // 更新玩家后挖掘的矿物类型
    }

    private void trackContinuousStoneMining(Player player, UUID playerId, Material brokenBlock, long currentTime) {
        // 确保 stoneCount 和 tag 有值
        stoneCount.putIfAbsent(playerId, new HashMap<>());
        tag.putIfAbsent(playerId, 0); // 确保 tag 不为null

        HashMap<Material, Integer> stoneBroken = stoneCount.get(playerId);
        stoneBroken.putIfAbsent(brokenBlock, 0);

        if (Antixray.stoneLimits.containsKey(brokenBlock))
        {
            if (lastMineTime.containsKey(playerId) && currentTime - lastMineTime.get(playerId) <= 3000)
            {
                stoneBroken.put(brokenBlock, stoneBroken.get(brokenBlock) + 1);
                if (stoneBroken.get(brokenBlock) >= Antixray.stoneLimits.get(brokenBlock))
                {
                    tag.put(playerId, tag.get(playerId) + 1);
                    //player.sendMessage(tag.get(playerId).toString());
                }
            }
            else
            {
                stoneBroken.put(brokenBlock, 0); // 重置计数
                tag.put(playerId, 0); // 重置 tag
            }
            lastMineTime.put(playerId, currentTime); // 更新时间
            if(lastBrokenBlock.get(playerId) != brokenBlock){
                stoneBroken.put(lastBrokenBlock.get(playerId),0);
            }
        }
    }

    public void handleTrackedBlocks(Player player, UUID playerId, Material brokenBlock, HashMap<Material, Integer> counts) {
        blockCounts.putIfAbsent(playerId, new HashMap<>());
        if (tag.containsKey(playerId) && tag.getOrDefault(playerId,0)>0) {
            blockCounts.putIfAbsent(playerId, new HashMap<>());
            if (plugin.getConfig().getConfigurationSection("trackedBlocks").getKeys(false).contains(brokenBlock.toString())) {
                int limit = plugin.getConfig().getInt("trackedBlocks." + brokenBlock.toString());
                counts.put(brokenBlock, counts.getOrDefault(brokenBlock, 1) + 1);
                if (counts.get(brokenBlock) >= limit) {
                    Random random = new Random();
                    int r = random.nextInt(10);
                    switch (r) {
                        case 1:
                            player.kickPlayer("开了就是开了?");
                            break;
                        case 2:
                            player.kickPlayer("老实点");
                            break;
                        case 3:
                            player.kickPlayer("你在干嘛");
                            break;
                        case 4:
                            player.kickPlayer("你做了什么心里有数");
                            break;
                        case 5:
                            player.kickPlayer("今日不宜挖矿~");
                            break;
                        case 6:
                            player.kickPlayer("别以为我不知道你在干嘛");
                            break;
                        case 7:
                            player.kickPlayer("下次可不就是踢出游戏这么简单了");
                            break;
                        case 8:
                            player.kickPlayer("关了吧,没意思");
                            break;
                        case 9:
                            player.kickPlayer("住手,快住手!");
                            break;
                        case 0:
                            player.kickPlayer("妈妈说不准开挂");
                            break;
                        default:
                            player.kickPlayer("你干了什么");
                    }

                    //player.kick();
                }
            }
        }
    }

    /*private void resetBlockCounts(UUID playerId) {
        // 确保blockCounts中对应的HashMap已经存在，如果不存在则初始化
        HashMap<Material, Integer> counts = blockCounts.get(playerId);
        if (counts != null) {
            counts.clear();
        } else {
            // 如果没有记录，则创建1个新的HashMap
            blockCounts.put(playerId, new HashMap<>());
        }
        stone.put(playerId, 0);  // 重置与石头相关的计数
    }*/

}
