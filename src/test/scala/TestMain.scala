package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	
	val t = new FileBPlusTree[Int, String]( newfile, 3 )
	
	t.insert( 1, "a"*500 )
	t.insert( 2, "b"*500 )
	t.insert( 3, "c"*500 )
	t.insert( 4, "d"*500 )
	t.insert( 5, "e"*500 )
	println( t.wellConstructed )
	println( t.search(3))
}