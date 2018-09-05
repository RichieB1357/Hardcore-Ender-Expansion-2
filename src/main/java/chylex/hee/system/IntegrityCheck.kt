package chylex.hee.system
import chylex.hee.HardcoreEnderExpansion

object IntegrityCheck{
	var removedEnderChestRecipe: Boolean = false
	var removedPurpurRecipe: Boolean = false
	var removedEndRodRecipe: Boolean = false
	
	fun verify(){
		warnIfFalse(removedEnderChestRecipe, "could not remove vanilla Ender Chest recipe")
		warnIfFalse(removedPurpurRecipe, "could not remove vanilla Purpur Block recipe")
		warnIfFalse(removedEndRodRecipe, "could not remove vanilla End Rod recipe")
	}
	
	// Utilities
	
	private fun warnIfFalse(value: Boolean, message: String){
		if (!value){
			failIntegrityCheck(message, Debug.enabled)
		}
	}
	
	private fun crashIfFalse(value: Boolean, message: String){
		if (!value){
			failIntegrityCheck(message, true)
		}
	}
	
	private fun failIntegrityCheck(message: String, crash: Boolean){
		HardcoreEnderExpansion.log.error("[IntegrityCheck] $message")
		
		if (crash){
			throw IllegalStateException("Integrity check failed: $message")
		}
	}
}
