package xyz.hyperreal.btree

import util.Random


object TestMain extends App {
	val tree = new FileBPlusTree[Int, Any]( "btree", 3, true )
//	val tree = new MemoryBPlusTree[Int, Any]( 3 )
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

// 	println( "inserting..." )
// 	
// 	tree.insertKeysAndCheck( Random.shuffle(1 to 10): _* ) match {
// 		case "true" =>
// 		case r =>
// 			println( r )
// 			sys.exit
// 	}
// 
// 	tree.prettyPrintKeysOnly
// 	println( "deleting..." )
// 		
// 	for (k <- Random.shuffle( 1 to 10 )) {
// 		println( k )
// 		tree.delete( k ) match {
// 			case true =>
// 			case _ =>
// 				println( "key " + k + " not found" )
// 				tree.prettyPrintKeysOnly
// 				sys.exit
// 		}
// 
// 		tree.wellConstructed match {
// 			case "true" =>
// 			case r =>
// 				tree.prettyPrintKeysOnly
// 				println( r )
// 				sys.exit
// 		}
// 	}
// 	
// 	tree.prettyPrintKeysOnly

// 	tree.build( """
// 		(
// 			(
// 				([1] 2 [2 3]) 4 ([5] 7 [7]) 8 ([8] 9 [9])
// 			)
// 			11
// 			(
// 				([12] 13 [13]) 14 ([14 15] 16 [16 17])
// 			)
// 		)
// 		""" ).prettyPrintKeysOnly
// 	tree.delete( 13 )
//  	tree.prettyPrintKeysOnly
//  	println( tree.wellConstructed )
	
// [n0: (null, null, null) n1 | 5 | n2 | 8 | n3]
// [n1: (null, n0, n2) n4 | 2 | n5 | 3 | n6] [n2: (n1, n0, n3) n7 | 7 | n8] [n3: (n2, n0, null) n9 | 9 | n10]
// [n4: (null, n1, n5) 1] [n5: (n4, n1, n6) 2] [n6: (n5, n1, n7) 3 4] [n7: (n6, n2, n8) 5 6] [n8: (n7, n2, n9) 7] [n9: (n8, n3, n10) 8] [n10: (n9, n3, null) 9 10]

// 6
// 2
// 5
// 9
// 8
// 1
// [n0: (null, null, null) n1 | 5 | n2]
// [n1: (null, n0, n2) n3 | 3 | n4] [n2: (n1, n0, null) n5 | 8 | n6]
// [n3: (null, n1, n4) 3] [n4: (n3, n1, n5) 3] [n5: (n4, n2, n6) 7] [n6: (n5, n2, null) 10]

	tree.build( """
		(
			([1] 2 [2] 3 [3 4]) 5 ([5 6] 7 [7]) 8 ([8] 9 [9 10])
		)
		""" ).prettyPrintKeysOnly
	tree.delete( 6 )
	tree.delete( 2 )
	tree.delete( 5 )
	tree.delete( 9 )
	tree.delete( 8 )
	tree.delete( 1 )
	tree.prettyPrintKeysOnly
	println( tree.wellConstructed )
	
// 	tree.build( """
// 		(
// 			([4] 5 [5] 6 [6]) 8 ([8] 9 [9])
// 		)
// 		""" ).prettyPrintKeysOnly
// 	tree.delete( 8 )
// 	tree.prettyPrintKeysOnly
// 	println( tree.wellConstructed )
}