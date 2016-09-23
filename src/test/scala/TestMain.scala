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
	
// [n0: (null, null, null) n1 | 11 | n2]
// [n1: (null, n0, n2) n3 | 4 | n4 | 8 | n5] [n2: (n1, n0, null) n6 | 14 | n7]
// [n3: (null, n1, n4) n8 | 2 | n9] [n4: (n3, n1, n5) n10 | 7 | n11] [n5: (n4, n1, null) n12 | 9 | n13] [n6: (null, n2, n7) n14 | 13 | n15] [n7: (n6, n2, null) n16 | 16 | n17]
// [n8: (null, n3, n9) 1] [n9: (n8, n3, n10) 2 3] [n10: (n9, n4, n11) 5] [n11: (n10, n4, n12) 7] [n12: (n11, n5, n13) 8] [n13: (n12, n5, n14) 9] [n14: (n13, n6, n15) 12] [n15: (n14, n6, n16) 13] [n16: (n15, n7, n17) 14 15] [n17: (n16, n7, null) 16 17]
// delete 13
// [n0: (null, null, null) n1 | 9 | n2]
// [n1: (null, n0, n2) n3 | 4 | n4] [n2: (n1, n0, null) n5 | 17 | n6]
// [n3: (null, n1, n4) n7 | 2 | n8] [n4: (n3, n1, n5) n9 | 7 | n10] [n5: (n4, n2, null) n11 | 9 | n12] [n6: (null, n2, null) n13 | 14 | n14 | 16 | n15]
// [n7: (null, n3, n8) 1] [n8: (n7, n3, n9) 2 3] [n9: (n8, n4, n10) 5] [n10: (n9, n4, n11) 7] [n11: (n10, n5, n12) 8] [n12: (n11, n5, n13) 9] [n13: (n12, n6, n14) 12] [n14: (n13, n6, n15) 14 15] [n15: (n14, n6, null) 16 17]

// 	tree.insertKeys( Random.shuffle(1 to 17): _* )
// 	tree.prettyPrintKeysOnly
// 	println( tree.wellConstructed )
// 
// 	for (k <- Random.shuffle( 1 to 17 )) {
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

// 	tree.build( """
// 		(
// 			(
// 				([1 2] 3 [3]) 4 ([4] 5 [5])
// 			)
// 			7
// 			(
// 				([7] 8 [8 9]) 10 ([10] 11 [11 12])
// 			)
// 		)
// 		""" ).prettyPrintKeysOnly
// 	tree.delete( 5 )
//  	tree.prettyPrintKeysOnly
//  	println( tree.wellConstructed )
	
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