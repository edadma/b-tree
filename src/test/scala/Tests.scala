package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers {
	
	"ascending insertion (order 3)" in {
		val tree = new BPlusTree[Char, Int]( 3 )
	
		tree.prettyString shouldBe "[n0: (null, null, null)]"
		tree.insert( 'a', 1 )
		tree.prettyStringWithValues shouldBe "[n0: (null, null, null) <a, 1>]"
		tree.insert( 'b', 2 )
		tree.prettyStringWithValues shouldBe "[n0: (null, null, null) <a, 1> <b, 2>]"
		tree.insert( 'c', 3 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | b | n2]
					|[n1: (null, n0, n2) <a, 1>] [n2: (n1, n0, null) <b, 2> <c, 3>]""".stripMargin
		tree.insert( 'd', 4 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | b | n2 | c | n3]
					|[n1: (null, n0, n2) <a, 1>] [n2: (n1, n0, n3) <b, 2>] [n3: (n2, n0, null) <c, 3> <d, 4>]""".stripMargin
		tree.insert( 'e', 5 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | c | n2]
					|[n1: (n0) n3 | b | n4] [n2: (n0) n5 | d | n6]
					|[n3: (null, n1, n4) <a, 1>] [n4: (n3, n1, n5) <b, 2>] [n5: (n4, n2, n6) <c, 3>] [n6: (n5, n2, null) <d, 4> <e, 5>]""".stripMargin
		tree.insert( 'f', 6 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | c | n2]
					|[n1: (n0) n3 | b | n4] [n2: (n0) n5 | d | n6 | e | n7]
					|[n3: (null, n1, n4) <a, 1>] [n4: (n3, n1, n5) <b, 2>] [n5: (n4, n2, n6) <c, 3>] [n6: (n5, n2, n7) <d, 4>] [n7: (n6, n2, null) <e, 5> <f, 6>]""".stripMargin
		tree.insert( 'g', 7 )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | c | n2 | e | n3]
					|[n1: (n0) n4 | b | n5] [n2: (n0) n6 | d | n7] [n3: (n0) n8 | f | n9]
					|[n4: (null, n1, n5) a] [n5: (n4, n1, n6) b] [n6: (n5, n2, n7) c] [n7: (n6, n2, n8) d] [n8: (n7, n3, n9) e] [n9: (n8, n3, null) f g]""".stripMargin
		tree.wellConstructed shouldBe "true"
		tree.prettySearch( 'c' ) shouldBe "n6 3 0"
	}
	
	"descending insertion (order 3)" in {
		val tree = new BPlusTree[Char, Null]( 3 )
	
		tree.insert( 'g', 'f' )
		tree.prettyString shouldBe "[n0: (null, null, null) f g]"
		tree.insert( 'e' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | f | n2]
					|[n1: (null, n0, n2) e] [n2: (n1, n0, null) f g]""".stripMargin
		tree.insert( 'd' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | f | n2]
					|[n1: (null, n0, n2) d e] [n2: (n1, n0, null) f g]""".stripMargin
		tree.insert( 'c' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | d | n2 | f | n3]
					|[n1: (null, n0, n2) c] [n2: (n1, n0, n3) d e] [n3: (n2, n0, null) f g]""".stripMargin
		tree.insert( 'b' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | d | n2 | f | n3]
					|[n1: (null, n0, n2) b c] [n2: (n1, n0, n3) d e] [n3: (n2, n0, null) f g]""".stripMargin
		tree.insert( 'a' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | d | n2]
					|[n1: (n0) n3 | b | n4] [n2: (n0) n5 | f | n6]
					|[n3: (null, n1, n4) a] [n4: (n3, n1, n5) b c] [n5: (n4, n2, n6) d e] [n6: (n5, n2, null) f g]""".stripMargin
		tree.wellConstructed shouldBe "true"
		tree.prettySearch( 'e' ) shouldBe "n5 null 1"
	}
	
	"random insertion (order 3)" in	{
		val tree = new BPlusTree[Char, Null]( 3 )
	
		for (k <- Vector( 'v', 't', 'u', 'j', 'g', 'w', 'y', 'c', 'n', 'l', 'a', 'r', 'b', 's', 'e', 'f', 'i', 'z', 'h', 'd', 'p', 'x', 'm', 'k', 'o', 'q' ))
			tree.insert( k )
			
		tree.wellConstructed shouldBe "true"
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | g | n2 | r | n3]
					|[n1: (n0) n4 | e | n5] [n2: (n0) n6 | j | n7 | n | n8] [n3: (n0) n9 | u | n10 | w | n11]
					|[n4: (n1) n12 | c | n13] [n5: (n1) n14 | f | n15] [n6: (n2) n16 | h | n17] [n7: (n2) n18 | l | n19] [n8: (n2) n20 | o | n21 | p | n22] [n9: (n3) n23 | s | n24] [n10: (n3) n25 | v | n26] [n11: (n3) n27 | y | n28]
					|[n12: (null, n4, n13) a b] [n13: (n12, n4, n14) c d] [n14: (n13, n5, n15) e] [n15: (n14, n5, n16) f] [n16: (n15, n6, n17) g] [n17: (n16, n6, n18) h i] [n18: (n17, n7, n19) j k] [n19: (n18, n7, n20) l m] [n20: (n19, n8, n21) n] [n21: (n20, n8, n22) o] [n22: (n21, n8, n23) p q] [n23: (n22, n9, n24) r] [n24: (n23, n9, n25) s t] [n25: (n24, n10, n26) u] [n26: (n25, n10, n27) v] [n27: (n26, n11, n28) w x] [n28: (n27, n11, null) y z]""".stripMargin
		tree.prettySearch( 'i' ) shouldBe "n17 null 1"
	}
	
	"ascending insertion (order 4)" in {
		val tree = new BPlusTree[Char, Null]( 4 )
	
		tree.prettyString shouldBe "[n0: (null, null, null)]"
		tree.insert( 'a' )
		tree.insert( 'b' )
		tree.insert( 'c' )
		tree.insert( 'd' )
		tree.insert( 'e' )
		tree.insert( 'f' )
		tree.insert( 'g' )
		tree.insert( 'h' )
		tree.insert( 'i' )
		tree.insert( 'j' )
		tree.insert( 'k' )
		tree.insert( 'l' )
		tree.insert( 'm' )
		tree.insert( 'n' )
		tree.insert( 'o' )
		tree.insert( 'p' )
		tree.wellConstructed shouldBe "true"
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | g | n2 | m | n3]
					|[n1: (n0) n4 | c | n5 | e | n6] [n2: (n0) n7 | i | n8 | k | n9] [n3: (n0) n10 | o | n11]
					|[n4: (null, n1, n5) a b] [n5: (n4, n1, n6) c d] [n6: (n5, n1, n7) e f] [n7: (n6, n2, n8) g h] [n8: (n7, n2, n9) i j] [n9: (n8, n2, n10) k l] [n10: (n9, n3, n11) m n] [n11: (n10, n3, null) o p]""".stripMargin
		tree.prettySearch( 'h' ) shouldBe "n7 null 1"
	}
	
	"descending insertion (order 4)" in {
		val tree = new BPlusTree[Char, Null]( 4 )
	
		tree.insert( 'p', 'o', 'n', 'm', 'l', 'k', 'j', 'i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a' )
		tree.wellConstructed shouldBe "true"
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | i | n2 | m | n3]
					|[n1: (n0) n4 | c | n5 | e | n6 | g | n7] [n2: (n0) n8 | k | n9] [n3: (n0) n10 | o | n11]
					|[n4: (null, n1, n5) a b] [n5: (n4, n1, n6) c d] [n6: (n5, n1, n7) e f] [n7: (n6, n1, n8) g h] [n8: (n7, n2, n9) i j] [n9: (n8, n2, n10) k l] [n10: (n9, n3, n11) m n] [n11: (n10, n3, null) o p]""".stripMargin
		tree.prettySearch( 'h' ) shouldBe "n7 null 1"
	}
	
}