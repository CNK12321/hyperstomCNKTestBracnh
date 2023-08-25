package emeraldwater.infernity.dev.commands

import emeraldwater.infernity.dev.instanceHub
import emeraldwater.infernity.dev.playerModes
import emeraldwater.infernity.dev.plots.*
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

/**
 * Handles logic for "joining plot 0" or the /leave command.
 */
fun handleLeavingLogic(player: Player){
    if (playerModes[player.username]?.mode == PlotMode.IN_HUB) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be in a plot to leave it!"))
        return
    }
    player.sendMessage(MiniMessage.miniMessage().deserialize("Leaving plot id ${playerModes[player.username]!!.id}..."))
    val future = player.setInstance(instanceHub)
    playerModes[player.username] = PlotState(0, PlotMode.IN_HUB)
    player.gameMode = GameMode.SURVIVAL
    future.thenRun {
        player.teleport(Pos(0.0, 52.0, 0.0))
    }
}

/**
 * Handles logic for /join and /play when not in a plot. This assumes that arguments has at least 2 elements, the command name and the plot id to join.
 * @param player the player who is joining.
 * @param id the plot id to join.
 */
fun handleJoinCommandLogic(player: Player, id: Int) {
    val filtered = plots.filter { it.id == id }
    if(id == 0){
        handleLeavingLogic(player)
        return
    }
    if(filtered.size == 1) {
        val plot = filtered[0]
        plot.joinInstance(player)
    } else {
        val plot = Plot(id)
        plot.joinInstance(player)
    }
}

object JoinCommand : Command("join") {
    init {
        setDefaultExecutor { sender, context -> sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must provide a plot id!")) }

        val plotId = ArgumentType.Integer("plot id")

        addSyntax({ sender, context ->
            val plotIdNum: Int = context.get(plotId)
            handleJoinCommandLogic(sender as Player, plotIdNum)
        }, plotId)
    }
}

object PlayCommand : Command("play") {
    init {
        setDefaultExecutor { sender, context ->
            val player = sender as Player
            val mode = playerModes[player.username]!!
            if (mode.mode != PlotMode.IN_HUB) {
            playerModes[player.username] = PlotState(mode.id, PlotMode.PLAY)
            try { player.setInstance(filterPlot(mode.id).buildInstance) } catch(_: Exception) {}
            player.teleport(Pos(1.0, 52.0, 1.0))
            player.setGameMode(GameMode.SURVIVAL)
            player.inventory.clear()
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must provide a plot id, or be on a plot to enter play mode!"))
            }
        }

        val plotId = ArgumentType.Integer("plot id")

        addSyntax({ sender, context ->
            val plotIdNum: Int = context.get(plotId)
            handleJoinCommandLogic(sender as Player, plotIdNum)
        }, plotId)
    }
}

object LeaveCommand : Command("leave") {
    init {
        setDefaultExecutor { sender, _ -> handleLeavingLogic(sender as Player)}
    }
}

object BuildCommand : Command("build") {
    init {
        setDefaultExecutor { sender, _ ->
            val player = sender as Player
            val mode = playerModes[player.username]!!
            if(mode.mode != PlotMode.IN_HUB) {
                playerModes[player.username] = PlotState(mode.id, PlotMode.BUILD)
                try { player.setInstance(filterPlot(mode.id).buildInstance) } catch(_: Exception) {}
                player.teleport(Pos(1.0, 52.0, 1.0))
                player.setGameMode(GameMode.CREATIVE)
                player.inventory.clear()
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be on a plot to use this command!"))
            }
        }
    }
}
object DevCommand : Command("dev", "code") {
    init {
        setDefaultExecutor { sender, _ ->
            val player = sender as Player
            val mode = playerModes[player.username]!!
            if(mode.mode != PlotMode.IN_HUB) {
                playerModes[player.username] = PlotState(mode.id, PlotMode.DEV)
                filterPlot(mode.id).joinDev(player)
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be on a plot to use this command!"))
            }
        }
    }
}

object DebugCommand : Command("debug"){
    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must have a valid subcommand!"))
        }

        val debugInfo = ArgumentType.Word("debug info").from("state")

        debugInfo.setCallback { sender, exception ->
            val input: String = exception.getInput()
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>$input is not a valid subcommand!"))
        }

        addSyntax({ sender, context ->
            val player = sender as Player
            val debugInfoToGet: String = context.get("debug info")
            if (debugInfoToGet == "state") {
                sender.sendMessage("player state: ${playerModes[player.username]!!}")
            }
        }, debugInfo)
    }
}

object SavePlotsCommand : Command("saveplots") {
    init {
        setDefaultExecutor { player, _ ->
            player.sendMessage("Saving plots to storage...")
            for(plot in plots) {
                val buildWorld = plot.buildInstance
                val savedBuildWorld = buildWorld.saveChunksToStorage()
                savedBuildWorld.thenRun {
                    player.sendMessage("Saved plot #${plot.id}'s build area to storage.")
                }
                val devWorld = plot.devInstance
                val savedDevWorld = devWorld.saveChunksToStorage()
                savedDevWorld.thenRun {
                    player.sendMessage("Saved plot #${plot.id}'s development area to storage.")
                }
            }
            player.sendMessage("Casted all futures.")
        }
    }
}

fun handleCommandRegistration(){
    MinecraftServer.getCommandManager().register(JoinCommand)
    MinecraftServer.getCommandManager().register(PlayCommand)
    MinecraftServer.getCommandManager().register(LeaveCommand)
    MinecraftServer.getCommandManager().register(BuildCommand)
    MinecraftServer.getCommandManager().register(DevCommand)
    MinecraftServer.getCommandManager().register(DebugCommand)
    MinecraftServer.getCommandManager().register(SavePlotsCommand)
}