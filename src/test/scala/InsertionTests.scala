package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._


class InsertionTests extends FreeSpec with PropertyChecks with Matchers {
	
	val order3 =
		Table(
			("object generator", 													"storage"),
			//----------------                               -------
			(() => new MemoryBPlusTree[String, Any]( 3 ),    "in memory"),
			(() => new FileBPlusTree[String, Any]( newfile, 3 ),    "on disk")
	 		)
	val order4 =
		Table(
			("object generator", 													"storage"),
			//----------------                               -------
			(() => new MemoryBPlusTree[String, Any]( 4 ),    "in memory"),
			(() => new FileBPlusTree[String, Any]( newfile, 4 ),    "on disk")
	 		)
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		"ascending insertion (order 3): " + storage in {
		
			t.prettyString shouldBe "[n0: (null, null, null)]"
			t.insert( "a", 1 )
			t.prettyStringWithValues shouldBe "[n0: (null, null, null) <a, 1>]"
			t.insert( "b", 2 )
			t.prettyStringWithValues shouldBe "[n0: (null, null, null) <a, 1> <b, 2>]"
			t.insert( "c", 3 )
			t.prettyStringWithValues shouldBe
				"""	|[n0: (null, null, null) n1 | b | n2]
						|[n1: (null, n0, n2) <a, 1>] [n2: (n1, n0, null) <b, 2> <c, 3>]""".stripMargin
			t.insert( "d", 4 )
			t.prettyStringWithValues shouldBe
				"""	|[n0: (null, null, null) n1 | b | n2 | c | n3]
						|[n1: (null, n0, n2) <a, 1>] [n2: (n1, n0, n3) <b, 2>] [n3: (n2, n0, null) <c, 3> <d, 4>]""".stripMargin
			t.insert( "e", 5 )
			t.prettyStringWithValues shouldBe
				"""	|[n0: (null, null, null) n1 | c | n2]
						|[n1: (null, n0, n2) n3 | b | n4] [n2: (n1, n0, null) n5 | d | n6]
						|[n3: (null, n1, n4) <a, 1>] [n4: (n3, n1, n5) <b, 2>] [n5: (n4, n2, n6) <c, 3>] [n6: (n5, n2, null) <d, 4> <e, 5>]""".stripMargin
			t.insert( "f", 6 )
			t.prettyStringWithValues shouldBe
				"""	|[n0: (null, null, null) n1 | c | n2]
						|[n1: (null, n0, n2) n3 | b | n4] [n2: (n1, n0, null) n5 | d | n6 | e | n7]
						|[n3: (null, n1, n4) <a, 1>] [n4: (n3, n1, n5) <b, 2>] [n5: (n4, n2, n6) <c, 3>] [n6: (n5, n2, n7) <d, 4>] [n7: (n6, n2, null) <e, 5> <f, 6>]""".stripMargin
			t.insert( "g", 7 )
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | c | n2 | e | n3]
						|[n1: (null, n0, n2) n4 | b | n5] [n2: (n1, n0, n3) n6 | d | n7] [n3: (n2, n0, null) n8 | f | n9]
						|[n4: (null, n1, n5) a] [n5: (n4, n1, n6) b] [n6: (n5, n2, n7) c] [n7: (n6, n2, n8) d] [n8: (n7, n3, n9) e] [n9: (n8, n3, null) f g]""".stripMargin
			t.wellConstructed shouldBe "true"
			t.prettySearch( "c" ) shouldBe "n6 3 0"
		}
	}
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
			
		"descending insertion (order 3): " + storage in {
			t.insertKeysAndCheck( "g", "f" ) shouldBe "true"
			t.prettyString shouldBe "[n0: (null, null, null) f g]"
			t.insert( "e" )
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | f | n2]
						|[n1: (null, n0, n2) e] [n2: (n1, n0, null) f g]""".stripMargin
			t.insert( "d" )
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | f | n2]
						|[n1: (null, n0, n2) d e] [n2: (n1, n0, null) f g]""".stripMargin
			t.insert( "c" )
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | d | n2 | f | n3]
						|[n1: (null, n0, n2) c] [n2: (n1, n0, n3) d e] [n3: (n2, n0, null) f g]""".stripMargin
			t.insert( "b" )
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | d | n2 | f | n3]
						|[n1: (null, n0, n2) b c] [n2: (n1, n0, n3) d e] [n3: (n2, n0, null) f g]""".stripMargin
			t.insert( "a" )
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | d | n2]
						|[n1: (null, n0, n2) n3 | b | n4] [n2: (n1, n0, null) n5 | f | n6]
						|[n3: (null, n1, n4) a] [n4: (n3, n1, n5) b c] [n5: (n4, n2, n6) d e] [n6: (n5, n2, null) f g]""".stripMargin
			t.wellConstructed shouldBe "true"
			t.prettySearch( "e" ) shouldBe "n5 null 1"
		}
	}
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
	
		"random insertion (order 3): " + storage in	{
			t.insertKeysAndCheck( "v", "t", "u", "j", "g", "w", "y", "c", "n", "l", "a", "r", "b", "s", "e", "f", "i", "z", "h", "d", "p", "x", "m", "k", "o", "q" ) shouldBe "true"
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | g | n2 | r | n3]
						|[n1: (null, n0, n2) n4 | e | n5] [n2: (n1, n0, n3) n6 | j | n7 | n | n8] [n3: (n2, n0, null) n9 | u | n10 | w | n11]
						|[n4: (null, n1, n5) n12 | c | n13] [n5: (n4, n1, null) n14 | f | n15] [n6: (null, n2, n7) n16 | h | n17] [n7: (n6, n2, n8) n18 | l | n19] [n8: (n7, n2, null) n20 | o | n21 | p | n22] [n9: (null, n3, n10) n23 | s | n24] [n10: (n9, n3, n11) n25 | v | n26] [n11: (n10, n3, null) n27 | y | n28]
						|[n12: (null, n4, n13) a b] [n13: (n12, n4, n14) c d] [n14: (n13, n5, n15) e] [n15: (n14, n5, n16) f] [n16: (n15, n6, n17) g] [n17: (n16, n6, n18) h i] [n18: (n17, n7, n19) j k] [n19: (n18, n7, n20) l m] [n20: (n19, n8, n21) n] [n21: (n20, n8, n22) o] [n22: (n21, n8, n23) p q] [n23: (n22, n9, n24) r] [n24: (n23, n9, n25) s t] [n25: (n24, n10, n26) u] [n26: (n25, n10, n27) v] [n27: (n26, n11, n28) w x] [n28: (n27, n11, null) y z]""".stripMargin
			t.prettySearch( "i" ) shouldBe "n17 null 1"
		}
	}
	
	forAll (order4) { (gen, storage) =>
		val t = gen()
	
		"ascending insertion (order 4): " + storage in {
			t.prettyString shouldBe "[n0: (null, null, null)]"
			t.insert( "a" )
			t.insert( "b" )
			t.insert( "c" )
			t.insert( "d" )
			t.insert( "e" )
			t.insert( "f" )
			t.insert( "g" )
			t.insert( "h" )
			t.insert( "i" )
			t.insert( "j" )
			t.insert( "k" )
			t.insert( "l" )
			t.insert( "m" )
			t.insert( "n" )
			t.insert( "o" )
			t.insert( "p" )
			t.wellConstructed shouldBe "true"
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | g | n2 | m | n3]
						|[n1: (null, n0, n2) n4 | c | n5 | e | n6] [n2: (n1, n0, n3) n7 | i | n8 | k | n9] [n3: (n2, n0, null) n10 | o | n11]
						|[n4: (null, n1, n5) a b] [n5: (n4, n1, n6) c d] [n6: (n5, n1, n7) e f] [n7: (n6, n2, n8) g h] [n8: (n7, n2, n9) i j] [n9: (n8, n2, n10) k l] [n10: (n9, n3, n11) m n] [n11: (n10, n3, null) o p]""".stripMargin
			t.prettySearch( "h" ) shouldBe "n7 null 1"
		}
	}
	
	forAll (order4) { (gen, storage) =>
		val t = gen()
	
		"descending insertion (order 4): " + storage in {
			t.insertKeysAndCheck( "p", "o", "n", "m", "l", "k", "j", "i", "h", "g", "f", "e", "d", "c", "b", "a" ) shouldBe "true"
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | i | n2 | m | n3]
						|[n1: (null, n0, n2) n4 | c | n5 | e | n6 | g | n7] [n2: (n1, n0, n3) n8 | k | n9] [n3: (n2, n0, null) n10 | o | n11]
						|[n4: (null, n1, n5) a b] [n5: (n4, n1, n6) c d] [n6: (n5, n1, n7) e f] [n7: (n6, n1, n8) g h] [n8: (n7, n2, n9) i j] [n9: (n8, n2, n10) k l] [n10: (n9, n3, n11) m n] [n11: (n10, n3, null) o p]""".stripMargin
			t.prettySearch( "h" ) shouldBe "n7 null 1"
		}
	}
	
	forAll (order4) { (gen, storage) =>
		val t = gen()
	
		"random insertion (order 4): " + storage in	{
			t.insertKeysAndCheck( "v", "t", "u", "j", "g", "w", "y", "c", "n", "l", "a", "r", "b", "s", "e", "f", "i", "z", "h", "d", "p", "x", "m", "k", "o", "q" ) shouldBe "true"
			t.prettyString shouldBe
				"""	|[n0: (null, null, null) n1 | h | n2 | n | n3 | u | n4]
						|[n1: (null, n0, n2) n5 | c | n6 | f | n7] [n2: (n1, n0, n3) n8 | j | n9 | l | n10] [n3: (n2, n0, n4) n11 | p | n12 | s | n13] [n4: (n3, n0, null) n14 | w | n15 | y | n16]
						|[n5: (null, n1, n6) a b] [n6: (n5, n1, n7) c d e] [n7: (n6, n1, n8) f g] [n8: (n7, n2, n9) h i] [n9: (n8, n2, n10) j k] [n10: (n9, n2, n11) l m] [n11: (n10, n3, n12) n o] [n12: (n11, n3, n13) p q r] [n13: (n12, n3, n14) s t] [n14: (n13, n4, n15) u v] [n15: (n14, n4, n16) w x] [n16: (n15, n4, null) y z]""".stripMargin
			t.prettySearch( "i" ) shouldBe "n8 null 1"
		}
	}
}