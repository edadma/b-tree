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
// 	tree.build( """
// 		(
// 			([1 2] 3 [3]) 4 ([4] 5 [5]) 6 ([6 7] 8 [8] 9 [9 10])
// 		)
// 		""" ).prettyPrintKeysOnly
//  	tree.delete( "d" )
//  	tree.prettyPrintKeysOnly
//  	println( tree.wellConstructed )
	
// 	tree.insertKeys( Random.shuffle(1 to 12): _* )
// 	tree.prettyPrintKeysOnly
// 	println( tree.wellConstructed )
// 	
// 	for (k <- Random.shuffle( 1 to 12 )) {
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
				([1 2] 3 [3]) 4 ([4] 5 [5])
			)
			7
			(
				([7] 8 [8 9]) 10 ([10] 11 [11 12])
			)
		)
		""" ).prettyPrintKeysOnly
	tree.delete( 5 )
 	tree.prettyPrintKeysOnly
 	println( tree.wellConstructed )
	
// 	tree.build( """
// 		(
// 			([4] 5 [5]) 6 ([6] 8 [8] 9 [9])
// 		)
// 		""" ).prettyPrintKeysOnly
// 	tree.delete( 4 )
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