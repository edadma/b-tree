package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	val t = new FileBPlusTree[Int, Any]( "btree", 3, true )
//	val t = new MemoryBPlusTree[Int, Any]( 3 )
// 	val m = new MutableSortedMap[String, Any]
	
	t.insertKeys( 1, 2, 3, 4 )
	t.prettyPrintKeysOnly
	println( "------" )
	t.insertKeys( 5 )
	t.prettyPrintKeysOnly
	println( t.wellConstructed )
}