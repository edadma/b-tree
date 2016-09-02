package xyz.hyperreal.btree

import xyz.hyperreal.ramfile.RamFile

import scala.io.Codec


class FileBPlusTree( order: Int ) extends AbstractBPlusTree[String, Any, Long]( order ) {
	val LEAF_NODE = 0
	val INTERNAL_NODE = 1
	
	val TYPE_STRING = 0x10
	val TYPE_INT = 0x11
	val TYPE_LONG = 0x12
	val TYPE_DOUBLE = 0x13
	val TYPE_BOOLEAN = 0x14
	val TYPE_NULL = 0x15
	
	val FILE_HEADER = 0
	val FILE_ORDER = FILE_HEADER + 12
	val FILE_FREE_PTR = FILE_ORDER + 2
	val FILE_ROOT_PTR = FILE_FREE_PTR + 8
	
	val NODE_TYPE = 0
	val NODE_LENGTH = NODE_TYPE + 1
	val NODE_HEAD_PTR = NODE_LENGTH + 4
	val NODE_KEYS = NODE_HEAD_PTR + 8
	
	val NUL = 0
	
	val btree = new RamFile( "btree" )
	
	var root =
		if (btree.length == 0) {
			btree writeBytes "B+ Tree v0.1"
			btree writeLong NUL
			btree writeLong btree.length

			val res = btree.length
			
			// write root leaf
		} else {
			btree seek FILE_ROOT_PTR
			btree readLong
		}
	
	def branch( node: Long, index: Int ): Long = {
		0
	}
	
	def branches( node: Long ): Seq[Long] = {
		Seq( 0L )
	}
	
	def getKey( node: Long, index: Int ): String = {
		btree seek (node + NODE_KEYS)
		
	}
	
	def getValue( node: Long, index: Int ): Any = {
		
	}
	
	def insertBranch( node: Long, index: Int, key: String, branch: Long ) {
		
	}
	
	def insertValue( node: Long, index: Int, key: String, value: Any ) {
		
	}
	
	def isLeaf( node: Long ): Boolean = {
		btree seek node
		btree.read == LEAF_NODE
	}
	
	private def readData( addr: Long ) = {
		def readUTF8( len: Int ) = {
			val a = new Array[Byte]( len )
			
			btree readFully a
			new String( Codec fromUTF8 a )
		}
		
		btree seek addr
		
		btree read match {
			case len if len <= 0x08 => readUTF8( len )
			case TYPE_STRING => 
				btree seek btree.readLong
				readUTF8( btree readInt )
			case TYPE_INT => btree readInt
		}
	}
	
	private def readString( addr: Long ) = readData.asInstanceOf[String]
	
	def keys( node: Long ): Seq[String] =
		new Seq[String] {
			def apply( idx: Int ) = 
			
			def iterator =
				new Iterator[String] {
					def hasNext =
					
					def next = 
				}
				
			def length = {
				btree seek (node + 1)
				btree.readInt
			}
		}
	
	def length( node: Long ): Int = 
	def moveInternal( src: Long, begin: Int, end: Int, dst: Long ) {
		
	}
	
	def moveLeaf( src: Long, begin: Int, end: Int, dst: Long ) {
	def newInternal( parent: Long ): Long = 0
	def newLeaf( parent: Long ): Long = 
	def newRoot( branch: Long ): Long = 
	def next( node: Long ): Long = 
	def next( node: Long, p: Long ) {
		
	}
	
	def nul: Long = 0
	
	def parent( node: Long ): Long = 
	def parent( node: Long, p: Long ) {
	def prev( node: Long ): Long = 
	def prev( node: Long, p: Long ) {
	def setValue( node: Long, index: Int, v: Any ) {
	def values( node: Long ): Seq[Any] = 

}

/* Example B+ Tree File Format
 * ***************************

fixed length ASCII text			"B+ Tree v0.1" (12)
branching factor						short (2)
free block pointer					long (8)
root node pointer						long (8)
====================================
Leaf Node
------------------------------------
type												0 (1)
length											short (2)
head pointer								long (8)
key/value array
	key type									byte (1)
	key data									(8)
	. . .
prev pointer								long (8)
next pointer								long (8)
value array
	value type								byte (1)
	value data								(8)
	. . .

*/