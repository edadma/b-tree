package xyz.hyperreal.btree

import collection.mutable.{HashMap, ArrayBuffer}
import scala.collection.Searching._

import java.io.{ByteArrayOutputStream, PrintStream}


class BPlusTree[K <% Ordered[K], V]( order: Int, elems: (K, V)* ) {
	private var root: Node = new LeafNode( null )
	
	private [btree] val keyComparator = (elem: K, target: K) => elem compare target
	
	for ((k, v) <- elems)
		insert( k, v )
		
	private [btree] def lookup( key: K ): (Boolean, LeafNode, Int) = {
		def _lookup( n: Node ): (Boolean, LeafNode, Int) =
			n match {
				case node: InternalNode =>
					binarySearch( node.keys, key, keyComparator ) match {
						case index if index >= 0 => _lookup( node.branches(index + 1) )
						case index => _lookup( node.branches(-(index + 1)) )
					}
				case node: LeafNode =>
					binarySearch( node.keys, key, keyComparator ) match {
						case index if index >= 0 => (true, node, index)
						case index => (false, node, -(index + 1))
					}
			}
			
		_lookup( root )
	}
	
	def search( key: K ): Option[V] =
		lookup( key ) match {
			case (true, leaf, index) => Some( leaf.values(index) )
			case _ => None
		}
	
	def insert( key: K ): Boolean = insert( key, null.asInstanceOf[V] )
	
	def insert( key: K, value: V ): Boolean = {
		lookup( key ) match {
			case (true, leaf, index) =>
				leaf.values(index) = value
				true
			case (false, leaf, index) =>
				def splitLeafNode = {
					val newleaf = new LeafNode( leaf.parent )
					
					newleaf.next = leaf.next
					
					if (leaf.next ne null)
						leaf.next.prev = newleaf
						
					leaf.next = newleaf
					newleaf.prev = leaf
					leaf.keys.view( leaf.length/2, leaf.length ) copyToBuffer newleaf.keys
					leaf.keys.remove( leaf.length/2, newleaf.length )
					leaf.values.view( leaf.values.size/2, leaf.values.size ) copyToBuffer newleaf.values
					leaf.values.remove( leaf.values.size/2, newleaf.values.size )
					newleaf
				}
		
				leaf.keys.insert( index, key )
				leaf.values.insert( index, value )
				
				if (leaf.values.size == order) {
					if (leaf.parent eq null) {
						val newroot = new InternalNode( null )
						
						leaf.parent = newroot
						
						val newleaf = splitLeafNode
						
						newroot.keys += newleaf.keys.head
						newroot.branches += leaf
						newroot.branches += newleaf
						root = newroot
					} else {
						var parent = leaf.parent
						val newleaf = splitLeafNode
						
						binarySearch( parent.keys, newleaf.keys.head, keyComparator ) match {
							case index if index >= 0 => sys.error( "key found in internal node" )
							case insertion =>
								val index = -(insertion + 1)
								parent.keys.insert( index, newleaf.keys.head )
								parent.branches.insert( index + 1, newleaf )
						}
						
						while (parent.length == order) {
							val newinternal = new InternalNode( parent.parent )
							val middle = parent.keys(parent.length/2)
							
							parent.keys.view( parent.length/2 + 1, parent.length ) copyToBuffer newinternal.keys
							
							val brindex = parent.length/2 + 1
							val brcount = newinternal.length + 1

							parent.keys.remove( parent.length/2, newinternal.length + 1 )
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
								binarySearch( parent.keys, middle, keyComparator ) match {
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
	
	def wellConstructed: String = {
		val nodes = new ArrayBuffer[Node]
		var depth = -1
		var prev: LeafNode = null
		var nextptr: LeafNode = null
		
		def check( n: Node, p: Node, d: Int ): String = {
			if (!(n.keys.dropRight( 1 ) zip n.keys.drop( 1 ) forall( p => p._1 < p._2 )))
				return "false"
			
			if (n.parent ne p)
				return "false"
				
			if (n isLeaf) {
				if (depth == -1)
					depth = d
				else if (d != depth)
					return "false"
			
				if (prev ne n.asLeaf.prev)
					return "prev pointer incorrect"
				else
					prev = n.asLeaf
					
				if ((nextptr ne null) && (nextptr ne n))
					return "next pointer incorrect"
				else
					nextptr = n.asLeaf.next
			}
			else {
				if (n.asInternal.branches.head.keys.last >= n.keys.head)
					return "left internal node branch"
					
				if (!(n.keys drop 1 zip n.asInternal.branches drop 1 forall (p => p._2.keys.head < p._1)))
					return "right internal node branch"
				
				for (b <- n.asInternal.branches)
					check( b, n, d + 1 ) match {
						case "true" =>
						case error => return error
					}
			}
			
			"true"
		}
		
		check( root, null, 0 ) match {
			case "true" =>
			case error => return error
		}
			
		if (nextptr ne null)
			return "rightmost next pointer not null"
			
		"true"
	}
	
	def prettyPrint = println( prettyStringWithValues )
	
	def prettyPrintKeysOnly = println( prettyString )
	
	def prettyString = serialize( "", false )
	
	def prettyStringWithValues = serialize( "", true )
	
	def serialize( after: String, withValues: Boolean ) = {
		val bytes = new ByteArrayOutputStream
		val s = new PrintStream( bytes )
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
				s.print( nodes map (n => "[" + id(n) + ": (" + id(n.asLeaf.prev) + ", " + id(n.parent) + ", " + id(n.asLeaf.next) + ")" + (if (n.asLeaf.values isEmpty) "" else " ") +
					(if (withValues) (n.keys zip n.asLeaf.values) map (p => "<" + p._1 + ", " + p._2 + ">") mkString " " else n.keys mkString " ") + "]") mkString " " )
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
	
	private [btree] abstract class Node {
		var parent: InternalNode
		val keys = new ArrayBuffer[K]
		
		def length = keys.size
		
		def isLeaf: Boolean
		
		def asInternal = asInstanceOf[InternalNode]
		
		def asLeaf = asInstanceOf[LeafNode]
	}
	
	private [btree] class InternalNode( var parent: InternalNode ) extends Node {
		val isLeaf = false
		val branches = new ArrayBuffer[Node]
	}
	
	private [btree] class LeafNode( var parent: InternalNode ) extends Node {
		val isLeaf = true
		val values = new ArrayBuffer[V]
		var prev: LeafNode = null
		var next: LeafNode = null
	}
}