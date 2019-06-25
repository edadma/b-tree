package xyz.hyperreal.btree

import io.Codec
import collection.mutable.ArrayBuffer
import collection.AbstractSeq
import java.io._

trait FileBPlusTreeFormat {
	
	val POINTER_SIZE = 8
	
	val TREE_ROOT_PTR = 0
	val TREE_FIRST_PTR = TREE_ROOT_PTR + POINTER_SIZE
	val TREE_LAST_PTR = TREE_FIRST_PTR + POINTER_SIZE
	val TREE_RECORD_SIZE = TREE_LAST_PTR + POINTER_SIZE
	
	val FILE_HEADER = 0
	val FILE_HEADER_SIZE = 12
	val FILE_ORDER = FILE_HEADER + FILE_HEADER_SIZE
	val FILE_FREE_PTR = FILE_ORDER + 2
	val FILE_ROOT_RECORD = FILE_FREE_PTR + POINTER_SIZE
	val FILE_BLOCKS = FILE_ROOT_RECORD + TREE_RECORD_SIZE
	
}

object FileBPlusTree extends FileBPlusTreeFormat {
	
	/**
	 * Re-open a previously created (and closed) file created by the class constructor. This method basically reads the order from the file so that it does not need to be known to re-open the file.
	 * 
	 * @return a [[FileBPlusTree]] instance for accessing the B+ tree
	 */
	def apply[K <% Ordered[K], V]( filename: String, synchronous: Boolean = false ) = {
		val file = new RandomAccessFile( filename, if (synchronous) "rws" else "rw" )
		
		file seek FILE_ORDER
		new FileBPlusTree[K, V]( file, FILE_ROOT_RECORD, file.readShort )
	}
	
}

/**
 * An on-disk B+ Tree implementation.
 * 
 * @constructor creates an object to provide access to a B+ Tree contained within `file` with it's tree descriptor record at offset `tree` and with a branching factor of `order`. The on-disk tree's order must be equal to `order` or an exception will be thrown. This constructor is used internally to access a B+ tree that is a value in another B+ tree.
 * @param file the `java.io.RandomAccessFile` to be used to access the B+ tree
 * @param tree the offset within `file` of the tree's descriptor record
 * @param order the branching factor (maximum number of branches in an internal node) of the tree
 * @tparam K the type of the keys contained in this map.
 * @tparam V the type of the values associated with the keys.
 */
class FileBPlusTree[K <% Ordered[K], V]( protected val file: RandomAccessFile, protected val tree: Long, val order: Int ) extends BPlusTree[K, V] with FileBPlusTreeFormat {
	
	/**
	 * creates an object to provide access to the root B+ Tree contained within the file at `filename` with a branching factor of `order`. The on-disk tree's order must be equal to `order` or an exception will be thrown. This is the constructor that would normally be used to access a B+ tree file.
	 * 
	 * @param filename path to the file to contain the tree. The file created is completely self contained: if the file alread exists (and `newfile` is `false`) then the object opens the file and provides continued access to the tree
	 * @param order the branching factor (maximum number of branches in an internal node) of the tree
	 * @param synchronous `true` causes any changes to the file to be written to disk immediately, `false` is the default
	 */
	def this( filename: String, order: Int, synchronous: Boolean = false ) {
		this( new RandomAccessFile(filename, if (synchronous) "rws" else "rw"), FileBPlusTree.FILE_ROOT_RECORD, order )
	}
	
	val NUL = 0
	
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
	val TYPE_ARRAY = 0x16
	val TYPE_MAP = 0x17
	
	val DATUM_SIZE = 1 + 8		// type + contents
	val DATA_ARRAY_SIZE = (order - 1)*DATUM_SIZE
	
	val NODE_TYPE = 0
	val NODE_PARENT_PTR = NODE_TYPE + 1
	val NODE_PREV_PTR = NODE_PARENT_PTR + POINTER_SIZE
	val NODE_NEXT_PTR = NODE_PREV_PTR + POINTER_SIZE
	val NODE_LENGTH = NODE_NEXT_PTR + POINTER_SIZE
	val NODE_KEYS = NODE_LENGTH + 2
	
	val LEAF_VALUES = NODE_KEYS + DATA_ARRAY_SIZE
	
	val INTERNAL_BRANCHES = NODE_KEYS + DATA_ARRAY_SIZE
	
	val BLOCK_SIZE = LEAF_VALUES + (DATA_ARRAY_SIZE max (order*POINTER_SIZE))
	
	protected type N = Long
	
	private var savedNode: Long = NUL
	private var savedLength: Int = _
	private val savedKeys = new Array[Byte]( order*DATUM_SIZE )
	private val savedValues = new Array[Byte]( order*DATUM_SIZE )
	private val savedBranches = new Array[Long]( order + 1 )
	
	protected var root: Long = _
	protected var first: Long = _
	protected var last: Long = _
	protected var lastlen: Int = _
		
	file seek 0
		
	if (file.length == 0) {
		file writeBytes "B+ Tree v1  "
		file writeShort order
		file writeLong NUL
		writeEmptyRecord( FILE_BLOCKS )
		root = newLeaf( NUL )
		first = FILE_BLOCKS
		last = FILE_BLOCKS
		lastlen = 0
	} else {
		val header = new Array[Byte]( FILE_HEADER_SIZE )
		
		file readFully header
		
		if (header.toList != "B+ Tree v1  ".getBytes.toList)
			sys.error( "bad file header" )
			
		if (file.readShort != order)
			sys.error( "order not the same as on disk" )
			
		file seek tree
		root = file.readLong
		first = file.readLong
		last = file.readLong
		lastlen = nodeLength( last )
	}

	protected def writeEmptyRecord( node: Long ) {
		file writeLong node
		file writeLong node
		file writeLong node
	}
	
  protected def addBranch( node: Long, branch: Long ) = setBranch( node, nodeLength(node), branch )
		
	protected def addKey( node: Long, key: K ) {
		setKey( node, nodeLength(node), key )
		nodeLength( node, nodeLength(node) + 1 )
	}
	
	protected def freeKey( node: N, index: Int ) = dispose( node + NODE_KEYS + index*DATUM_SIZE )
	
	protected def freeNode( node: Long ) = free( node, BLOCK_SIZE )
	
	protected def freeValue( node: N, index: Int ) = dispose( node + LEAF_VALUES + index*DATUM_SIZE )
	
	protected def getBranch( node: Long, index: Int ) = {
		if (node == savedNode)
			savedBranches(index)
		else {
			file seek (node + INTERNAL_BRANCHES + index*POINTER_SIZE)
			file readLong
		}
	}
	
	protected def getBranches( node: Long ): Seq[Long] =
		new AbstractSeq[Long] with IndexedSeq[Long] {
			def apply( idx: Int ) = getBranch( node, idx )
			
			def length = nodeLength( node ) + 1
		}
	
	protected def getKey( node: Long, index: Int ) =
		(if (savedNode == node)
			readDatumArray( savedKeys, index*DATUM_SIZE )
		else
			readDatumFile( node + NODE_KEYS + index*DATUM_SIZE )).asInstanceOf[K]
	
	protected def getKeys( node: Long ): Seq[K] =
		new AbstractSeq[K] with IndexedSeq[K] {
			def apply( idx: Int ) = getKey( node, idx )
				
			def length = nodeLength( node )
		}
	
	protected def getNext( node: Long ): Long = {
		file seek (node + NODE_NEXT_PTR)
		file readLong
	}
	
	protected def getParent( node: Long ): Long = {
		file seek (node + NODE_PARENT_PTR)
		file readLong
	}
	
	protected def getPrev( node: Long ): Long = {
		file seek (node + NODE_PREV_PTR)
		file readLong
	}
	
	protected def getValue( node: Long, index: Int ) = readDatumFile( node + LEAF_VALUES + index*DATUM_SIZE ).asInstanceOf[V]
	
	protected def getValues( node: Long ) =
		new AbstractSeq[V] with IndexedSeq[V] {
			def apply( idx: Int ) = getValue( node, idx )
				
			def length = nodeLength( node )
		}
	
	protected def insertInternal( node: Long, keyIndex: Int, key: K, branchIndex: Int, branch: Long ) {
		val len = nodeLength( node )
		
		if (len < order - 1) {
			nodeLength( node, len + 1 )
			
			if (keyIndex < len)
				copyKeys( node, keyIndex, len, node, keyIndex + 1 )
			
			if (branchIndex < len + 1) {
				val data = new Array[Byte]( (len + 1 - branchIndex)*POINTER_SIZE )
			
				file seek (node + INTERNAL_BRANCHES + branchIndex*POINTER_SIZE)
				file readFully data
				file seek (node + INTERNAL_BRANCHES + (branchIndex + 1)*POINTER_SIZE)
				file write data
			}
			
			setKey( node, keyIndex, key )
			file seek (node + INTERNAL_BRANCHES + branchIndex*POINTER_SIZE)
			file writeLong branch
		} else {
			if (savedNode != NUL)
				sys.error( "a node is already being saved" )
				
			file seek (node + NODE_KEYS)
			file readFully (savedKeys, 0, keyIndex*DATUM_SIZE)
			file seek (node + NODE_KEYS + keyIndex*DATUM_SIZE)
			file readFully (savedKeys, (keyIndex + 1)*DATUM_SIZE, (len - keyIndex)*DATUM_SIZE)
			writeDatumArray( savedKeys, keyIndex, key )
			getBranches( node ).copyToArray( savedBranches, 0, branchIndex )
			getBranches( node ).view( branchIndex, len + 1 ).copyToArray( savedBranches, branchIndex + 1 )
			savedBranches( branchIndex ) = branch
			savedLength = len + 1
			savedNode = node
		}
	}
	
	protected def insertLeaf[V1 >: V]( node: Long, index: Int, key: K, value: V1 ) {
		val len = nodeLength( node )
		
		if (len < order - 1) {
			nodeLength( node, len + 1 )
			
			if (index < len)
				copyLeaf( node, index, len, node, index + 1 )
			
			setKey( node, index, key )
			setValue( node, index, value )
		} else {
			if (savedNode != NUL)
				sys.error( "a node is already being saved" )
				
			file seek (node + NODE_KEYS)
			file readFully (savedKeys, 0, index*DATUM_SIZE)
			file seek (node + NODE_KEYS + index*DATUM_SIZE)
			file readFully (savedKeys, (index + 1)*DATUM_SIZE, (len - index)*DATUM_SIZE)
			writeDatumArray( savedKeys, index, key )
			file seek (node + LEAF_VALUES)
			file readFully (savedValues, 0, index*DATUM_SIZE)
			file seek (node + LEAF_VALUES + index*DATUM_SIZE)
			file readFully (savedValues, (index + 1)*DATUM_SIZE, (len - index)*DATUM_SIZE)
			writeDatumArray( savedValues, index, value )
			savedLength = len + 1
			savedNode = node
		}
	}
	
	protected def isLeaf( node: Long ): Boolean = {
		file seek node
		file.read == LEAF_NODE
	}
	
	protected def moveInternal( src: Long, begin: Int, end: Int, dst: Long, index: Int ) {
		if (savedNode == NUL) {
			val dstlen = nodeLength( dst )
			val srclen = nodeLength( src )
			
			copyInternal( dst, index, dstlen, dst, index + end - begin )
			copyInternal( src, begin, end, dst, index )
			copyInternal( src, end, srclen, src, begin )
			nodeLength( src, srclen - (end - begin) )
			nodeLength( dst, dstlen + end - begin )
		} else {
			val dstKeys = savedKeys.view( begin*DATUM_SIZE, end*DATUM_SIZE ).toArray
			val dstBranches = savedBranches.view( begin + 1, end + 1 ).toArray
			val len = savedLength - (end - begin)
			file seek (src + NODE_KEYS)
			file write (savedKeys, 0, len*DATUM_SIZE)
			file seek (dst + NODE_KEYS)
			file write dstKeys

			for (i <- 0 to begin)
				setBranch( src, i, savedBranches(i) )

			for ((b, i) <- dstBranches zipWithIndex)
				setBranch( dst, i + 1, b )
				
			nodeLength( src, len )
			nodeLength( dst, end - begin )
			savedNode = NUL
		}
	}
	
	protected def moveLeaf( src: Long, begin: Int, end: Int, dst: Long, index: Int ) {
		val srclen = nodeLength( src )
		val dstlen = nodeLength( dst )
			
		if (savedNode == NUL) {
			copyLeaf( dst, index, dstlen, dst, index + end - begin )
			copyLeaf( src, begin, end, dst, index )
			copyLeaf( src, end, srclen, src, begin )
			nodeLength( src, srclen - (end - begin) )
			nodeLength( dst, dstlen + end - begin )
		} else {
			val dstKeys = savedKeys.view( begin*DATUM_SIZE, end*DATUM_SIZE ).toArray
			val dstValues = savedValues.view( begin*DATUM_SIZE, end*DATUM_SIZE ).toArray
			val dstlen = end - begin
			val len = savedLength - dstlen
			
			file seek (src + NODE_KEYS)
			file write (savedKeys, 0, len*DATUM_SIZE)
			file seek (src + LEAF_VALUES)
			file write (savedValues, 0, len*DATUM_SIZE)
			file seek (dst + NODE_KEYS)
			file write dstKeys
			file seek (dst + LEAF_VALUES)
			file write dstValues
			nodeLength( src, len )
			nodeLength( dst, dstlen )
			savedNode = NUL
		}
	}
	
	protected def newInternal( parent: Long ): Long = {
		val node = alloc( BLOCK_SIZE )
		
		file write INTERNAL_NODE
		file writeLong parent
		file writeLong NUL
		file writeLong NUL
		file writeShort 0
		node
	}
	
	protected def newLeaf( parent: Long ): Long = {
		val node = alloc( BLOCK_SIZE )
		
		file write LEAF_NODE
		file writeLong parent
		file writeLong NUL
		file writeLong NUL
		file writeShort 0
		node
	}
	
	protected def newRoot( branch: Long ): Long = {
		val node = newInternal( NUL )
		
		file seek (node + INTERNAL_BRANCHES)
		file writeLong branch
		file seek (tree + TREE_ROOT_PTR)
		file writeLong node
		node
	}
	
	protected def nodeLength( node: Long ) =
		if (node == savedNode)
			savedLength
		else {
			file seek (node + NODE_LENGTH)
			file.readShort
		}
	
	protected def nodeLength( node: Long, len: Int ) {
		file seek (node + NODE_LENGTH)
		file writeShort len
	}
	
	protected def nul = 0

	protected def removeInternal( node: Long, keyIndex: Int, branchIndex: Int ) =
		if (node == savedNode) {
			Array.copy( savedKeys, (keyIndex + 1)*DATUM_SIZE, savedKeys, keyIndex*DATUM_SIZE, (savedLength - keyIndex - 1)*DATUM_SIZE )
			Array.copy( savedBranches, branchIndex + 1, savedBranches, branchIndex, savedLength - branchIndex )
			savedLength -= 1
			savedLength
		} else {
			val len = nodeLength( node )
			val newlen = len - 1
			
			nodeLength( node, newlen )
			
			if (keyIndex < newlen)
				copyKeys( node, keyIndex + 1, len, node, keyIndex )
				
			if (branchIndex < len) {
				val data = new Array[Byte]( (len + 1 - (branchIndex + 1))*POINTER_SIZE )
				
				file seek (node + INTERNAL_BRANCHES + (branchIndex + 1)*POINTER_SIZE)
				file readFully data
				file seek (node + INTERNAL_BRANCHES + branchIndex*POINTER_SIZE)
				file write data
			}
			
			newlen
		}

	protected def dispose( addr: Long ) {
		file seek addr
		
		file.readByte match {
			case TYPE_STRING =>
				val s = file readLong
				
				file seek s
				free( s, file.readInt + 4)
			case TYPE_ARRAY =>
				val array = file readLong
				
				file seek array
				
				val len = file.readInt
				
				for (i <- 0 until len)
					dispose( i*DATUM_SIZE + array + 4 )
					
				free( array, len*DATUM_SIZE + 4 )
			case TYPE_MAP =>
				val rec = file.readLong
				
				new FileBPlusTree[K, V]( file, rec, order ).traverseBreadthFirst(
					nodes => {
						val isleaf = isLeaf( nodes.head )
						
						for (n <- nodes) {
							for (i <- 0 until nodeLength( n )) {
								freeKey( n, i )
								
								if (isleaf)
									freeValue( n, i )
							}
							
							freeNode( n )
						}
					}
				)
				
				free( rec, TREE_RECORD_SIZE )
			case _ =>
		}
	}
	
	protected def removeLeaf( node: Long, index: Int ) = {
		val len = nodeLength( node )
		val newlen = len - 1
		
		nodeLength( node, newlen )
		
		if (index < newlen)
			copyLeaf( node, index + 1, len, node, index )
			
		newlen
	}
	
	protected def setBranch( node: Long, index: Int, branch: Long ) {
		file seek (node + INTERNAL_BRANCHES + index*POINTER_SIZE)
		file writeLong branch
	}
	
	protected def setFirst( leaf: Long ) {
		file seek (tree + TREE_FIRST_PTR)
		file writeLong leaf
	}

	protected def setKey( node: Long, index: Int, key: K ) = writeDatumFile( node + NODE_KEYS + index*DATUM_SIZE, key )

	protected def setLast( leaf: Long ) {
		file seek (tree + TREE_LAST_PTR)
		file writeLong leaf
	}
	
	protected def setNext( node: Long, p: Long ) {
		file seek (node + NODE_NEXT_PTR)
		file writeLong p
	}
	
	protected def setParent( node: Long, p: Long ) {
		file seek (node + NODE_PARENT_PTR)
		file writeLong p
	}
	
	protected def setPrev( node: Long, p: Long ) {
		file seek (node + NODE_PREV_PTR)
		file writeLong p
	}
	
	protected def setRoot( node: Long ) {
		file seek (tree + TREE_ROOT_PTR)
		file writeLong node
	}
	
	protected def setValue[V1 >: V]( node: Long, index: Int, v: V1 ) = writeDatumFile( node + LEAF_VALUES + index*DATUM_SIZE, v )
	
		
	def close = file.close
	
	
	protected def copyKeys( src: Long, begin: Int, end: Int, dst: Long, index: Int ) = {
		val data = new Array[Byte]( (end - begin)*DATUM_SIZE )
		
		file seek (src + NODE_KEYS + begin*DATUM_SIZE)
		file readFully data
		file seek (dst + NODE_KEYS + index*DATUM_SIZE)
		file write data
		data
	}
	
	protected def copyLeaf( src: Long, begin: Int, end: Int, dst: Long, index: Int ) {
		val data = copyKeys( src, begin, end, dst, index )
		
		file seek (src + LEAF_VALUES + begin*DATUM_SIZE)
		file readFully data
		file seek (dst + LEAF_VALUES + index*DATUM_SIZE)
		file write data
	}
	
	protected def copyInternal( src: Long, begin: Int, end: Int, dst: Long, index: Int ) {
		copyKeys( src, begin, end, dst, index )
		
		val data = new Array[Byte]( (end - begin)*POINTER_SIZE )
		
		file seek (src + INTERNAL_BRANCHES + (begin + 1)*POINTER_SIZE)
		file readFully data
		file seek (dst + INTERNAL_BRANCHES + (index + 1)*POINTER_SIZE)
		file write data
	}
	
	protected def decode( in: DataInput ) = {
		def readUTF8( s: DataInput, len: Int ) = {
			val a = new Array[Byte]( len )
			
			s readFully a
			new String( Codec fromUTF8 a )
		}
		
		in readByte match {
			case len if len <= 0x08 => readUTF8( in, len )
			case TYPE_BOOLEAN_FALSE => false
			case TYPE_BOOLEAN_TRUE => true
			case TYPE_INT => in readInt
			case TYPE_LONG => in readLong
			case TYPE_DOUBLE => in readDouble
			case TYPE_STRING => 
				file seek file.readLong
				readUTF8( file, file readInt )
			case TYPE_NULL => null
			case TYPE_ARRAY =>
				val array = in.readLong
				
				file seek array
				
				val len = file readInt
				
				new collection.immutable.IndexedSeq[Any] {
					def apply( idx: Int ) = {
						require( idx >= 0 && idx < len, "index out of range" )
						readDatumFile( idx*DATUM_SIZE + array + 4 )
					}
					
					def length = len
					
					override def toString = mkString( "Array(", ", ", ")" )
				}
			case TYPE_MAP =>
				new MutableSortedMap[K, V]( new FileBPlusTree[K, V](file, in.readLong, order) )
		}
	}
		
	protected def readDatumFile( addr: Long ): Any = {
		file seek addr
		decode( file )
	}
	
	protected def readDatumArray( array: Array[Byte], index: Int ) = {
		val datum = new Array[Byte]( DATUM_SIZE )
		
		Array.copy( array, index, datum, 0, DATUM_SIZE )
		decode( new DataInputStream(new ByteArrayInputStream(datum)) )
	}
	
	protected def writeDatumArray( array: Array[Byte], index: Int, datum: Any ) {
		val os = new ByteArrayOutputStream

		encode( new DataOutputStream(os), datum )
		os.toByteArray.copyToArray( array, index*DATUM_SIZE )
	}
	
	protected def writeDatumFile( addr: Long, datum: Any ) {
		file seek addr
		encode( file, datum )
	}
	
	protected def encode( out: DataOutput, datum: Any ) {	
		datum match {
			case null => out write TYPE_NULL
			case false => out write TYPE_BOOLEAN_FALSE
			case true => out write TYPE_BOOLEAN_TRUE
			case d: Long =>
				out write TYPE_LONG
				out writeLong d
			case d: Int =>
				out write TYPE_INT
				out writeInt d
			case d: Double =>
				out write TYPE_DOUBLE
				out writeDouble d
			case d: String =>
				val utf = Codec.toUTF8( d )
				
				if (utf.length > 8) {
					out write TYPE_STRING
					
					val here = file.getFilePointer
					val addr = alloc( utf.length + 4 )
					
					file seek here
					out writeLong addr
					file seek addr
					file writeInt utf.length
					file write utf
				} else {
					out write utf.length
					out write utf
				}
			case m: collection.Map[K, V] =>
				out write TYPE_MAP
				
				val here = file.getFilePointer
				val newroot = newLeaf( NUL )
				val record = alloc( TREE_RECORD_SIZE )
				
				writeEmptyRecord( newroot )
				file seek here
				out writeLong record

				val newtree = new FileBPlusTree[K, V]( file, record, order )
			
				newtree load (m.toSeq: _*)
			case a: Seq[Any] =>
				out write TYPE_ARRAY
				
				val here = file.getFilePointer
				val array = alloc( a.length*DATUM_SIZE + 4 )
				
				file writeInt a.length
				
				for ((e, i) <- a zipWithIndex)
					writeDatumFile( array + 4 + i*DATUM_SIZE, e )
					
				file seek here
				out writeLong array
			case _ => sys.error( "type not supported: " + datum )
		}
	}
	
	protected def alloc( size: Int ) = {
		file seek FILE_FREE_PTR
		
		val ptr = file.readLong
		
		if (ptr == NUL || size > BLOCK_SIZE) {
			val addr = file.length
			val blocks = size/BLOCK_SIZE + (if (size%BLOCK_SIZE == 0) 0 else 1)
			
			file.setLength( addr + blocks*BLOCK_SIZE )
			file seek addr
			addr
		} else {
			file seek ptr
			
			val n = file readLong
			
			file seek FILE_FREE_PTR
			file writeLong n
			file seek ptr
			ptr
		}
	}
	
	protected def free( start: Long, size: Int ) {
		val blocks = size/BLOCK_SIZE + (if (size%BLOCK_SIZE == 0) 0 else 1)
		
		for (i <- 0 until blocks) {
			val block = start + i*BLOCK_SIZE
			
			file seek FILE_FREE_PTR
			
			val next = file readLong
			
			file seek block
			file writeLong next
			file seek FILE_FREE_PTR
			file writeLong block
		}
	}
	
	override protected def str( n: Long ) =
		if (n == savedNode)
			savedKeys.mkString( if (isLeaf(n)) "leaf[" else "internal[", ", ", "]" )
		else
			super.str( n )
			
	private [btree] def hex( n: Long* ) = println( n map (a => "%h" format a) mkString ("(", ", ", ")") )
	
	private [btree] def dump {
		val cur = file.getFilePointer
		val width = 16
		
		file.seek( 0 )
		
		def printByte( b: Int ) = print( "%02x ".format(b&0xFF).toUpperCase )
		
		def printChar( c: Int ) = print( if (' ' <= c && c <= '~') c.asInstanceOf[Char] else '.' )
		
		for (line <- 0L until file.length by width) {
			print( "%10x  ".format(line).toUpperCase )
			
			val mark = file.getFilePointer
			
			for (i <- line until ((line + width) min file.length)) {
				if (i%16 == 8)
					print( ' ' )
					
				printByte( file.readByte )
			}
			
			val bytes = (file.getFilePointer - mark).asInstanceOf[Int]
			
			print( " "*((width - bytes)*3 + 1 + (if (bytes < 9) 1 else 0)) )
			
			file.seek( mark )
			
			for (i <- line until ((line + width) min file.length))
				printChar( file.readByte.asInstanceOf[Int] )
				
			println
		}
		
		file.seek( cur )
	}

	/**
		* Creates a PNG image file called `name` (with `.png` added) which visually represents the structure and contents of the tree, only showing the keys. This method uses GraphViz (specifically the `dot` command) to produce the diagram, and ImageMagik (specifically the `convert` command) to convert it from SVG to PNG. `dot` can product PNG files directly but I got better results producing SVG and converting to PNG.
		*/
	def diagram( name: String ) {
		val before =
			"""	|digraph {
									|    graph [splines=line];
									|    edge [penwidth=2];
									|    node [shape = record, height=.1, width=.1, penwidth=2, style=filled, fillcolor=white];
									|
									|""".stripMargin

		def internalnode( n: N, id: N => String, emit: String => Unit ) = {
			val buf = new StringBuilder( id(n) + """[label = "<b0> &bull;""" )

			emit( id(n) + ":b0" + " -> " + id(getBranch(n, 0)) + ";" )

			for ((k, i) <- getKeys(n) zipWithIndex) {
				buf ++= " | " + k + " | <b" + (i + 1) + "> &bull;"
				emit( id(n) + ":b" + (i + 1) + " -> " + id(getBranch(n, i + 1)) + ";" )
			}

			buf ++= """"];"""
			buf toString
		}

		// 		def leafnode( n: N, id: N => String ) = id(n) + """[label = "<prev> &bull; | """ + (getKeys(n) mkString " | ") + """ | <next> &bull;"];"""
		def leafnode( n: N, id: N => String ) = id(n) + """[label = """" + (getKeys(n) mkString " | ") + """"];"""

		val file = new PrintWriter( name + ".dot" )

		file.println( serialize(before, "    ", internalnode, leafnode, "}") )
		file.close
		s"dot -Tsvg $name.dot -o $name.svg".!
		s"convert $name.svg $name.png".!
	}

}

/* Example B+ Tree File Format
 * ***************************

fixed length ASCII text			"B+ Tree v1  " (12)
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