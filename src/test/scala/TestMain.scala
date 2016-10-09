package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	
	val t = new FileBPlusTree[Int, String]( newfile, 3 )
	
	t.insert( 3, "a" )
	t.insert( 5, "b" )
	println( t.greatestLessThan(3) )
	
}