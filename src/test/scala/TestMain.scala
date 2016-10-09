package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	
	val t = new FileBPlusTree[Int, Null]( newfile, 3 )
	
	t.insertKeys( 3, 4, 5 )
	println( t.reverseBoundedIterator(('<, 5), ('>, 2)).toList )
	
}