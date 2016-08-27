package xyz.hyperreal.btree

import collection.mutable.{HashMap, ArrayBuffer}
import scala.collection.Searching._

import java.io.{ByteArrayOutputStream, PrintStream}


class InMemoryBPlusTree[K <% Ordered[K], V]( order: Int, elems: (K, V)* ) extends BPlusTree[K, V]( order, elems:_* ) {
	
}

abstract class BPlusTree[K <% Ordered[K], V]( order: Int, elems: (K, V)* ) {
	private var root: Node = new LeafNode( null )
	
	private [btree] val leafNodeComparator = (elem: Pair, target: K) => elem.key compare target
	
	private [btree] val internalNodeComparator = (elem: K, target: K) => elem compare target
	
	for ((k, v) <- elems)
		insert( k, v )
		
	private [btree] def lookup( key: K ): (Boolean, LeafNode, Int) = {
		def _lookup( n: Node ): (Boolean, LeafNode, Int) =
			n match {
				case node: InternalNode =>
					binarySearch( node.keys, key, internalNodeComparator ) match {
						case index if index >= 0 => _lookup( node.branches(index + 1) )
						case index => _lookup( node.branches(-(index + 1)) )
					}
				case node: LeafNode =>
					binarySearch( node.values, key, leafNodeComparator ) match {
						case index if index >= 0 => (true, node, index)
						case index => (false, node, -(index + 1))
					}
			}
			
		_lookup( root )
	}
	
	def search( key: K ): Option[V] =
		lookup( key ) match {
			case (true, leaf, index) => Some( leaf.values(index).value )
			case _ => None
		}
	
	def insert( key: K ): Boolean = insert( key, null.asInstanceOf[V] )
	
	def insert( key: K, value: V ): Boolean = {
		lookup( key ) match {
			case (true, leaf, index) =>
				leaf.values(index).value = value
				true
			case (false, leaf, index) =>
				def splitLeafNode = {
					val newleaf = new LeafNode( leaf.parent )
					
					newleaf.next = leaf.next
					leaf.next = newleaf
					newleaf.prev = leaf
					leaf.values.view( leaf.values.size/2, leaf.values.size ) copyToBuffer newleaf.values
					leaf.values.remove( leaf.values.size/2, newleaf.values.size )
					newleaf
				}
		
				leaf.values.insert( index, Pair(key, value) )
				
				if (leaf.values.size == order) {
					if (leaf.parent eq null) {
						val newroot = new InternalNode( null )
						
						leaf.parent = newroot
						
						val newleaf = splitLeafNode
						
						newroot.keys += newleaf.values.head.key
						newroot.branches += leaf
						newroot.branches += newleaf
						root = newroot
					} else {
						var parent = leaf.parent
						val newleaf = splitLeafNode
						
						binarySearch( parent.keys, newleaf.values.head.key, internalNodeComparator ) match {
							case index if index >= 0 => sys.error( "key found in internal node" )
							case insertion =>
								val index = -(insertion + 1)
								parent.keys.insert( index, newleaf.values.head.key )
								parent.branches.insert( index + 1, newleaf )
						}
						
						while (parent.keys.size == order) {
							val newinternal = new InternalNode( parent.parent )
							val middle = parent.keys(parent.keys.size/2)
							
							parent.keys.view( parent.keys.size/2 + 1, parent.keys.size ) copyToBuffer newinternal.keys
							
							val brindex = parent.keys.size/2 + 1
							val brcount = newinternal.keys.size + 1

							parent.keys.remove( parent.keys.size/2, newinternal.keys.size + 1 )
							parent.branches.view( brindex, brindex + brcount ) copyToBuffer newinternal.branches
							parent.branches.remove( brindex, brcount )
							
							for (child <- newinternal.branches)
								child.parent = newinternal
								
							if (parent.parent eq null) {
								val newroot = new InternalNode( null )
								
								newinternal.parent = newroot
								parent.parent = newroot
								newroot.keys += middle
								newroot.branches += parent
								newroot.branches += newinternal
								root = newroot
								parent = newroot
							} else {
								parent = parent.parent
								binarySearch( parent.keys, middle, internalNodeComparator ) match {
									case index if index >= 0 => sys.error( "key found in internal node" )
									case insertion =>
										val index = -(insertion + 1)
										parent.keys.insert( index, middle )
										parent.branches.insert( index + 1, newinternal )
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
	
	def prettyPrint = println( serialize("", true) )
	
	def prettyPrintKeysOnly = println( serialize("", false) )
	
	def prettyString = serialize( "", false )
	
	def prettyStringWithValues = serialize( "", true )
	
	def serialize( after: String, withValues: Boolean ) = {
		val bytes = new ByteArrayOutputStream
		val s = new PrintStream(bytes)
		val map = new HashMap[Node, String]
		val nodes = new ArrayBuffer[Node]
		var count = 0
		
		def id( node: Node ) =
			if (node eq null)
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
			if (nodes.head isLeaf) {
				s.print( nodes map (n => "[" + id(n) + ": (" + id(n.parent) + ")" + (if (n.asLeaf.values isEmpty) "" else " ") +
					(if (withValues) n.asLeaf.values.mkString(" ") else n.asLeaf.values map (p => p.key) mkString " ") + "]") mkString " " )
				s.print( after )
			} else {
				for ((n, i) <- nodes zipWithIndex) {
					s.print( "[" + id(n) + ": (" + id(n.parent) + ") " + id(n.asInternal.branches.head) )
					
					for ((k, i) <- n.asInternal.keys zipWithIndex)
						s.print( " | " + k + " | " + id(n.asInternal.branches(i + 1)) )
				
					s.print( "]" )
					
					if (i < nodes.size - 1)
						s.print( " " )
				}
				
				val ns = nodes.toList
				
				nodes.clear
				
				for (n <- ns)
					nodes ++= n.asInternal.branches
				
				s.println
				printNodes
			}
		}

		nodes += root
		printNodes
		bytes toString
	}

	private object address {
		private val obj = ".*@(.*)"r
		
		def apply( node: Node ) = String.valueOf( node ) match {
			case obj( addr ) => addr
			case n => n
		}
	}
	
	def display {
		val nodes = new ArrayBuffer[Node]
		
		def printNodes {
			if (nodes.head isLeaf) {
				for (n <- nodes)
					print( "[" + address(n) + ": (" + address(n.parent) + ") " + n.asLeaf.values.mkString(" ") + "] " )
				
				println
			} else {
				for (n <- nodes) {
					print( "[" + address(n) + ": (" + address(n.parent) + ") " + address(n.asInternal.branches.head) )
					
					for ((k, i) <- n.asInternal.keys zipWithIndex) {
						print( " | " + k + " | " + address(n.asInternal.branches(i + 1)) )
					}
				
					print( "] " )
				}
				
				val ns = nodes.toList
				
				nodes.clear
				
				for (n <- ns)
					nodes ++= n.asInternal.branches
				
				println
				printNodes
			}
		}

		nodes += root
		printNodes
	}
	
	private def pointer( level: Int, index: Int ) = (if (level > 9 || index > 9) "%02d%02d" else "%01d%01d").format( level, index )
	
	def show {
		val nodes = new ArrayBuffer[Node]
		var level = 0
		
		def printNodes {
			if (nodes.head isLeaf) {
				for ((n, i) <- nodes zipWithIndex)
					print( "[" + pointer(level, i) + ": " + n.asLeaf.values.mkString(" ") + "] " )
				
				println
			} else {
				var branches = 0
				
				for ((n, i) <- nodes zipWithIndex) {
					print( "[" + pointer(level, i) + ": " + pointer(level + 1, branches) )
					branches += 1
					
					for (k <- n.asInternal.keys) {
						print( " | " + k + " | " + pointer(level + 1, branches) )
						branches += 1
					}
				
					print( "] " )
				}
				
				val ns = nodes.toList
				
				nodes.clear
				
				for (n <- ns)
					nodes ++= n.asInternal.branches
				
				level += 1
				println
				printNodes
			}
		}

		nodes += root
		printNodes
	}
	
	private [btree] case class Pair( key: K, var value: V ) {
		override def toString = "<" + key + ", " + value + ">"
	}
	
	private [btree] abstract class Node {
		var parent: InternalNode
		
		def isLeaf: Boolean
		
		def asInternal = asInstanceOf[InternalNode]
		
		def asLeaf = asInstanceOf[LeafNode]
	}
	
	private [btree] class InternalNode( var parent: InternalNode ) extends Node {
		val isLeaf = false
		val keys = new ArrayBuffer[K]
		val branches = new ArrayBuffer[Node]
	}
	
	private [btree] class LeafNode( var parent: InternalNode ) extends Node {
		val isLeaf = true
		val values = new ArrayBuffer[Pair]
		var prev: LeafNode = null
		var next: LeafNode = null
	}
}