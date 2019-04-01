package chylex.hee.game.world.structure.piece
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode
import chylex.hee.game.world.structure.piece.StructureBuild.PositionedPiece
import chylex.hee.game.world.structure.piece.StructurePiece.MutableInstance
import chylex.hee.system.util.Rotation4
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nextItemOrNull
import net.minecraft.util.Rotation
import java.util.Random

interface IStructureBuilder{
	fun build(rand: Random): IStructureBuild?
	
	abstract class ProcessBase<T : MutableInstance>(protected val build: StructureBuild<T>, protected val rand: Random){
		protected fun baseAddPiece(mode: AddMode, targetPiece: PositionedPiece<T>, targetConnection: IStructurePieceConnection, generatedPieceConstructor: (Rotation) -> T): PositionedPiece<T>?{
			for(rotation in Rotation4.randomPermutation(rand)){
				val generatedInstance = generatedPieceConstructor(rotation)
				val connections = generatedInstance.findAvailableConnections(targetConnection)
				
				if (connections.isNotEmpty()){
					return build.addPiece(generatedInstance, rand.nextItem(connections), targetPiece, targetConnection, mode)
				}
			}
			
			return null
		}
		
		protected fun baseAddPiece(mode: AddMode, targetPiece: PositionedPiece<T>, generatedPieceConstructor: (Rotation) -> T): PositionedPiece<T>?{
			return rand.nextItemOrNull(targetPiece.instance.findAvailableConnections())?.let { targetConnection -> baseAddPiece(mode, targetPiece, targetConnection, generatedPieceConstructor) }
		}
	}
}
