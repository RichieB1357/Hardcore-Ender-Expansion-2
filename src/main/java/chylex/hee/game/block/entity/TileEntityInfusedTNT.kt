package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.util.TagCompound

class TileEntityInfusedTNT : TileEntityBase(){
	var infusions = InfusionList.EMPTY
	
	override fun writeNBT(nbt: TagCompound, context: Context){
		if (context == STORAGE){
			InfusionTag.setList(nbt, infusions)
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context){
		if (context == STORAGE){
			infusions = InfusionTag.getList(nbt)
		}
	}
}
