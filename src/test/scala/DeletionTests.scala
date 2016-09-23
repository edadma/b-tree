package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._


class DeletionTests extends FreeSpec with PropertyChecks with Matchers {
	
	val order3 =
		Table(
			("object generator", 													"storage"),
			//----------------                               -------
			(() => new MemoryBPlusTree[String, Any]( 3 ),    "in memory"),
			(() => new FileBPlusTree[String, Any]( newfile, 3, true ),    "on disk")
 			)
	
	val order3int =
		Table(
			("object generator", 													"storage"),
			//----------------                               -------
			(() => new MemoryBPlusTree[Int, Any]( 3 ),    "in memory"),
			(() => new FileBPlusTree[Int, Any]( newfile, 3, true ),    "on disk")
 			)
	
	val order3int_temp =
		Table(
			("object generator", 													"storage"),
			//----------------                               -------
			(() => new MemoryBPlusTree[Int, Any]( 3 ),    "in memory")
 			)
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 2 level tree, first): " + storage + ", order 3") in {
			t.build( """
			(
				[a] b [b c]
			)
			""" ).prettyString shouldBe
			"""	|[n0: (null, null, null) n1 | b | n2]
					|[n1: (null, n0, n2) a] [n2: (n1, n0, null) b c]""".stripMargin
			t.delete( "b" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | b | n2]
						|[n1: (null, n0, n2) a] [n2: (n1, n0, null) c]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 2 level tree, second): " + storage + ", order 3") in {
			t.build( """
			(
				[a] b [b c]
			)
			""" )
			
			t.delete( "c" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | b | n2]
						|[n1: (null, n0, n2) a] [n2: (n1, n0, null) b]""".stripMargin
			t.wellConstructed shouldBe "true"
			t.delete( "b" ) shouldBe true
			t.prettyString shouldBe "[n0: (null, null, null) a]"
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 3 level tree, first): " + storage + ", order 3") in {
			t.build( """
			(
				([a] b [b c]) d ([d] e [e])
			)
			""" ).prettyString shouldBe
			"""	|[n0: (null, null, null) n1 | d | n2]
					|[n1: (null, n0, n2) n3 | b | n4] [n2: (n1, n0, null) n5 | e | n6]
					|[n3: (null, n1, n4) a] [n4: (n3, n1, n5) b c] [n5: (n4, n2, n6) d] [n6: (n5, n2, null) e]""".stripMargin
			t.delete( "b" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | d | n2]
						|[n1: (null, n0, n2) n3 | b | n4] [n2: (n1, n0, null) n5 | e | n6]
						|[n3: (null, n1, n4) a] [n4: (n3, n1, n5) c] [n5: (n4, n2, n6) d] [n6: (n5, n2, null) e]""".stripMargin
			t.wellConstructed shouldBe "true"
			t.delete( "a" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | d | n2 | e | n3]
						|[n1: (null, n0, n2) c] [n2: (n1, n0, n3) d] [n3: (n2, n0, null) e]""".stripMargin
			t.wellConstructed shouldBe "true"
			t.delete( "c" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | e | n2]
						|[n1: (null, n0, n2) d] [n2: (n1, n0, null) e]""".stripMargin
			t.wellConstructed shouldBe "true"
			t.delete( "d" ) shouldBe true
			t.prettyString shouldBe "[n0: (null, null, null) e]"
			t.wellConstructed shouldBe "true"
			t.delete( "e" ) shouldBe true
			t.prettyString shouldBe "[n0: (null, null, null)]"
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 3 level tree, second): " + storage + ", order 3") in {
			t.build( """
			(
				([a] b [b]) d ([d] e [e])
			)
			""" ).prettyString shouldBe
			"""	|[n0: (null, null, null) n1 | d | n2]
					|[n1: (null, n0, n2) n3 | b | n4] [n2: (n1, n0, null) n5 | e | n6]
					|[n3: (null, n1, n4) a] [n4: (n3, n1, n5) b] [n5: (n4, n2, n6) d] [n6: (n5, n2, null) e]""".stripMargin
			t.delete( "b" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | d | n2 | e | n3]
						|[n1: (null, n0, n2) a] [n2: (n1, n0, n3) d] [n3: (n2, n0, null) e]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 3 level tree, third): " + storage + ", order 3") in {
			t.build( """
			(
				([a] b [b]) d ([d] e [e])
			)
			""" )
			t.delete( "d" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | b | n2 | e | n3]
						|[n1: (null, n0, n2) a] [n2: (n1, n0, n3) b] [n3: (n2, n0, null) e]""".stripMargin
			t.wellConstructed shouldBe "true"
			t.delete( "e" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | b | n2]
						|[n1: (null, n0, n2) a] [n2: (n1, n0, null) b]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 3 level tree, fourth): " + storage + ", order 3") in {
			t.build( """
			(
				([a] b [b]) d ([d] e [e])
			)
			""" )
			t.delete( "d" ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | b | n2 | e | n3]
						|[n1: (null, n0, n2) a] [n2: (n1, n0, n3) b] [n3: (n2, n0, null) e]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3int) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 3 level tree, merge up to root - not empty): " + storage + ", order 3") in {
			t.build( """
			(
				([1] 2 [2]) 3 ([3] 4 [4]) 5 ([5] 6 [6 7])
			)
			""" ).prettyString shouldBe
			"""	|[n0: (null, null, null) n1 | 3 | n2 | 5 | n3]
					|[n1: (null, n0, n2) n4 | 2 | n5] [n2: (n1, n0, n3) n6 | 4 | n7] [n3: (n2, n0, null) n8 | 6 | n9]
					|[n4: (null, n1, n5) 1] [n5: (n4, n1, n6) 2] [n6: (n5, n2, n7) 3] [n7: (n6, n2, n8) 4] [n8: (n7, n3, n9) 5] [n9: (n8, n3, null) 6 7]""".stripMargin
			t.delete( 4 ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | 3 | n2]
						|[n1: (null, n0, n2) n3 | 2 | n4] [n2: (n1, n0, null) n5 | 5 | n6 | 6 | n7]
						|[n3: (null, n1, n4) 1] [n4: (n3, n1, n5) 2] [n5: (n4, n2, n6) 3] [n6: (n5, n2, n7) 5] [n7: (n6, n2, null) 6 7]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3int_temp) { (gen, storage) =>
		val t = gen()
		
		("deletion (non-leaf borrow from right): " + storage + ", order 3") in {
			t.build( """
			(
				([4] 5 [5]) 6 ([6] 8 [8] 9 [9])
			)
			""" )
			t.delete( 4 ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | 8 | n2]
						|[n1: (null, n0, n2) n3 | 6 | n4] [n2: (n1, n0, null) n5 | 9 | n6]
						|[n3: (null, n1, n4) 5] [n4: (n3, n1, n5) 6] [n5: (n4, n2, n6) 8] [n6: (n5, n2, null) 9]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3int_temp) { (gen, storage) =>
		val t = gen()
		
		("deletion (non-leaf borrow from left): " + storage + ", order 3") in {
			t.build( """
			(
				([4] 5 [5] 6 [6]) 8 ([8] 9 [9])
			)
			""" )
			t.delete( 8 ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | 6 | n2]
						|[n1: (null, n0, n2) n3 | 5 | n4] [n2: (n1, n0, null) n5 | 9 | n6]
						|[n3: (null, n1, n4) 4] [n4: (n3, n1, n5) 5] [n5: (n4, n2, n6) 6] [n6: (n5, n2, null) 9]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3int_temp) { (gen, storage) =>
		val t = gen()
		
		("deletion (merge up to root, 4 level tree): " + storage + ", order 3") in {
			t.build( """
			(
				(
					([1 2] 3 [3]) 4 ([4] 5 [5])
				)
				7
				(
					([7] 8 [8 9]) 10 ([10] 11 [11 12])
				)
			)
			""" )
			t.delete( 5 ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | 7 | n2 | 10 | n3]
						|[n1: (null, n0, n2) n4 | 3 | n5 | 4 | n6] [n2: (n1, n0, n3) n7 | 8 | n8] [n3: (n2, n0, null) n9 | 11 | n10]
						|[n4: (null, n1, n5) 1 2] [n5: (n4, n1, n6) 3] [n6: (n5, n1, n7) 4] [n7: (n6, n2, n8) 7] [n8: (n7, n2, n9) 8 9] [n9: (n8, n3, n10) 10] [n10: (n9, n3, null) 11 12]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
	
	forAll (order3int_temp) { (gen, storage) =>
		val t = gen()
		
		("deletion (merge not up to root, 3 level tree): " + storage + ", order 3") in {
			t.build( """
			(
				([1] 2 [2]) 6 ([6 7] 8 [8 9]) 10 ([11] 12 [12])
			)
			""" )
			t.delete( 2 ) shouldBe true
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | 10 | n2]
						|[n1: (null, n0, n2) n3 | 6 | n4 | 8 | n5] [n2: (n1, n0, null) n6 | 12 | n7]
						|[n3: (null, n1, n4) 1] [n4: (n3, n1, n5) 6 7] [n5: (n4, n1, n6) 8 9] [n6: (n5, n2, n7) 11] [n7: (n6, n2, null) 12]""".stripMargin
			t.wellConstructed shouldBe "true"
		}
	}
}