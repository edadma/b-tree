package xyz.hyperreal.btree

import collection.mutable.{HashMap, ArrayBuffer}

import java.io.{ByteArrayOutputStream, PrintStream}


abstract class AbstractBPlusTree[K <% Ordered[K], V, N]( order: Int ) {
	protected var root: N
	
	protected def getBranch( node: N, index: Int ): N
	
	protected def getBranches( node: N ): Seq[N]
		
	protected def getKey( node: N, index: Int ): K
	
	protected def getKeys( node: N ): Seq[K]
	
	protected def getValue( node: N, index: Int ): V
	
	protected def getValues( node: N ): Seq[V]
	
	protected def insertInternal( node: N, index: Int, key: K, branch: N ): Unit
	
	protected def insertLeaf( node: N, index: Int, key: K, value: V ): Unit
	
	protected def isLeaf( node: N ): Boolean
	
	protected def moveInternal( src: N, begin: Int, end: Int, dst: N ): Unit
	
	protected def moveLeaf( src: N, begin: Int, end: Int, dst: N ): Unit
	
	protected def newInternal( parent: N ): N
	
	protected def newLeaf( parent: N ): N
	
	protected def newRoot( branch: N ): N
	
	protected def next( node: N, p: N ): Unit
	
	protected def next( node: N ): N
	
	protected def nodeLength( node: N ): Int
	
	protected def nul: N
	
	protected def parent( node: N, p: N ): Unit
	
	protected def parent( node: N ): N
	
	protected def prev( node: N, p: N ): Unit
	
	protected def prev( node: N ): N
		
	protected def setValue( node: N, index: Int, v: V ): Unit
		
	private [btree] def binarySearch( node: N, target: K ): Int = {
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
	
	private [btree] def lookup( key: K ): (Boolean, N, Int) = {
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
	
	def search( key: K ): Option[V] =
		lookup( key ) match {
			case (true, leaf, index) => Some( getValue(leaf, index) )
			case _ => None
		}
	
	def insertKeys( keys: K* ) {
		for (k <- keys)
			insert( k, null.asInstanceOf[V] )
	}
	
	// def insertIfNotFound
	
	def insert( key: K, value: V = null.asInstanceOf[V] ): Boolean = {
		lookup( key ) match {
			case (true, leaf, index) =>
				setValue( leaf, index, value )
				true
			case (false, leaf, index) =>
				def split = {
					val newleaf = newLeaf( parent(leaf) )
					
					next( newleaf, next(leaf) )
					
					if (next( leaf ) != nul)
						prev( next(leaf), newleaf )
						
					next( leaf, newleaf )
					prev( newleaf, leaf )
					
					val len = nodeLength( leaf )
					val mid = len/2
					
					moveLeaf( leaf, mid, len, newleaf )
					newleaf
				}
				
				insertLeaf( leaf, index, key, value )
				
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
				
				false
		}
	}
	
	def delete( key: K ) = {
		
	}
	
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
			
				if (prevnode != prev( n ))
					return "incorrect prev pointer"
				else
					prevnode = n
					
				if ((nextptr != nul) && (nextptr != n))
					return "incorrect next pointer"
				else
					nextptr = next( n )
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
	
	def prettySearch( key: K ) = {
		val nodes = new ArrayBuffer[N]
		val map = new HashMap[N, String]
		var count = 0
		
		def traverse {
			for (n <- nodes) {
				map(n) = "n" + count
				count += 1
			}
			
			if (!isLeaf( nodes.head )) {
				val ns = nodes.toList
				
				nodes.clear
				
				for (n <- ns)
					nodes ++= getBranches( n )
				
				traverse
			}
		}
		
		nodes += root
		traverse
		lookup( key ) match {
			case (true, leaf, index) => map(leaf) + " " + getValue( leaf, index ) + " " + index
			case _ => "not found"
		}
	}
	
	def prettyPrint = println( prettyStringWithValues )
	
	def prettyPrintKeysOnly = println( prettyString )
	
	def prettyString = serialize( "", false )
	
	def prettyStringWithValues = serialize( "", true )
	
	def serialize( after: String, withValues: Boolean ) = {
		val bytes = new ByteArrayOutputStream
		val s = new PrintStream( bytes )
		val map = new HashMap[N, String]
		val nodes = new ArrayBuffer[N]
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
		
		def printNodes {
			if (isLeaf( nodes.head )) {
				s.print( nodes map (n => "[" + id(n) + ": (" + id(prev(n)) + ", " + id(parent(n)) + ", " + id(next(n)) + ")" + (if (nodeLength(n) == 0) "" else " ") +
					(if (withValues) (getKeys(n) zip getValues(n)) map (p => "<" + p._1 + ", " + p._2 + ">") mkString " " else getKeys(n) mkString " ") + "]") mkString " " )
				s.print( after )
			} else {
				for ((n, i) <- nodes zipWithIndex) {
					s.print( "[" + id(n) + ": (" + id(parent(n)) + ") " + id(getBranch(n, 0)) )
					
					for ((k, i) <- getKeys( n ) zipWithIndex)
						s.print( " | " + k + " | " + id(getBranch(n, i + 1)) )
				
					s.print( "]" )
					
					if (i < nodes.size - 1)
						s.print( " " )
				}
				
				val ns = nodes.toList
				
				nodes.clear
				
				for (n <- ns)
					nodes ++= getBranches( n )
				
				s.println
				printNodes
			}
		}

		nodes += root
		printNodes
		bytes toString
	}
}