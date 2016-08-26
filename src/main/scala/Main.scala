package xyz.hyperreal.btree


object Main extends App {
	val tree = new BPlusTree[Char, Null]( 3 )
	
// 	tree.insert( "a", 1 )
// 	tree.insert( "b", 2 )
// 	tree.insert( "c", 3 )
// 	tree.insert( "d", 4 )
// 	tree.insert( "e", 5 )
// 	tree.insert( "f", 6 )
// 	tree.insert( "g", 7 )
	tree.insert( 'g' )
	tree.insert( 'f' )
	tree.insert( 'e' )
	tree.insert( 'd' )
	tree.insert( 'c' )
	tree.insert( 'b' )
	tree.insert( 'a' )
//  	println( tree.lookup("a") )
// 	println( tree.lookup("b") )
// 	println( tree.lookup("c") )
// 	println( tree.lookup("d") )
// 	println( tree.lookup("e") )
// 	println( tree.lookup("f") )
	
// 	for (k <- Vector( 'v', 't', 'u', 'j', 'g', 'w', 'y', 'c', 'n', 'l', 'a', 'r', 'b', 's', 'e', 'f', 'i', 'z', 'h', 'd', 'p', 'x', 'm', 'k', 'o', 'q' ))
// 		tree.insert( k )

	tree.prettyPrintKeysOnly
}