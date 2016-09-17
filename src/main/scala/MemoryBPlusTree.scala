package xyz.hyperreal.btree

import collection.mutable.ArrayBuffer
import util.matching.Regex.Match


class MemoryBPlusTree[K <% Ordered[K], V]( order: Int ) extends AbstractBPlusTree[K, V]( order ) {
	protected type N = Node[K, V]
	
	protected var first: Node[K, V] = new LeafNode[K, V]( null )
	protected var last: Node[K, V] = first
	protected var root: Node[K, V] = first
	protected var lastlen = 0
	
  protected def addBranch( node: Node[K, V], branch: Node[K,V] ) = node.asInternal.branches += branch
		
	protected def addKey( node: Node[K, V], key: K ) = node.keys += key
	
	protected def addValue[V1 >: V]( node: Node[K, V], value: V1 ) = node.asInstanceOf[Node[K, V1]].asLeaf.values += value

	protected def freeNode( node: Node[K, V] ) {}

	protected def getBranch( n: Node[K, V], index: Int ) = n.asInternal.branches( index )
	
	protected def getBranches( n: Node[K, V] ) = n.asInternal.branches
	
	protected def getKey( n: Node[K, V], index: Int ) = n.keys( index )
	
	protected def getKeys( node: Node[K, V] ) = node.keys
	
	protected def getNext( node: Node[K, V] ) = node.asLeaf.next
	
	protected def getParent( node: Node[K, V] ) = node.parent
	
	protected def getPrev( node: Node[K, V] ) = node.asLeaf.prev
	
	protected def getValue( n: Node[K, V], index: Int ) = n.asLeaf.values( index )
		
	protected def getValues( node: Node[K, V] ) = node.asLeaf.values
	
	protected def insertInternal( n: Node[K, V], index: Int, key: K, branch: Node[K, V] ) {
		n.keys.insert( index, key )
		n.asInternal.branches.insert( index + 1, branch )
	}
	
	protected def insertLeaf[V1 >: V]( n: Node[K, V], index: Int, key: K, value: V1 ) {
		n.keys.insert( index, key )
		n.asInstanceOf[Node[K, V1]].asLeaf.values.insert( index, value )
	}
	
	protected def isLeaf( node: Node[K, V] ) = node.isLeaf
	
	protected def nodeLength( node: Node[K, V] ) = node.keys.length
	
	protected def moveInternal( src: Node[K, V], begin: Int, end: Int, dst: Node[K, V] ) {
		src.keys.view( begin, end ) copyToBuffer dst.keys
		src.keys.remove( begin - 1, end - begin + 1 )
		src.asInternal.branches.view( begin, end + 1 ) copyToBuffer dst.asInternal.branches
		src.asInternal.branches.remove( begin, end - begin + 1 )
	}
	
	protected def moveLeaf( src: Node[K, V], begin: Int, end: Int, dst: Node[K, V], index: Int ) {
		dst.keys.insertAll( index, src.keys.view(begin, end) ) 
		src.keys.remove( begin, end - begin )
		dst.asLeaf.values.insertAll( index, src.asLeaf.values.view(begin, end) )
		src.asLeaf.values.remove( begin, end - begin )
	}
	
	protected def newInternal( parent: Node[K, V] ) = new InternalNode( parent.asInstanceOf[InternalNode[K, V]] )
	
	protected def newLeaf( parent: Node[K, V] ) = new LeafNode( parent.asInstanceOf[InternalNode[K, V]] )
	
	protected def newRoot( branch: Node[K, V] ) = {
		val res = new InternalNode[K, V]( null )
		
		res.branches += branch
		res
	}
	
	protected def nul = null

	protected def removeInternal( node: Node[K, V], index: Int ) = {
		node.keys.remove( index, 1 )
		node.asInternal.branches.remove( index + 1, 1 )
		node.length
	}

	protected def removeLeaf( node: Node[K, V], index: Int ) = {
		node.keys.remove( index, 1 )
		node.asLeaf.values.remove( index, 1 )
		node.length
	}
	
	protected def setFirst( leaf: Node[K, V] ) {}
	
	protected def setKey( node: Node[K, V], index: Int, key: K ) = node.keys( index ) = key

	protected def setLast( leaf: Node[K, V] ) {}
	
	protected def setNext( node: Node[K, V], p: Node[K, V] ) = node.asLeaf.next = p.asInstanceOf[LeafNode[K, V]]
	
	protected def setParent( node: Node[K, V], p: Node[K, V] ) = node.parent = p.asInstanceOf[InternalNode[K, V]]
	
	protected def setPrev( node: Node[K, V], p: Node[K, V] ) = node.asLeaf.prev = p.asInstanceOf[LeafNode[K, V]]
		
	protected def setRoot( node: Node[K, V] ) {}
	
	protected def setValue[V1 >: V]( node: Node[K, V], index: Int, v: V1 ) = node.asInstanceOf[Node[K, V1]].asLeaf.values(index) = v
	
	/**
	 * Returns a B+ Tree build from a string representation of the tree. The syntax of the input string is simple: internal nodes are coded as lists of nodes alternating with keys (alpha strings with no quotation marks) using parentheses with elements separated by space, leaf nodes are coded as lists of alpha strings (no quotation marks) using brackets with elements separated by space.
	 * 
	 * @example
	 * 
	 * {{{
	 * (
	 *   [g] j [j t] u [u v]
	 * )
	 * }}}
	 * 
	 * produces a tree that pretty prints as
	 * 
	 * {{{
	 * [n0: (null) n1 | j | n2 | u | n3]
	 * [n1: (null, n0, n2) g] [n2: (n1, n0, n3) j t] [n3: (n2, n0, null) u v]
	 * }}}
	 */
	def build( s: String ) = {
		val it = """[a-z]+|\n|.""".r.findAllMatchIn(s) filterNot (m => m.matched.head.isWhitespace)
		var prev: LeafNode[K, V] = null
		
		def internal( it: Iterator[Match], node: InternalNode[K, V] ): Node[K, V] =
			it.next.matched match {
				case "(" =>
					assert( node.keys.length == node.branches.length, "illegal internal node" )
					node.branches += internal( it, new InternalNode[K, V](node) )
					internal( it, node )
				case "[" =>
					assert( node.keys.length == node.branches.length, "illegal leaf node" )
					node.branches += leaf( it, new LeafNode[K, V](node) )
					internal( it, node )
				case ")" => node
				case key if key.head.isLetter =>
					assert( node.keys.length == node.branches.length - 1, "illegal key" )
					node.keys += key.asInstanceOf[K]
					internal( it, node )
				case t => sys.error( "unexpected token: " + t )
			}
			
		def leaf( it: Iterator[Match], node: LeafNode[K, V] ): LeafNode[K, V] =
			it.next.matched match {
				case "]" =>
					last = node
					lastlen = node.length
					
					if (prev != nul)
						prev.next = node
						
					node.prev = prev
					prev = node
					node
				case key if key.head.isLetter =>
					node.keys += key.asInstanceOf[K]
					leaf( it, node )
				case t => sys.error( "unexpected token: " + t )
			}
		
		first = null
		root =
			it.next.matched match {
				case "(" => internal( it, new InternalNode[K, V](null) )
				case "[" => leaf( it, new LeafNode[K, V](null) )
				case t => sys.error( "unexpected token: " + t.head.toInt )
			}
		this
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
	
	override def toString = keys.mkString( "[keys: ", ", ", "| ") + values.mkString( "values: ", ", ", "]" )
}