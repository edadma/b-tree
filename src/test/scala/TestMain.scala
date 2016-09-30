package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	
// 	val m = new MutableSortedMap[Int, Any]
// 	
// 	m += (1 -> "one")
// 	println( m(1) )
	
//	val t = new MemoryBPlusTree[Int, Any]( 3 )
	val t = new FileBPlusTree[Int, Any]( newfile, 3, true )
	
	t.insert( 1, Map(10 -> "ten", 11 -> "eleven", 12 -> "twelve") )
	println( t.search(1).get.asInstanceOf[collection.Map[Int, Any]](11) )
}