package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
//	val tree = new FileBPlusTree[String, Any]( "btree", 3 )
	val tree = new MemoryBPlusTree[Int, Any]( 3 )
// 	val map = new MutableSortedMap[String, Any]
	
// 	tree.insert( "a" )
// 	tree.insert( "b" )
//  	tree.insert( "c" )
// 	tree.insert( "d" )
//	tree.insert( "e" )
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
// 	tree.insert( "h" )
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
//		tree.insertKeys( "v", "t", "u", "j", "g", "w", "y", "c", "n", "l", "a", "r", "b", "s", "e", "f", "i", "z", "h", "d", "p", "x", "m", "k", "o", "q" )
// 	tree.insert( "abc" )
// 	tree.insert( "def" )
// 	tree.insert( "ghi" )
// 	tree.insert( "jkl" )
// 	tree.insert( "mno" )
//	tree.file.dump
//	tree.load( ("b", 2), ("a", 1), ("c", 3) )
// 	println( tree.max )
// 	tree.diagram( "tree" )
//	println( tree.keysIterator.toList )
//	println( tree.reverseKeysIterator.toList )
//	println( tree.boundedIterator(('>, "a"), ('<, "d")) map {case (k, _) => k} toList )
//	println( tree.boundedIteratorOverKeys(('<=, "z")) toList )
// 	tree.build( """
// 		(
// 			[g] j [j t] u [u v]
// 		)
// 		""" ).prettyPrintKeysOnly
//  	println( tree.wellConstructed )
// 	tree.delete( "u" )
//  	tree.prettyPrintKeysOnly
// 	tree.delete( "v" )
//  	tree.prettyPrintKeysOnly
//  	println( tree.wellConstructed )
// 	tree.delete( "j" )
//  	tree.prettyPrintKeysOnly
//  	println( tree.wellConstructed )

// 	tree.insertKeysAndCheck( Random.shuffle(1 to 40): _* ) match {
// 		case "true" =>
// 		case r =>
// 			println( r )
// 			sys.exit
// 	}
// 
// 	tree.prettyPrintKeysOnly
// 		
// 	for (k <- Random.shuffle( 1 to 40 )) {
// 		tree.delete( k )
// 		println( "delete " + k )
// 		tree.prettyPrintKeysOnly
// 		
// 		tree.wellConstructed match {
// 			case "true" =>
// 			case r =>
// 				println( r )
// 				sys.exit
// 		}
// 	}

	tree.build( """
		(
			(
				([1] 2 [2 3]) 4 ([5] 7 [7]) 8 ([8] 9 [9])
			)
			11
			(
				([12] 13 [13]) 14 ([14 15] 16 [16 17])
			)
		)
		""" ).prettyPrintKeysOnly
	tree.delete( 13 )
 	tree.prettyPrintKeysOnly
 	println( tree.wellConstructed )
	
// 	tree.build( """
// 		(
// 			([1] 2 [2]) 6 ([6 7] 8 [8 9]) 10 ([11] 12 [12])
// 		)
// 		""" ).prettyPrintKeysOnly
// 	tree.delete( 2 )
// 	tree.prettyPrintKeysOnly
// 	println( tree.wellConstructed )
// 	
// 	tree.build( """
// 		(
// 			([4] 5 [5] 6 [6]) 8 ([8] 9 [9])
// 		)
// 		""" ).prettyPrintKeysOnly
// 	tree.delete( 8 )
// 	tree.prettyPrintKeysOnly
// 	println( tree.wellConstructed )
}