package chylex.hee.game.item.trinket
import net.minecraft.entity.player.EntityPlayer

/**
 * Describes a Trinket item which itself can handle [ITrinketHandler] requests. The interface must be applied to a class extending [Item][net.minecraft.item.Item].
 * If an **active** Trinket implementing this interface is placed in the Trinket slot, its [ITrinketHandler] will be used instead of the default one.
 */
interface ITrinketHandlerProvider : ITrinketItem{
	fun createTrinketHandler(player: EntityPlayer): ITrinketHandler
}
