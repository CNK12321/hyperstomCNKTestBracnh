package emeraldwater.infernity.dev.events

import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTType

/**
 * Handles the logic for placing blocks in the developer space.
 * @param event The event to get the information from.
 */
fun placeDevBlock(event: PlayerBlockPlaceEvent) {
    event.isCancelled = true
    val x = event.blockPosition.blockX()
    val y = event.blockPosition.blockY()
    val z = event.blockPosition.blockZ()

    if(event.player.instance.getBlock(Pos(x.toDouble(), y-1.0, z.toDouble())) != Block.WHITE_STAINED_GLASS) return
    var placed = true
    when(event.block) {
        Block.DIAMOND_BLOCK -> {
            event.player.instance.setBlock(x, y, z, Block.DIAMOND_BLOCK.withTag(Tag.String("codeBlockType"), "block"))
            event.player.instance.setBlock(x, y, z+1, Block.STONE)
            event.player.instance.setBlock(x-1, y, z, signWithLines("PLAYER EVENT"))
        }
        Block.COBBLESTONE -> {
            event.player.instance.setBlock(x, y, z, Block.COBBLESTONE.withTag(Tag.String("codeBlockType"), "block"))
            event.player.instance.setBlock(x, y, z+1, Block.STONE)
            event.player.instance.setBlock(x, y+1, z, Block.BARREL)
            event.player.instance.setBlock(x-1, y, z, signWithLines("PLAYER ACTION"))
        }
        Block.IRON_BLOCK -> {
            event.player.instance.setBlock(x, y, z, Block.IRON_BLOCK.withTag(Tag.String("codeBlockType"), "block"))
            event.player.instance.setBlock(x, y, z+1, Block.STONE)
            event.player.instance.setBlock(x, y+1, z, Block.BARREL)
            event.player.instance.setBlock(x-1, y, z, signWithLines("SET VARIABLE"))
        }
        else -> {
            placed = false
            event.isCancelled = true
        }
    }
    if(placed) {
        val blocks = mutableListOf<Block>()
        for(sx in x-1..x) {
            for(sy in y..y+2) {
                for(sz in z+2..z+128) {
                    blocks.add(event.player.instance.getBlock(Vec(sx.toDouble(), sy.toDouble(), sz.toDouble())))
                    event.player.instance.setBlock(Vec(sx.toDouble(), sy.toDouble(), sz.toDouble()), Block.AIR)
                }
            }
        }
        val iter = blocks.iterator()
        for(sx in x-1..x) {
            for(sy in y..y+2) {
                for(sz in z+2+2..z+128+2) {
                    event.player.instance.setBlock(Vec(sx.toDouble(), sy.toDouble(), sz.toDouble()), iter.next())
                }
            }
        }
    }
}

/**
 * Handles the logic for breaking blocks in the developer space.
 * @param event The event to get the information from.
 */
fun breakDevBlock(event: PlayerBlockBreakEvent) {
    event.isCancelled = true

    val x = event.blockPosition.blockX()
    val y = event.blockPosition.blockY()
    val z = event.blockPosition.blockZ()

    println(event.block.getTag(Tag.String("codeBlockType")))
    var placed = true
    when(event.block.getTag(Tag.String("codeBlockType"))) {
        "block" -> {
            event.player.instance.setBlock(x, y, z, Block.AIR)
            event.player.instance.setBlock(x, y, z+1, Block.AIR)
            event.player.instance.setBlock(x, y+1, z, Block.AIR)
            event.player.instance.setBlock(x-1, y, z, Block.AIR)
        }
        "sign" -> {
            event.player.instance.setBlock(x+1, y, z, Block.AIR)
            event.player.instance.setBlock(x+1, y, z+1, Block.AIR)
            event.player.instance.setBlock(x+1, y+1, z, Block.AIR)
            event.player.instance.setBlock(x, y, z, Block.AIR)
        }
        else -> placed = false
    }
    if(placed) {
        val blocks = mutableListOf<Block>()
        for(sx in x-1..x+1) {
            for(sy in y..y+2) {
                for(sz in z+2..z+128+2) {
                    blocks.add(event.player.instance.getBlock(Vec(sx.toDouble(), sy.toDouble(), sz.toDouble())))
                    event.player.instance.setBlock(Vec(sx.toDouble(), sy.toDouble(), sz.toDouble()), Block.AIR)
                }
            }
        }
        val iter = blocks.iterator()
        for(sx in x-1..x+1) {
            for(sy in y..y+2) {
                for(sz in z..z+128) {
                    event.player.instance.setBlock(Vec(sx.toDouble(), sy.toDouble(), sz.toDouble()), iter.next())
                }
            }
        }
    }
}

/**
 * Returns a `Block` with text on it's lines.
 * @param lines The lines to place on the sign.
 */
fun signWithLines(vararg lines: String): Block {
    val line1 = if(lines.isNotEmpty()) { lines[0] } else { "" }
    val line2 = if(lines.size >= 2) { lines[1] } else { "" }
    val line3 = if(lines.size >= 3) { lines[2] } else { "" }
    val line4 = if(lines.size >= 4) { lines[3] } else { "" }
    return Block.OAK_WALL_SIGN
        .withNbt(NBT.Compound(mapOf(
            "is_waxed" to NBT.Boolean(true),
            "front_text" to NBT.Compound(mapOf(
                "color" to NBT.String("black"),
                "has_glowing_text" to NBT.Boolean(false),
                "messages" to NBT.List(NBTType.TAG_String, NBT.String("""{"text":"$line1"}"""), NBT.String("""{"text":"$line2"}"""), NBT.String("""{"text":"$line3"}"""), NBT.String("""{"text":"$line4"}"""))
            )),
            "back_text" to NBT.Compound(mapOf(
                "color" to NBT.String("black"),
                "has_glowing_text" to NBT.Boolean(false),
                "messages" to NBT.List(NBTType.TAG_String, NBT.String("""{"text":""}"""), NBT.String("""{"text":""}"""), NBT.String("""{"text":""}"""), NBT.String("""{"text":""}"""))
            ))
        )))
        .withProperty("facing", "west")
        .withTag(Tag.String("codeBlockType"), "sign")
        .withTag(Tag.String("line1"), line1)
        .withTag(Tag.String("line2"), line2)
        .withTag(Tag.String("line3"), line3)
        .withTag(Tag.String("line4"), line4)
}