package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineRoomConnection
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.MutableWeightedList.Companion.mutableWeightedListOf
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class EnergyShrineRoom_Primary_SplitT(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineRoomConnection(Pos(centerX - 1, 0, 0), NORTH),
		EnergyShrineRoomConnection(Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		val decorations = mutableWeightedListOf(
			75 to Single(ModBlocks.DARK_CHEST.withFacing(WEST)),
			
			20 to Single(Blocks.FLOWER_POT), // TODO add death flower
			15 to Single(Blocks.FLOWER_POT),
			15 to Single(Blocks.FLOWER_POT),
			
			50 to Air,
			25 to Air,
			 5 to Air
		)
		
		for(z in 3..(maxZ - 3)){
			placeDecoration(world, Pos(maxX - 1, 2, z), decorations.removeItem(rand))
		}
		
		placeWallBanner(world, Pos(maxX - 1, 3, 1), WEST)
		placeWallBanner(world, Pos(maxX - 1, 3, maxZ - 1), WEST)
	}
}