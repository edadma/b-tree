package xyz.hyperreal.btree

import collection.mutable.{HashMap, ArrayBuffer}

import java.io.{ByteArrayOutputStream, PrintStream}


abstract class AbstractBPlusTree[K <% Ordered[K], V, N]( order: Int ) {
	protected var root: N
	
	protected def branch( node: N, index: Int ): N
	
	protected def branches( node: N ): Seq[N]
		
	protected def getKey( node: N, index: Int ): K
	
	protected def getValue( node: N, index: Int ): V
	
	protected def insertValue( node: N, index: Int, key: K, value: V ): Unit
	
	protected def insertBranch( node: N, index: Int, key: K, branch: N ): Unit
	
	protected def isLeaf( node: N ): Boolean
	
	protected def keys( node: N ): Seq[K]
	
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
		
	protected def values( node: N ): Seq[V]
		
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
					case index if index >= 0 => _lookup( branch(n, index + 1) )
					case index => _lookup( branch(n, -(index + 1)) )
				}
			
		_lookup( root )
	}
	
	def search( key: K ): Option[V] =
		lookup( key ) match {
			case (true, leaf, index) => Some( getValue(leaf, index) )
			case _ => None
		}
	
	def insert( keys: K* ) {
		for (k <- keys)
			insert( k, null.asInstanceOf[V] )
	}
	
	// def insertIfNotFound
	
	def insert( key: K, value: V ): Boolean = {
		lookup( key ) match {
			case (true, leaf, index) =>
				setValue( leaf, index, value )
				true
			case (false, leaf, index) =>
				if (nodeLength( leaf ) + 1 == order) {
					val newleaf = newLeaf( parent(leaf) )
						
					next( newleaf, next(leaf) )
					
					if (next( leaf ) != nul)
						prev( next(leaf), newleaf )
						
					next( leaf, newleaf )
					prev( newleaf, leaf )
					
					val len = order - 1
					val mid = len/2
					val adjusted =
						if (len%2 == 0 && index < mid)
							mid - 1
						else if (len%2 == 1 && index > mid)
							mid + 1
						else
							mid
							
					moveLeaf( leaf, adjusted, len, newleaf )
						
					if (len%2 == 0)
						if (index < mid)
							insertValue( leaf, index, key, value )
						else
							insertValue( newleaf, index - adjusted, key, value )
					else
						if (index <= mid)
							insertValue( leaf, index, key, value )
						else
							insertValue( newleaf, index - adjusted, key, value )
				
// 						if ((nodeLength(leaf) - nodeLength(newleaf)).abs >= 2)
// 							println("uneven split")
					if (parent( leaf ) == nul) {
						root = newRoot( leaf )
						parent( leaf, root )
						parent( newleaf, root )
						insertBranch( root, 0, keys(newleaf).head, newleaf )
					} else {
						var par = parent( leaf )
						
						binarySearch( par, keys(newleaf).head ) match {
							case index if index >= 0 => sys.error( "key found in internal node" )
							case insertion =>
								val index = -(insertion + 1)
								insertBranch( par, index, keys(newleaf).head, newleaf )
						}
						
						while (nodeLength( par ) == order) {
							val newinternal = newInternal( parent(par) )
							val len = nodeLength( par )
							val mid = len/2
							val middle = getKey( par, mid )
							
							moveInternal( par, mid + 1, len, newinternal )
							
							for (child <- branches( newinternal ))
								parent( child, newinternal )
								
							if (parent( par ) == nul) {
								root = newRoot( par )
								
								parent( newinternal, root )
								parent( par, root )
								insertBranch( root, 0, middle, newinternal )
								par = root
							} else {
								par = parent( par )
								binarySearch( par, middle ) match {
									case index if index >= 0 => sys.error( "key found in internal node" )
									case insertion =>
										val index = -(insertion + 1)
										insertBranch( par, index, middle, newinternal )
								}
							}
						}
					}
				} else
					insertValue( leaf, index, key, value )
				
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
			if (!(keys( n ).dropRight( 1 ) zip keys( n ).drop( 1 ) forall( p => p._1 < p._2 )))
				return "false"
			
			if (parent( n ) != p)
				return "incorrect parent pointer in level " + d
				
			if (isLeaf( n )) {
				if (depth == -1)
					depth = d
				else if (d != depth)
					return "false"
			
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
				if (branches( n ) exists (p => p == nul))
					return "null branch pointer"
					
				if (keys( branch(n, 0) ) isEmpty)
					return "empty internal node"
					
				if (keys( n ) isEmpty)
					return "empty internal node"
					
				if (keys( branch(n, 0) ).last >= keys( n ).head)
					return "left internal node branch not strictly less than"
					
				if (!(keys( n ) drop 1 zip branches( n ) drop 1 forall (p => keys( p._2 ).head < p._1)))
					return "right internal node branch not greater than or equal"
				
				for (b <- branches( n ))
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
					nodes ++= branches( n )
				
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
					(if (withValues) (keys(n) zip values(n)) map (p => "<" + p._1 + ", " + p._2 + ">") mkString " " else keys(n) mkString " ") + "]") mkString " " )
				s.print( after )
			} else {
				for ((n, i) <- nodes zipWithIndex) {
					s.print( "[" + id(n) + ": (" + id(parent(n)) + ") " + id(branch(n, 0)) )
					
					for ((k, i) <- keys( n ) zipWithIndex)
						s.print( " | " + k + " | " + id(branch(n, i + 1)) )
				
					s.print( "]" )
					
					if (i < nodes.size - 1)
						s.print( " " )
				}
				
				val ns = nodes.toList
				
				nodes.clear
				
				for (n <- ns)
					nodes ++= branches( n )
				
				s.println
				printNodes
			}
		}

		nodes += root
		printNodes
		bytes toString
	}
}