package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	
// 	val m = new MutableSortedMap[Int, Any]
// 	
// 	m += (1 -> "one")
// 	println( m(1) )
	
//	val t = new MemoryBPlusTree[Int, Any]( 3 )
	val f = newfile
	val t = new FileBPlusTree[Int, Any]( f, 3 )
	
	t.insert( 1, Seq(3, 4, 5) )
	t.close
	
	val t1 = FileBPlusTree[Int, Any]( f )
	
	println( t1.search(1) )
}