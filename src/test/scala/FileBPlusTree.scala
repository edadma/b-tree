package xyz.hyperreal.btree

import xyz.hyperreal.ramfile.RamFile


class FileBPlusTree( order: Int ) extends AbstractBPlusTree[String, Any, Long]( order ) {
	val LEAF_NODE = 0
	val INTERNAL_NODE = 1
	
	val btree = new RamFile( "btree" )
	
	var root =
		if (btree.length == 0) {
			btree writeBytes "B+ Tree v0.1"
			btree writeLong 0
			btree writeLong 28
			28
		} else {
			btree seek 20
			btree readLong
		}
	
	def branch( node: Long,index: Int ): Long = {
		0
	}
	
	def branches( node: Long ): Seq[Long] = {
		Seq( 0L )
	}
	
	def getKey( node: Long,index: Int ): String = {
		
	}
	
	def getValue( node: Long,index: Int ): Any = {
		
	}
	
	def insertBranch( node: Long,index: Int,key: String,branch: Long ) {
		
	}
	
	def insertValue( node: Long,index: Int,key: String,value: Any ) {
		
	}
	
	def isLeaf( node: Long ): Boolean = {
		btree seek node
		btree.read == LEAF_NODE
	}
	
	def keys( node: Long ): Seq[String] = 
	def length( node: Long ): Int = 
	def moveInternal( src: Long,begin: Int,end: Int,dst: Long ) {
		
	}
	
	def moveLeaf( src: Long,begin: Int,end: Int,dst: Long ) {
	def newInternal( parent: Long ): Long = 0
	def newLeaf( parent: Long ): Long = 
	def newRoot( branch: Long ): Long = 
	def next( node: Long ): Long = 
	def next( node: Long,p: Long ) {
		
	}
	
	def nul: Long = 0
	
	def parent( node: Long ): Long = 
	def parent( node: Long,p: Long ) {
	def prev( node: Long ): Long = 
	def prev( node: Long,p: Long ) {
	def setValue( node: Long,index: Int,v: Any ) {
	def values( node: Long ): Seq[Any] = 

}

/* Example B+ Tree File Format
 * ***************************

fixed length ASCII text			"B+ Tree v0.1" (12)
free block pointer					long (8)
root node pointer						long (8)
====================================
Leaf Node
------------------------------------
type												0 (1)
length											int (4)
prev pointer								long (8)
head pointer								long (8)
next pointer								long (8)
key/value array
	key type									byte (1)
	key data									(8)
	value type								byte (1)
	value data								(8)
	. . .

*/