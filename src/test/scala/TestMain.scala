package xyz.hyperreal.btree


object TestMain extends App {
	val tree = new FileBPlusTree( "btree", 3 )
//	val tree = new MemoryBPlusTree[String, Any]( 3 )
	
// 	tree.insert( "a" )
//  	tree.insert( "b" )
//  	tree.insert( "c" )
//  	tree.insert( "d" )
//  	tree.insert( "e" )
// 	tree.insert( "f" )
// 	tree.insert( "g" )
// 	tree.insert( "h" )
// 	tree.insert( "i" )
// 	tree.insert( "j" )
// 	tree.insert( "k" )
// 	tree.insert( "l" )
// 	tree.insert( "m" )
// 	tree.insert( "n" )
// 	tree.insert( "o" )
// 	tree.insert( "p" )
// 	tree.insert( "p", "o", "n", "m", "l", "k", "j", "i", "h" )
// 	tree.insert( "g" )
// 	tree.insert( "f" )
// 	tree.insert( "e" )
// 	tree.insert( "d" )
// 	tree.insert( "c" )
// 	tree.insert( "b" )
// 	tree.insert( "a" )
//  	println( tree.prettySearch("h") )
// 	println( tree.lookup("b") )
// 	println( tree.lookup("c") )
// 	println( tree.lookup("d") )
// 	println( tree.lookup("e") )
// 	println( tree.lookup("f") )
	
// 	tree.insert( "v", "t", "u", "j", "g", "w", "y", "c", "n", "l", "a", "r", "b", "s", "e", "f", "i", "z", "h", "d", "p", "x", "m", "k", "o", "q" )
// 	tree.insert( "abc" )
// 	tree.insert( "def" )
// 	tree.insert( "ghi" )
// 	tree.insert( "jkl" )
// 	tree.insert( "mno" )
//	tree.file.dump
	println( tree.wellConstructed )
	tree.prettyPrintKeysOnly
	println( tree.iterator.toList )
}