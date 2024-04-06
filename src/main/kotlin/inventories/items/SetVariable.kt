package emeraldwater.infernity.dev.inventories.items

import emeraldwater.infernity.dev.interpreter.SetVariable
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.TransactionOption
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

private val menuItems: List<ItemStack> = listOf(
    DevItemBuilder()
        .item(Material.BRICK)
        .name("<red>Set Variable (=)")
        .description("<gray>Sets a variable to a value you define")
        .parameter(Type.VARIABLE, "Variable to set")
        .parameter(Type.ANY_VALUE, "Value to set")
        .build(SetVariable.SET_EQUALS)
)

fun displaySetVariableMenu(player: Player) {
    val inventory = Inventory(InventoryType.CHEST_1_ROW, "Player Event")
    inventory.addItemStacks(menuItems, TransactionOption.ALL)
    player.openInventory(inventory)
}