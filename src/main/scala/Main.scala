package xyz.hyperreal.btree

import collection.mutable.ArrayBuffer
import scala.collection.Searching._


object Main extends App {
	val tree = new BPlusTree[String, Int]( 3 )
	
	tree.insert( "a", 1 )
	tree.insert( "b", 2 )
	println( tree.lookup("c") )
	println( tree.lookup("aa") )
	println( tree.lookup("A") )
	println( tree.lookup("b") )
}

class BPlusTree[K <% Ordered[K], V]( order: Int ) {
	private var root: Node = new LeafNode
	
	private [btree] val compare = (elem: Pair, target: K) => elem.key compare target

	private [btree] def binarySearch[E, K]( seq: IndexedSeq[E], target: K, compare: (E, K) => Int ): Int = {
		def search( start: Int, end: Int ): Int = {
			if (start > end)
				-start - 1
			else {
				val mid = start + (end-start + 1)/2
				
				if (compare( seq(mid), target ) == 0)
					mid
				else if (compare( seq(mid), target ) > 0)
					search( start, mid - 1 )
				else
					search( mid + 1, end )
			}
		}
		
		search( 0, seq.length - 1 )
	}
	
	private [btree] def lookup( key: K ): (Boolean, LeafNode, Int) = {
		def _lookup( n: Node ): (Boolean, LeafNode, Int) =
			n match {
				case i: InternalNode =>
					if (key < i.keys.head)
						_lookup( i.branches.head )
					else if (key >= i.keys.last)
						_lookup( i.branches.last )
					else
						_lookup( i.branches(i.keys.view(1, i.keys.size - 1) indexWhere (key >= _)) )
				case l: LeafNode =>
					binarySearch( l.values, key, compare ) match {
						case index if index >= 0 => (true, l, index)
						case index => (false, l, -(index + 1))
					}
// 					l.values indexWhere (key <= _.key) match {
// 						case -1 => (false, l, l.values.size)
// 						case index =>
// 							if (key == l.values(index).key)
// 								(true, l, index)
// 							else
// 								(false, l, index)
// 					}
			}
			
		_lookup( root )
	}
	
	def search( key: K ): Option[V] =
		lookup( key ) match {
			case (true, l, index) => Some( l.values(index).value )
			case _ => None
		}
		
	def insert( key: K, value: V ): Boolean = {
		lookup( key ) match {
			case (true, l, index) =>
				l.values(index).value = value
				true
			case (false, l, index) =>
				if (l.values.size < order) {
					l.values.insert( index, Pair(key, value) )
					false
				} else
					sys.error( "root full" )
		}
	}
	
	def delete( key: K ) = {
		
	}
	
	def print {
		
	}
	
	case class Pair( key: K, var value: V )
	
	abstract class Node {
		def isLeaf: Boolean
		
		def asInternal = asInstanceOf[InternalNode]
		
		def asLeaf = asInstanceOf[LeafNode]
	}
	
	class InternalNode extends Node {
		val keys = new ArrayBuffer
		val isLeaf = false
		val branches = new ArrayBuffer[Node]
	}
	
	class LeafNode extends Node {
		val isLeaf = true
		val values = new ArrayBuffer[Pair]
		val left: LeafNode = null
		val right: LeafNode = null
	}
}