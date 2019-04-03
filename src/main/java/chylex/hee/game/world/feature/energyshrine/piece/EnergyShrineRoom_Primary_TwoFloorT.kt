package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing
import net.minecraft.block.Block
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class EnergyShrineRoom_Primary_TwoFloorT(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineRoomConnection(Pos(maxX, 5, 2), EAST),
		EnergyShrineRoomConnection(Pos(0, 5, 3), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		world.setBlock(Pos(2, 0, 1), cornerBlock)
		world.setBlock(Pos(maxX - 2, 0, 1), cornerBlock)
		world.setBlock(Pos(1, 5, 1), cornerBlock)
		world.setBlock(Pos(maxX - 1, 5, 1), cornerBlock)
		
		placeWallBanner(world, Pos(6, 5, maxZ - 3), EAST)
		placeWallBanner(world, Pos(maxX - 6, 5, maxZ - 3), WEST)
		
		val rand = world.rand
		val chestPos = Pos(if (rand.nextBoolean()) 4 else maxX - 4, 6, maxZ - 1)
		
		world.setState(chestPos, ModBlocks.DARK_CHEST.withFacing(NORTH))
		world.addTrigger(chestPos, LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
	}
}