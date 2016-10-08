package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	
	val t = new FileBPlusTree[Int, Any]( newfile, 3 )
	
	t.insertKeysAndCheck( 1, 2 )
	t.dump
	println( "---------------" )
	t.insertKeys( 3 )
	t.dump
	println( t.wellConstructed )
}