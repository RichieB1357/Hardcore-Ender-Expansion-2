package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.util.Pos

class ObsidianTowerRoom_Regular_CenterPillar(file: String) : ObsidianTowerRoom_General(file){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		if (world.rand.nextInt(4) != 0){
			placeSpawner(world, Pos(centerX, centerY, centerZ), instance)
		}
	}
}
