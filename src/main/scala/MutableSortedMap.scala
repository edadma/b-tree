package xyz.hyperreal.btree

import collection.SortedMap
import collection.mutable.{Map, MapLike, AbstractMap}


object MutableSortedMap {
	private val ORDER = 10
}

class MutableSortedMap[K <% Ordered[K], V]( btree: MemoryBPlusTree[K, V] ) extends SortedMap[K, V] {
	def this() = this( new MemoryBPlusTree[K, V]( MutableSortedMap.ORDER ) )

	implicit def ordering = implicitly[Ordering[K]]
	
	def +=( kv: (K, V) ) = {
		kv match {case (k, v) => btree insert (k, v)}
		this
	}
	
	def -=( key: K ) = {
		btree delete key
		this
	}
	
	override def +[V1 >: V]( kv: (K, V1) ) = {
		val newtree = new MemoryBPlusTree[K, V1]( MutableSortedMap.ORDER )
		
		newtree.load( btree.boundedIterator( ('<, kv._1) ).toList: _* )
		newtree.insert( kv._1, kv._2 )
		newtree.load( btree.boundedIterator( ('>, kv._1) ).toList: _* )
		new MutableSortedMap( newtree )
	}
	
	override def -( key: K ) = {
		val newtree = new MemoryBPlusTree[K, V]( MutableSortedMap.ORDER )
		
		newtree.load( btree.boundedIterator( ('<, key) ).toList: _* )
		newtree.load( btree.boundedIterator( ('>, key) ).toList: _* )
		new MutableSortedMap( newtree )
	}
	
	def rangeImpl( from: Option[K], until: Option[K] ) = {
		val bounds =
			(from, until) match {
				case (None, None) => Nil
				case (None, Some( u )) => List( ('<, u) )
				case (Some( l ), None) => List( ('>=, l) )
				case (Some( l ), Some( u )) => List( ('>=, l), ('<, u) )
			}
			
		val view = new MutableSortedMap[K, V]( btree ) {
			override def iterator = if (bounds isEmpty) btree.iterator else btree.boundedIterator( bounds: _* )
		}
		
		view
	}
	
	override def empty = new MutableSortedMap[K, V]
	
	def get( key: K ) = btree search key
	
	def iterator = btree iterator
	
	def iteratorFrom( start: K ) = btree.boundedIterator( ('>=, start) )
	
	def keysIteratorFrom( start: K ) = btree.boundedKeysIterator( ('>=, start) )
	
	def valuesIteratorFrom( start: K ) = btree.boundedValuesIterator( ('>=, start) )
}