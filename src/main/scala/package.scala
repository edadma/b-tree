package xyz.hyperreal

import util.matching.Regex.Match
import collection.mutable.ListBuffer

/**
 * Provides an abstract class for building and using B+ Trees.
 * 
 * ==Overview==
 * The class to extend is [[AbstractBPlusTree]].  It is designed to be both generic (type parameters for keys and values, and an abstract type for node references) and general (doesn't care how the tree is stored). An extending class needs to implement a number of simple methods and node type that 	provide storage abstraction.
 * 
 * There are two examples that extend `AbstractBPlusTree`: `MemoryBPlusTree` and `FileBPlusTree`. [[MemoryBPlusTree]] implements a B+ Tree in-memory and is essentially a map implementation.  [[FileBPlusTree]] implements a B+ Tree on-disk and is sort-of a very simple database.
 */
package object btree {
	abstract class NodeList
	case class InternalNodeList( list: List[NodeList] ) extends NodeList
	case class LeafNodeList( list: List[String] ) extends NodeList
	
	def build( s: String ): /*Node[String, Null]*/NodeList = {
//		var lastleaf: LeafNode[String, Null] = null
		val it = """[a-z]+|.""".r.findAllMatchIn(s) filterNot (m => m.matched.head.isSpaceChar)
		
		def internal( it: Iterator[Match], buf: ListBuffer[NodeList] ): InternalNodeList =
			it.next.matched match {
				case "(" => internal( it, buf += internal(it, new ListBuffer[NodeList]) )
				case "[" => internal( it, buf += leaf(it, new ListBuffer[String]) )
				case ")" => InternalNodeList( buf.toList )
				case t => sys.error( "unexpected token: " + t )
			}
			
		def leaf( it: Iterator[Match], buf: ListBuffer[String] ): LeafNodeList =
			it.next.matched match {
				case "]" => LeafNodeList( buf.toList )
				case elem if elem.head.isLetter => leaf( it, buf += elem )
				case t => sys.error( "unexpected token: " + t )
			}
		
		it.next.matched match {
			case "(" => internal( it, new ListBuffer[NodeList] )
			case "[" => leaf( it, new ListBuffer[String] )
			case t => sys.error( "unexpected token: " + t )
		}
	}
}