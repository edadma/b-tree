package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	def test( t: BPlusTree[Int, Any] ) = {
		t.insertKeys( 1, 2, 3, 4 )
		t.prettyPrintKeysOnly
		println
		t.insertKeys( 5 )
		t.prettyPrintKeysOnly
		println( t.wellConstructed )
		println( "------" )
	}
	
	test( new MemoryBPlusTree[Int, Any](3) )
	test( new FileBPlusTree[Int, Any](newfile, 3, true) )
}