package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.palette.ColoredBlocks
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.random.nextItem

class EnergyShrineRoom_Secondary_Portal(file: String) : EnergyShrineRoom_Generic(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val carpet = Single(ColoredBlocks.CARPET.getValue(world.rand.nextItem()))
		
		world.placeCube(Pos(3, 1, 2), Pos(3, 1, maxZ - 1), carpet)
		world.placeCube(Pos(maxX - 3, 1, 2), Pos(maxX - 3, 1, maxZ - 1), carpet)
		
		placeWallBanner(world, instance, Pos(1, maxY - 2, maxZ - 2), NORTH)
		placeWallBanner(world, instance, Pos(maxX - 1, maxY - 2, maxZ - 2), NORTH)
	}
}
