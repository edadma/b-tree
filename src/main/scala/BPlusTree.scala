package xyz.hyperreal.btree

import collection.mutable.{HashMap, ArrayBuffer}
import scala.collection.Searching._

import java.io.{ByteArrayOutputStream, PrintStream}


class BPlusTree[K <% Ordered[K], V, N]( order: Int ) {
	protected var root: N
	
	protected val comparator = (elem: K, target: K) => elem compare target
	
	protected def nul: N
		
	protected def isLeaf( n: N ): Boolean
	
	protected def newLeaf( par: N ): N
		
	protected def getKey( n: N, index: Int ): K
	
	protected def getValue( n: N, index: Int ): V
	
	protected def length( n: N ): Int
		
	protected def branch( n: N, index: Int ): N
		
	protected def set( n: N, index: Int, v: V ): Unit
	
	protected def parent( n: N ): N
	
	protected def prev( n: N ): N
	
	protected def next( n: N ): N
	
	protected def parent( n: N, p: N ): N
	
	protected def prev( n: N, p: N ): N
	
	protected def next( n: N, p: N ): N
	
	protected def moveLeaf( src: N, begin: Int, end: Int, dst: N ): Unit
	
	protected def insert( n: N, index: Int, key: K, value: V ): Unit
	
	protected def insert( n: N, index: Int, key: K, b: N ): Unit
	
	protected def newInternal( par: N ): N
		
	protected def keys( n: N ): List[K]
		
	protected def values( n: N ): List[V]
		
	protected def branches( n: N ): List[N]
	
	protected def first( n: N, b: N ): Unit
	
	protected def newRoot( b: N ): N
		
	private [btree] def binarySearch( n: N, target: K ): Int = {
		def search( start: Int, end: Int ): Int = {
			if (start > end)
				-start - 1
			else {
				val mid = start + (end-start + 1)/2
				
				if (comparator( getKey(n, mid), target ) == 0)
					mid
				else if (comparator( getKey(n, mid), target ) > 0)
					search( start, mid - 1 )
				else
					search( mid + 1, end )
			}
		}
		
		search( 0, length(n) - 1 )
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
				set( leaf, index, value )
				true
			case (false, leaf, index) =>
				if (length( leaf ) + 1 == order) {
					val newleaf = newLeaf( parent(leaf) )
						
					next( newleaf, next(leaf) )
					
					if (next( leaf ) != nul)
						prev( next(leaf), newleaf )
						
					next( leaf, newleaf )
					prev( newleaf, leaf )
					
					val len = length( leaf )
					val mid = len/2
					val nlen = len - mid
					
					moveLeaf( leaf, mid, len, newleaf )
// 					leaf.keys.view( len/2, len ) copyToBuffer newleaf.keys
// 					leaf.keys.remove( len/2, nlen )
// 					leaf.values.view( len/2, len ) copyToBuffer newleaf.values
// 					leaf.values.remove( len/2, nlen )
		
					if (index < mid)
						insert( leaf, index, key, value )
					else
						insert( newleaf, index - mid, key, value )
					
// 				leaf.keys.insert( index, key )
// 				leaf.values.insert( index, value )
				
					if (parent( leaf ) != nul) {
						root = newRoot( leaf )
						parent( leaf, root )
						insert( root, 0, keys(newleaf).head, newleaf )
					} else {
						var par = parent( leaf )
						
						binarySearch( par, keys(newleaf).head ) match {
							case index if index >= 0 => sys.error( "key found in internal node" )
							case insertion =>
								val index = -(insertion + 1)
								insert( par, index, keys(newleaf).head, newleaf )
// 								par.keys.insert( index, newleaf.keys.head )
// 								par.branches.insert( index + 1, newleaf )
						}
						
						while (length( par ) == order) {
							val newinternal = newInternal( parent(par) )
							val mid = length( par )/2
							val middle = getKey( par, mid )
							
							par.keys.view( mid + 1, length(par) ) copyToBuffer newinternal.keys
							
							val brindex = par.length/2 + 1
							val brcount = newinternal.length + 1

							par.keys.remove( par.length/2, newinternal.length + 1 )
							par.branches.view( brindex, brindex + brcount ) copyToBuffer newinternal.branches
							par.branches.remove( brindex, brcount )
							
							for (child <- branches( newinternal ))
								parent( child, newinternal )
								
							if (parent( par ) == nul) {
								root = newRoot( par )
								
								parent( newinternal, root )
								parent( par, root )
								insert( newroot, 0, middle, newinternal )
								par = newroot
							} else {
								par = parent( par )
								binarySearch( par, middle ) match {
									case index if index >= 0 => sys.error( "key found in internal node" )
									case insertion =>
										val index = -(insertion + 1)
										insert( par, index, middle, newinternal )
// 										par.keys.insert( index, middle )
// 										par.branches.insert( index + 1, newinternal )
								}
							}
						}
					}
				} else
					insert( leaf, index, key, value )
				
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
				return "false"
				
			if (isLeaf( n )) {
				if (depth == -1)
					depth = d
				else if (d != depth)
					return "false"
			
				if (prevnode != prev( n ))
					return "prev pointer incorrect"
				else
					prevnode = n
					
				if ((nextptr != nul) && (nextptr != n))
					return "next pointer incorrect"
				else
					nextptr = next( n )
			}
			else {
				if (keys( branch(n, 0) ).last >= keys( n ).head)
					return "left internal node branch"
					
				if (!(keys( n ) drop 1 zip branches( n ) drop 1 forall (p => keys( p._2 ).head < p._1)))
					return "right internal node branch"
				
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
				s.print( nodes map (n => "[" + id(n) + ": (" + id(prev(n)) + ", " + id(parent(n)) + ", " + id(next(n)) + ")" + (if (length(n) == 0) "" else " ") +
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

abstract class Node[K, V] {
	var parent: InternalNode[K, V]
	val keys = new ArrayBuffer[K]
	
	def length = keys.size
	
	def isLeaf: Boolean
	
	def asInternal = asInstanceOf[InternalNode[K, V]]
	
	def asLeaf = asInstanceOf[LeafNode[K, V]]
}

class InternalNode[K, V]( var parent: InternalNode[K, V] ) extends Node[K, V] {
	val isLeaf = false
	val branches = new ArrayBuffer[Node[K, V]]
}

class LeafNode[K, V]( var parent: InternalNode[K, V] ) extends Node[K, V] {
	val isLeaf = true
	val values = new ArrayBuffer[V]
	var prev: LeafNode[K, V] = null
	var next: LeafNode[K, V] = null
}