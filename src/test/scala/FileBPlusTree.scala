package xyz.hyperreal.btree

import xyz.hyperreal.ramfile.RamFile

import scala.io.Codec


class FileBPlusTree( order: Int ) extends AbstractBPlusTree[String, Any, Long]( order ) {
	val LEAF_NODE = 0
	val INTERNAL_NODE = 1
	
	val TYPE_BOOLEAN = 0x10
		val TYPE_BOOLEAN_FALSE = 0x10
		val TYPE_BOOLEAN_TRUE = 0x18
	val TYPE_INT = 0x11
	val TYPE_LONG = 0x12
	val TYPE_DOUBLE = 0x13
	val TYPE_STRING = 0x14
	val TYPE_NULL = 0x15
	
	val FILE_HEADER = 0
	val FILE_ORDER = FILE_HEADER + 12
	val FILE_FREE_PTR = FILE_ORDER + 2
	val FILE_ROOT_PTR = FILE_FREE_PTR + 8
	val FILE_BLOCKS = FILE_ROOT_PTR + 8
	
	val NODE_TYPE = 0
	val NODE_PARENT_PTR = NODE_TYPE + 1
	val NODE_LENGTH = NODE_PARENT_PTR + 8
	val NODE_KEYS = NODE_LENGTH + 2
	
	val DATUM_SIZE = 1 + 8		// type + datum
	
	val DATA_ARRAY_SIZE = (order - 1)*DATUM_SIZE
	
	val LEAF_PREV_PTR = NODE_KEYS + DATA_ARRAY_SIZE
	val LEAF_NEXT_PTR = LEAF_PREV_PTR + 8
	val LEAF_VALUES = LEAF_NEXT_PTR + 8
	
	val BLOCK_SIZE = LEAF_VALUES + DATA_ARRAY_SIZE
	
	private [btree] val file = new RamFile( "btree" )
	
	protected [btree] var root =
		if (file.length == 0) {
			file writeBytes "B+ Tree v0.1"
			file writeShort order
			file writeLong nul
			file writeLong FILE_BLOCKS
			newLeaf( nul )
		} else {
			file seek FILE_ROOT_PTR
			file readLong
		}
	
	def branch( node: Long, index: Int ): Long = {
		ni
	}
	
	def branches( node: Long ): Seq[Long] = {
		ni
	}
	
	def getKey( node: Long, index: Int ) = readString( node + NODE_KEYS + index*DATUM_SIZE )
	
	def getValue( node: Long, index: Int ) = readDatum( node + LEAF_VALUES + index*DATUM_SIZE )
	
	def insertBranch( node: Long, index: Int, key: String, branch: Long ) {
		ni
	}
	
	def insertValue( node: Long, index: Int, key: String, value: Any ) {
		val len = nodeLength( node )
		
		file seek (node + NODE_LENGTH)
		file writeShort (len + 1)
		
		if (index < len) {
			val data = new Array[Byte]( (len - index)*DATUM_SIZE )
			
			file seek (node + NODE_KEYS + index*DATUM_SIZE)
			file readFully data
			file seek (node + NODE_KEYS + (index + 1)*DATUM_SIZE)
			file write data
			file seek (node + LEAF_VALUES + index*DATUM_SIZE)
			file readFully data
			file seek (node + LEAF_VALUES + (index + 1)*DATUM_SIZE)
			file write data
		}
		
		writeDatum( node + NODE_KEYS + index*DATUM_SIZE, key )
		writeDatum( node + LEAF_VALUES + index*DATUM_SIZE, value )
	}
	
	def isLeaf( node: Long ): Boolean = {
		file seek node
		file.read == LEAF_NODE
	}
	
	private def readDatum( addr: Long ) = {
		def readUTF8( len: Int ) = {
			val a = new Array[Byte]( len )
			
			file readFully a
			new String( Codec fromUTF8 a )
		}
		
		file seek addr
		
		file read match {
			case len if len <= 0x08 => readUTF8( len )
			case TYPE_STRING => 
				file seek file.readLong
				readUTF8( file readInt )
			case TYPE_INT => file readInt
		}
	}
	
	private def readString( addr: Long ) = readDatum( addr ).asInstanceOf[String]
	
	private def writeDatum( addr: Long, datum: Any ) = {
		file seek addr
		
		datum match {
			case null => file write TYPE_NULL
			case false => file write TYPE_BOOLEAN_FALSE
			case true => file write TYPE_BOOLEAN_TRUE
			case d: Long =>
				file write TYPE_LONG
				file writeLong d
			case d: Int =>
				file write TYPE_INT
				file writeInt d
			case d: Double =>
				file write TYPE_DOUBLE
				file writeDouble d
			case d: String =>
				val utf = Codec.toUTF8( d )
				
				if (utf.length > 8) {
					file write TYPE_STRING
					ni
				} else {
					file write utf.length
					file write utf
				}
				
			case _ => sys.error( "type not supported: " + datum )
		}
	}
	
	def keys( node: Long ): Seq[String] =
		new Seq[String] {
			def apply( idx: Int ) = getKey( node, idx )
			
			def iterator =
				new Iterator[String] {
					var len = nodeLength( node )
					var index = 0
					
					def hasNext = index < len
					
					def next = {
						if (!hasNext) throw new NoSuchElementException( "no more keys" )
							
						val res = getKey( node, index )
						
						index += 1
						res
					}
				}
				
			def length = nodeLength( node )
		}
	
	def moveInternal( src: Long, begin: Int, end: Int, dst: Long ) {
		ni
	}
	
	def moveLeaf( src: Long, begin: Int, end: Int, dst: Long ) {
		ni
	}
	
	def newInternal( parent: Long ): Long = {
		ni
	}
	
	private def alloc = {
		val addr = file.length
		
		file seek addr
		
		for (_ <- 1 to BLOCK_SIZE)
			file write 0
		
		file seek addr
		addr
	}
	
	def newLeaf( parent: Long ): Long = {
		val node = alloc
		
		file write LEAF_NODE
		file writeLong parent
		node
	}
	
	def newRoot( branch: Long ): Long = {
		ni
	}
	
	def next( node: Long ): Long = {
		file seek (node + LEAF_NEXT_PTR)
		file readLong
	}
	
	def next( node: Long, p: Long ) {
		ni
	}
	
	def nodeLength( node: Long ) = {
		file seek (node + NODE_LENGTH)
		file.readShort.toInt
	}
	
	def nul = 0
	
	def parent( node: Long ): Long = {
		file seek (node + NODE_PARENT_PTR)
		file readLong
	}
	
	def parent( node: Long, p: Long ) {
		ni
	}
	
	def prev( node: Long ): Long = {
		file seek (node + LEAF_PREV_PTR)
		file readLong
	}
	
	def prev( node: Long, p: Long ) {
		ni
	}
	
	def setValue( node: Long, index: Int, v: Any ) {
		ni
	}
	
	def values( node: Long ) =
		new Seq[Any] {
			def apply( idx: Int ) = getValue( node, idx )
			
			def iterator =
				new Iterator[Any] {
					var len = nodeLength( node )
					var index = 0
					
					def hasNext = index < len
					
					def next = {
						if (!hasNext) throw new NoSuchElementException( "no more keys" )
							
						val res = getValue( node, index )
						
						index += 1
						res
					}
				}
				
			def length = nodeLength( node )
		}

	private def ni = sys.error( "not implemented" )

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
parent pointer							long (8)
length											short (2)
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