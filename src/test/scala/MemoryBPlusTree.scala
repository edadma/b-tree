package xyz.hyperreal.btree

import collection.mutable.ArrayBuffer


class MemoryBPlusTree[K <% Ordered[K], V]( order: Int ) extends AbstractBPlusTree[K, V, Node[K, V]]( order ) {
	protected var root: Node[K, V] = new LeafNode[K, V]( null )
	
	def getBranch( n: Node[K, V], index: Int ) = n.asInternal.branches( index )
	
	def getBranches( n: Node[K, V] ) = n.asInternal.branches
	
	def getKey( n: Node[K, V], index: Int ) = n.keys( index )
	
	def getKeys( node: Node[K, V] ) = node.keys
	
	def getValue( n: Node[K, V], index: Int ) = n.asLeaf.values( index )
		
	def getValues( node: Node[K, V] ) = node.asLeaf.values
	
	def insertInternal( n: Node[K, V], index: Int, key: K, branch: Node[K, V] ) {
		n.keys.insert( index, key )
		n.asInternal.branches.insert( index + 1, branch )
	}
	
	def insertLeaf( n: Node[K, V], index: Int, key: K, value: V ) {
		n.keys.insert( index, key )
		n.asLeaf.values.insert( index, value )
	}
	
	def isLeaf( node: Node[K, V] ) = node.isLeaf
	
	def nodeLength( node: Node[K, V] ) = node.keys.length
	
	def moveInternal( src: Node[K, V], begin: Int, end: Int, dst: Node[K, V] ) {
		src.keys.view( begin, end ) copyToBuffer dst.keys
		src.keys.remove( begin - 1, end - begin + 1 )
		src.asInternal.branches.view( begin, end + 1 ) copyToBuffer dst.asInternal.branches
		src.asInternal.branches.remove( begin, end - begin + 1 )
	}
	
	def moveLeaf( src: Node[K, V], begin: Int, end: Int, dst: Node[K, V] ) {
		src.keys.view( begin, end ) copyToBuffer dst.keys
		src.keys.remove( begin, end - begin )
		src.asLeaf.values.view( begin, end ) copyToBuffer dst.asLeaf.values
		src.asLeaf.values.remove( begin, end - begin )
	}
	
	def newInternal( parent: Node[K, V] ) = new InternalNode( parent.asInstanceOf[InternalNode[K, V]] )
	
	def newLeaf( parent: Node[K, V] ) = new LeafNode( parent.asInstanceOf[InternalNode[K, V]] )
	
	def newRoot( branch: Node[K, V] ) = {
		val res = new InternalNode[K, V]( null )
		
		res.branches += branch
		res
	}
	
	def next( node: Node[K, V], p: Node[K, V] ) = node.asLeaf.next = p.asInstanceOf[LeafNode[K, V]]
	
	def next( node: Node[K, V] ) = node.asLeaf.next
	
	def nul = null
	
	def parent( node: Node[K, V], p: Node[K, V] ) = node.parent = p.asInstanceOf[InternalNode[K, V]]
	
	def parent( node: Node[K, V] ) = node.parent
	
	def prev( node: Node[K, V], p: Node[K, V] ) = node.asLeaf.prev = p.asInstanceOf[LeafNode[K, V]]
	
	def prev( node: Node[K, V] ) = node.asLeaf.prev
		
	def setValue( node: Node[K, V], index: Int, v: V ) = node.asLeaf.values(index) = v
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
	
	override def toString = keys.mkString( "keys: ", ", ", "| ") + values.mkString( "values: ", ", ", "" )
}