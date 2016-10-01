package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	
// 	val m = new MutableSortedMap[Int, Any]
// 	
// 	m += (1 -> "one")
// 	println( m(1) )
	
//	val t = new MemoryBPlusTree[Int, Any]( 3 )
	val t = new FileBPlusTree[Int, String]( newfile, 3, true )
	
	t.insert( 1, "123456789" )
	println( t.search(1) )
}