import collection.mutable.ArrayBuffer


object Main extends App {
	
}

class BPlusTree[K <: Ordered[K], V]( order: Int ) {
	var root: Node = new LeafNode
	
	private def lookup( key: K ): (Boolean, LeafNode, Int) = {
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
					l.values indexWhere (key <= _.key) match {
						case -1 => (false, l, l.values.size)
						case index =>
							if (key == l.values(index).key)
								(true, l, index)
							else
								(false, l, index)
					}
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
	
	case class Pair( key: K, var value: V )
		
	abstract class Node {
		def isLeaf: Boolean
		
		def asInternal = this.asInstanceOf[InternalNode]
		
		def asLeaf = this.asInstanceOf[LeafNode]
	}
	
	class InternalNode extends Node {
		val keys = new ArrayBuffer[K]
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