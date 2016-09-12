package xyz.hyperreal.btree

import scala.sys.process._
import collection.mutable.{HashMap, ArrayBuffer}
import collection.immutable.ListMap

import java.io.PrintWriter


/**
 * Provides for interaction (insertion, update, deletion) with a B+ Tree that can be stored any where (in memory, on disk).  It is the implementation's responsability to create the empty B+ Tree initially.  An empty B+ Tree consists of a single empty leaf node as the root.  Also the `first` and `last` should refer to the root leaf node, and the `lastlen` variable should be 0.
 */
abstract class AbstractBPlusTree[K <% Ordered[K], V]( order: Int ) {
	/**
	 * Abstract node type. For in-memory implementations this would probably be the actual node class and for on-disk it would likely be the file pointer where the node is stored.
	 */
	protected type N
	
	/**
	 * Root node. Implementations are required to set this as well as to create/update the in-storage copy of this if needed (only really applies to on-disk implementations). The methods in this class will take care of updating this variable, implementations only need to worry about the in-storage copy.
	 */
	protected var root: N
		
	/**
	 * First leaf node. Implementations are required to set this as well as to create/update the in-storage copy of this if needed (only really applies to on-disk implementations) within the implementation's `newRoot()` method. The methods in this class will take care of updating this variable, implementations only need to worry about the in-storage copy.
	 */
	protected var first: N
		
	/**
	 * Last leaf node. Implementations are required to set this as well as to create/update the in-storage copy of this if needed (only really applies to on-disk implementations). The methods in this class will take care of updating this variable, implementations only need to worry about the in-storage copy.
	 */
	protected var last: N
		
	/**
	 * Length of the last leaf node.  This just speeds up bulk loading (the `load` method). Implementations are required to set this.
	 */
	protected var lastlen: Int
	
	/**
	 * Gets a branch pointer from an internal node at a given `index`.  There is always one more branch pointer than there are keys in an internal node so the highest index is equal to `nodeLength( node )`.
	 */
	protected def getBranch( node: N, index: Int ): N
	
	/**
	 * Gets the branches of `node` as a non-strict immutable sequence.
	 */
	protected def getBranches( node: N ): Seq[N]
		
	/**
	 * Gets a key from a leaf `node` at a given `index`.
	 */
	protected def getKey( node: N, index: Int ): K
	
	/**
	 * Gets the keys of `node` as a non-strict immutable sequence.
	 */	
	protected def getKeys( node: N ): Seq[K]
		
	/**
	 * Gets a value from a leaf `node` at a given `index`.
	 */
	protected def getValue( node: N, index: Int ): V
	
	/**
	 * Gets the values of `node` as a non-strict immutable sequence.
	 */	
	protected def getValues( node: N ): Seq[V]
	
	/**
	 * Inserts `key` and `branch` into (internal) `node` at `index`. The branch is the right branch immediately to the right of `key`.
	 */
	protected def insertInternal( node: N, index: Int, key: K, branch: N ): Unit
	
	/**
	 * Inserts `key` and `value` into (leaf) `node` at `index`.
	 */
	protected def insertLeaf( node: N, index: Int, key: K, value: V ): Unit
	
	/**
	 * Returns `true` if `node` is a leaf node
	 */
	protected def isLeaf( node: N ): Boolean
	
	/**
	 * Moves key/branch pairs as well as the left branch of the first key from node `src` to node `dst` beginning at index `begin` and ending up to but not including index `end`, and also removes the key at index `begin - 1`.
	 */
	protected def moveInternal( src: N, begin: Int, end: Int, dst: N ): Unit
	
	/**
	 * Moves key/value pairs from node `src` to node `dst` beginning at index `begin` and ending up to but not including index `end`.
	 */
	protected def moveLeaf( src: N, begin: Int, end: Int, dst: N ): Unit
	
	/**
	 * Creates a new internal node with `parent` as its parent pointer.
	 */
	protected def newInternal( parent: N ): N
	
	/**
	 * Creates a new leaf node with `parent` as its parent pointer.
	 */
	protected def newLeaf( parent: N ): N
	
	/**
	 * Creates a new root (internal) node with `branch` as its leftmost branch pointer and `null` parent pointer. Implementations are require to update the in-storage copy of the root pointer if needed (only really applies to on-disk implementations).
	 */
	protected def newRoot( branch: N ): N
	
	/**
	 * Sets the next pointer of (leaf) `node` to `p`.
	 */
	protected def setNext( node: N, p: N ): Unit
	
	/**
	 * Returns the next pointer of (leaf) `node`.
	 */
	protected def getNext( node: N ): N
	
	/**
	 * Returns the length (number of keys) of `node`. For internal nodes, the number of branch pointers will one more than the length.
	 */
	protected def nodeLength( node: N ): Int
	
	/**
	 * Returns the ''null'' node pointer.  For in-memory implementations this will usually be a Scala `null` value.  For on-disk it would make sense for this to be `0L`.
	 */
	protected def nul: N
	
	/**
	 * Sets the parent pointer of `node` to `p`.
	 */
	protected def parent( node: N, p: N ): Unit
	
	/**
	 * Returns the parent pointer of `node`.
	 */
	protected def parent( node: N ): N
	
	/**
	 * Sets previous leaf node link pointer of (leaf) `node` to `p`.
	 */
	protected def prev( node: N, p: N ): Unit
	
	/**
	 * Returns the previous leaf node link pointer of (leaf) `node`.
	 */
	protected def prev( node: N ): N

	/**
	 * Removes the key/value pair from leaf `node` at `index`. This method is perhaps poorly named: it does not remove a leaf node from the tree, that's done by (concrete method) `delete()`.
	 */
	protected def removeLeaf( node: N, index: Int ): Int
	
	/**
	 * not used yet, could be empty
	 */
	protected def setFirst( leaf: N ): Unit

	/**
	 * not used yet, could be empty
	 */
	protected def setLast( leaf: N ): Unit

	/**
	 * Sets the value at `index` of `node` to `v`.
	 */
	protected def setValue( node: N, index: Int, v: V ): Unit
	
	
	/**
	 * Returns a bounded iterator over a range of key/value pairs in the tree in sorted order. The range of key/value pairs in the iterator is specified by `bounds`.  `bounds` must contain one or two pairs where the first element in the pair is a symbol corresponding to the type of bound (i.e. '<, '<=, '>, '>=) and the second element is a key value.
	 * 
	 * An example of a bounded iterator over all elements in a tree (with `String` keys) that will include all keys that sort greater than or equal to "a" and up to but not including "e" is `boundedIterator( ('>=, "a"), ('<, "e") )`.
	 */
	def boundedIterator( bounds: (Symbol, K)* ): Iterator[(K, V)] = {
		def gte( key: K ) =
			lookupGTE( key ) match {
				case (_, leaf, index) => (leaf, index)
			}

		def gt( key: K ) =
			lookupGTE( key ) match {
				case (true, leaf, index) =>
					nextLeaf( leaf, index ) match {
						case (_, nl, ni) => (nl, ni)
						}
				case (false, leaf, index) => (leaf, index)
			}

		require( bounds.length == 1 || bounds.length == 2, "boundedIterator: one or two bounds" )
			
		val symbols = ListMap[Symbol, K => (N, Int)]( '> -> gt, '>= -> gte, '< -> gte, '<= -> gt )
		
		require( bounds forall {case (s, _) => symbols contains s}, "boundedIterator: expected one of '<, '<=, '>, '>=" )
		
		def translate( bound: Int ) = symbols(bounds(bound)._1)(bounds(bound)._2)
		
		def order( s: Symbol ) = symbols.keys.toList indexOf s
		
		val ((loleaf, loindex), (upleaf, upindex)) =
			if (bounds.length == 2) {
				require( bounds(0)._1 != bounds(1)._1, "boundedIterator: expected bounds symbols to be different" )

				val (lo, hi, (slo, klo), (shi, khi)) =
					if (order( bounds(0)._1 ) > order( bounds(1)._1 ))
						(translate( 1 ), translate( 0 ), bounds(1), bounds(0))
					else
						(translate( 0 ), translate( 1 ), bounds(0), bounds(1))
						
				if (klo > khi || klo == khi && ((slo != '>=) || (shi != '<=)))
					(lo, lo)
				else
					(lo, hi)
			} else if (order( bounds(0)._1 ) < 2)
				(translate( 0 ), (nul, 0))
			else
				((first, 0), translate( 0 ))
			
		new Iterator[(K, V)] {
			var leaf: N = loleaf
			var index: Int = loindex

			def hasNext = leaf != nul && index < nodeLength( leaf ) && (leaf != upleaf || index < upindex)

			def next =
				if (hasNext)
					nextLeaf( leaf, index ) match {
						case (kv, nl, ni) =>
							leaf = nl
							index = ni
							kv
						}
				else
					throw new NoSuchElementException( "no more keys" )
		}
	}
	
	def boundedIteratorOverKeys( bounds: (Symbol, K)* ) = boundedIterator( bounds: _* ) map {case (k, _) => k}
		
	def iterator: Iterator[(K, V)] =
		new Iterator[(K, V)] {
			var leaf = first
			var index = 0
			
			def hasNext = leaf != nul && index < nodeLength( leaf )
			
			def next =
				if (hasNext)
					nextLeaf( leaf, index ) match {
						case (kv, nl, ni) =>
							leaf = nl
							index = ni
							kv
						}
				else
					throw new NoSuchElementException( "no more keys" )
		}
	
	def iteratorOverKeys = iterator map {case (k, _) => k}
	
	def max =
		lastlen match {
			case 0 => 	None
			case l => Some( (getKey(last, l - 1), getValue(last, l - 1)) )
		}
	
	def min =
		if (nodeLength( first ) == 0)
			None
		else
			Some( (getKey(first, 0), getValue(first, 0)) )
	
	def insert( key: K, value: V = null.asInstanceOf[V] ) =
		lookup( key ) match {
			case (true, leaf, index) =>
				setValue( leaf, index, value )
				true
			case (false, leaf, index) =>
				insertAt	( key, value, leaf, index )
				false
		}
	
	def insertIfNotFound( key: K, value: V = null.asInstanceOf[V] ) =
		lookup( key ) match {
			case (true, _, _) => true
			case (false, leaf, index) =>
				insertAt	( key, value, leaf, index )
				false
		}
	
	def insertKeys( keys: K* ) =
		for (k <- keys)
			insert( k, null.asInstanceOf[V] )
		
	def load( kvs: (K, V)* ) = {
		require( !kvs.isEmpty, "expected some key/value pairs to load" )
		
		val seq = kvs sortBy {case (k, _) => k}
		
		max match {
			case None =>
			case Some( (maxkey, _) ) =>
				if (maxkey >= seq.head._1)
					sys.error( "can only load into non-empty tree if maximum element is less than minimum element to be loaded" )
		}
		
		seq foreach {case (k, v) => insertAt( k, v, last, lastlen )}
	}
	
	def search( key: K ): Option[V] =
		lookup( key ) match {
			case (true, leaf, index) => Some( getValue(leaf, index) )
			case _ => None
		}

		
		
	protected def binarySearch( node: N, target: K ): Int = {
		def search( start: Int, end: Int ): Int = {
			if (start > end)
				-start - 1
			else {
				val mid = start + (end-start + 1)/2
				
				if (getKey( node, mid ) == target)
					mid
				else if (getKey(node, mid) > target)
					search( start, mid - 1 )
				else
					search( mid + 1, end )
			}
		}
		
		search( 0, nodeLength(node) - 1 )
	}
	
	protected def lookup( key: K ): (Boolean, N, Int) = {
		def _lookup( n: N ): (Boolean, N, Int) =
			if (isLeaf( n ))
				binarySearch( n, key ) match {
					case index if index >= 0 => (true, n, index)
					case index => (false, n, -(index + 1))
				}
			else
				binarySearch( n, key ) match {
					case index if index >= 0 => _lookup( getBranch(n, index + 1) )
					case index => _lookup( getBranch(n, -(index + 1)) )
				}
			
		_lookup( root )
	}
	
	protected def nextLeaf( leaf: N, index: Int ) = {
		val kv = (getKey( leaf, index ), getValue( leaf, index ))
		
		if (index == nodeLength( leaf ) - 1)
			(kv, getNext( leaf ), 0)
		else
			(kv, leaf, index + 1)
	}
	
	protected def lookupGTE( key: K ) =
		lookup( key ) match {
			case t@(true, _, _) => t
			case f@(false, leaf, index) =>
				if (index < nodeLength( leaf ))
					f
				else
					(false, getNext( leaf ), 0)
		}
	
	protected def insertAt( key: K, value: V, leaf: N, index: Int ) {
		def split = {
			val newleaf = newLeaf( parent(leaf) )
			val leafnext = getNext( leaf )
			
			setNext( newleaf, leafnext )
			
			if (leafnext == nul) {
				last = newleaf
				setLast( newleaf )
			} else
				prev( leafnext, newleaf )
				
			setNext( leaf, newleaf )
			prev( newleaf, leaf )
			
			val len = nodeLength( leaf )
			val mid = len/2
			
			if (leafnext == nul)
				lastlen = len - mid
				
			moveLeaf( leaf, mid, len, newleaf )
			newleaf
		}
		
		insertLeaf( leaf, index, key, value )
		
		if (leaf == last)
			lastlen += 1

		if (nodeLength( leaf ) == order) {
			if (parent( leaf ) == nul) {
				root = newRoot( leaf )
				parent( leaf, root )
				
				val newleaf = split
				
				insertInternal( root, 0, getKey(newleaf, 0), newleaf )
			} else {
				var par = parent( leaf )
				val newleaf = split
				
				binarySearch( par, getKey(newleaf, 0) ) match {
					case index if index >= 0 => sys.error( "key found in internal node" )
					case insertion =>
						val index = -(insertion + 1)
						insertInternal( par, index, getKey(newleaf, 0), newleaf )
				
						while (nodeLength( par ) == order) {
							val newinternal = newInternal( parent(par) )
							val len = nodeLength( par )
							val mid = len/2
							val middle = getKey( par, mid )
							
							moveInternal( par, mid + 1, len, newinternal )
							
							for (child <- getBranches( newinternal ))
								parent( child, newinternal )
								
							if (parent( par ) == nul) {
								root = newRoot( par )
								
								parent( newinternal, root )
								parent( par, root )
								insertInternal( root, 0, middle, newinternal )
								par = root
							} else {
								par = parent( par )
								binarySearch( par, middle ) match {
									case index if index >= 0 => sys.error( "key found in internal node" )
									case insertion =>
										val index = -(insertion + 1)
										insertInternal( par, index, middle, newinternal )
								}
							}
						}
				}
			}
		}
	}
	
// 	def delete( key: K ) = {
// 		lookup( key ) match {
// 			case (true, leaf, index) =>
// 				val len = removeLeaf( leaf, index )
// 				
// 				if (len < order/2) {
// 					if (getNext( leaf ) != nul)
// 						
// 				}
// 				
// 				true
// 			case (false, leaf, index) => false
// 		}
// 	}
	
	/**
	 * Analyzes the tree to determine if it is well constructed.
	 * 
	 * @return `"true"` (as a string) if the tree is a well constructed B+ Tree, a string description of the flaw otherwise.
	 */
	def wellConstructed: String = {
		val nodes = new ArrayBuffer[N]
		var depth = -1
		var prevnode: N = nul
		var nextptr: N = nul
		
		def check( n: N, p: N, d: Int ): String = {
			if (!(getKeys( n ).dropRight( 1 ) zip getKeys( n ).drop( 1 ) forall( p => p._1 < p._2 )))
				return "incorrectly ordered keys"
			
			if (parent( n ) != p)
				return "incorrect parent pointer in level " + d
				
			if (isLeaf( n )) {
				if (depth == -1)
					depth = d
				else if (d != depth)
					return "leaf nodes not at same depth"
			
				if (prevnode == nul && first != n)
					return "incorrect first pointer"
					
				if (prevnode != prev( n ))
					return "incorrect prev pointer"
				else
					prevnode = n
					
				if (getNext( n ) == nul && last != n)
					return "incorrect last pointer"
					
				if ((nextptr != nul) && (nextptr != n))
					return "incorrect next pointer"
				else
					nextptr = getNext( n )
			}
			else {
				if (getBranches( n ) exists (p => p == nul))
					return "null branch pointer"
					
				if (getKeys( getBranch(n, 0) ) isEmpty)
					return "empty internal node"
					
				if (getKeys( n ) isEmpty)
					return "empty internal node"
					
				if (getKeys( getBranch(n, 0) ).last >= getKey( n, 0 ))
					return "left internal node branch not strictly less than"
					
				if (!(getKeys( n ) drop 1 zip getBranches( n ) drop 1 forall (p => getKey( p._2, 0 ) < p._1)))
					return "right internal node branch not greater than or equal"
				
				for (b <- getBranches( n ))
					check( b, n, d + 1 ) match {
						case "true" =>
						case error => return error
					}
			}
			
			"true"
		}
		
		check( root, nul, 0 ) match {
			case "true" =>
			case error => return error
		}
			
		if (nextptr != nul)
			return "rightmost next pointer not null"
			
		"true"
	}
	
	/**
	 * Returns a string representing a search result for `key` that will be consistant with `prettyPrint`. This method is used mainly for unit testing.
	 */
	def prettySearch( key: K ) = {
		val map = new HashMap[N, String]
		var count = 0
		
		def traverse( nodes: List[N] ) {
			for (n <- nodes) {
				map(n) = "n" + count
				count += 1
			}
			
			if (!isLeaf( nodes.head ))
				traverse( List.concat(nodes flatMap getBranches) )
		}
		
		traverse( List(root) )
		lookup( key ) match {
			case (true, leaf, index) => map(leaf) + " " + getValue( leaf, index ) + " " + index
			case _ => "not found"
		}
	}
	
	/**
	 * Returns a serialization (string representation of the tree) using string and function arguments to specify the exact form of the serialization. This method is used for ''pretty printing'' and to generate a DOT (graph description language) description of the tree so that it can be visualized.
	 * 
	 * @param before string to be prepended to the serialization
	 * @param prefix string to be place before each line of the serialization that includes internal and leaf nodes
	 * @param internalnode function to generate internal node serializations using three parameters: the current node, a function to return a string id of a node, a function that allows a line of text to be appended after all nodes have been serialized
	 * @param leafnode function to generate leaf node serializations using two parameters: the current node, a function to return a string id of a node
	 * @param after string to be appended to the serialization
	 */
	protected def serialize( before: String, prefix: String, internalnode: (N, N => String, String => Unit) => String, leafnode: (N, N => String) => String, after: String ) = {
		val buf = new StringBuilder( before )
		val afterbuf = new StringBuilder
		val map = new HashMap[N, String]
		var count = 0
		
		def id( node: N ) =
			if (node == nul)
				"null"
			else
				map get node match {
					case Some( n ) => n
					case None =>
						val n = "n" + count
						map(node) = n
						count += 1
						n
				}
		
		def emit( s: String ) = {
			afterbuf ++= prefix
			afterbuf ++= s
			afterbuf += '\n'
		}
		
		def printNodes( nodes: List[N] ) {
			if (isLeaf( nodes.head )) {
				buf ++= prefix
				buf ++= nodes map (n => leafnode(n, id)) mkString " "
			} else {
				buf ++= prefix
				buf ++= nodes map (n => internalnode(n, id, emit)) mkString " "
				buf += '\n'
				printNodes( List.concat(nodes flatMap getBranches) )
			}
		}

		printNodes( List(root) )
		
		if (!afterbuf.isEmpty || after != "")
			buf += '\n'
			
		buf ++= afterbuf.toString
		buf ++= after
		buf toString
	}
	
	/**
	 * Prints (to stdout) a readable representation of the structure and contents of the tree.
	 */
	def prettyPrint = println( prettyStringWithValues )
	
	/**
	 * Prints (to stdout) a readable representation of the structure and contents of the tree, omitting the values and only printing the keys.
	 */
	def prettyPrintKeysOnly = println( prettyString )
	
	/**
	 * Returns a string containing a readable representation of the structure and contents of the tree, omitting the values and only printing the keys. This method is used mainly for unit testing.
	 */
	def prettyString = serialize( "", "", (n, id, _) => "[" + id(n) + ": (" + id(parent(n)) + ") " + id(getBranch(n, 0)) + " " + getKeys(n).zipWithIndex.map({case (k, j) => "| " + k + " | " + id(getBranch(n, j + 1))}).mkString(" ") + "]", (n, id) => "[" + id(n) + ": (" + id(prev(n)) + ", " + id(parent(n)) + ", " + id(getNext(n)) + ")" + (if (nodeLength(n) == 0) "" else " ") + getKeys(n).mkString(" ") + "]", "" )
	
	/**
	 * Returns a string containing a readable representation of the structure and contents of the tree. This method is used mainly for unit testing.
	 */
	def prettyStringWithValues = serialize( "", "", (n, id, _) => "[" + id(n) + ": (" + id(parent(n)) + ") " + id(getBranch(n, 0)) + " " + getKeys(n).zipWithIndex.map({case (k, j) => "| " + k + " | " + id(getBranch(n, j + 1))}).mkString(" ") + "]", (n, id) => "[" + id(n) + ": (" + id(prev(n)) + ", " + id(parent(n)) + ", " + id(getNext(n)) + ")" + (if (nodeLength(n) == 0) "" else " ") + (getKeys(n) zip getValues(n) map (p => "<" + p._1 + ", " + p._2 + ">") mkString " ") + "]", "" )
	
	/**
	 * Creates a PNG image file called `name` (with `.png` added) which visually represents the structure and contents of the tree, only showing the keys. This method uses GraphViz and specifically the `dot` command to produce the diagram.
	 */
	def diagram( name: String ) {
		val before =
			"""	|digraph {
					|    graph [splines=line];
					|    edge [penwidth=2];
					|    node [shape = record, height=.1, penwidth=2, style=filled, fillcolor=white];
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
		
		def leafnode( n: N, id: N => String ) = id(n) + """[label = "<prev> &bull; | """ + (getKeys(n) mkString " | ") + """ | <next> &bull;"];"""
		
		val file = new PrintWriter( name + ".dot" )
		
		file.println( serialize(before, "    ", internalnode, leafnode, "}") )
		file.close
		s"dot -Tpng $name.dot -o $name.png".!
	}
}