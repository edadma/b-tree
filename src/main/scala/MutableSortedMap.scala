package xyz.hyperreal.btree

import collection.SortedMap
import collection.mutable.{Map, MapLike, AbstractMap}


class MutableSortedMap[K <% Ordered[K], V] extends AbstractMap[K, V] with MapLike[K, V, MutableSortedMap[K, V]] {//SortedMap[K, V] {
	private val btree = new MemoryBPlusTree[K, V]( 10 )
	
	implicit def ordering = implicitly[Ordering[K]]
	
	def +=( kv: (K, V) ) = {
		kv match {case (k, v) => btree insert (k, v)}
		this
	}
	
	def -=( key: K ) = {
		btree delete key
		this
	}
	
	override def empty = new MutableSortedMap[K, V]
	
	def get( key: K ) = btree search key
	
	def iterator = btree iterator
	
	def iteratorFrom( start: K ) = btree.boundedIterator( ('>=, start) )
	
	def keysIteratorFrom( start: K ) = btree.boundedIteratorOverKeys( ('>=, start) )
}