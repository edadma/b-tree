package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers {
	
	"ascending order insertion" in {
		val tree = new BPlusTree[Char, Int]( 3 )
	
		tree.prettyString shouldBe "[n0: (null)]"
		tree.insert( 'a', 1 )
		tree.prettyStringWithValues shouldBe "[n0: (null) <a, 1>]"
		tree.insert( 'b', 2 )
		tree.prettyStringWithValues shouldBe "[n0: (null) <a, 1> <b, 2>]"
		tree.insert( 'c', 3 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | b | n2]
					|[n1: (n0) <a, 1>] [n2: (n0) <b, 2> <c, 3>]""".stripMargin
		tree.insert( 'd', 4 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | b | n2 | c | n3]
					|[n1: (n0) <a, 1>] [n2: (n0) <b, 2>] [n3: (n0) <c, 3> <d, 4>]""".stripMargin
		tree.insert( 'e', 5 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | c | n2]
					|[n1: (n0) n3 | b | n4] [n2: (n0) n5 | d | n6]
					|[n3: (n1) <a, 1>] [n4: (n1) <b, 2>] [n5: (n2) <c, 3>] [n6: (n2) <d, 4> <e, 5>]""".stripMargin
		tree.insert( 'f', 6 )
		tree.prettyStringWithValues shouldBe
			"""	|[n0: (null) n1 | c | n2]
					|[n1: (n0) n3 | b | n4] [n2: (n0) n5 | d | n6 | e | n7]
					|[n3: (n1) <a, 1>] [n4: (n1) <b, 2>] [n5: (n2) <c, 3>] [n6: (n2) <d, 4>] [n7: (n2) <e, 5> <f, 6>]""".stripMargin

//		a [RuntimeException] should be thrownBy {interpret( """ (= 1 1] """ )}
//		interpret( """ (cdr '(a)) """ ) shouldBe SNil
	}
	
	"descending order insertion" in {
		val tree = new BPlusTree[Char, Null]( 3, ('g', null), ('f', null) )
	
		tree.prettyString shouldBe "[n0: (null) f g]"
		tree.insert( 'e' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | f | n2]
					|[n1: (n0) e] [n2: (n0) f g]""".stripMargin
		tree.insert( 'd' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | f | n2]
					|[n1: (n0) d e] [n2: (n0) f g]""".stripMargin
		tree.insert( 'c' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | d | n2 | f | n3]
					|[n1: (n0) c] [n2: (n0) d e] [n3: (n0) f g]""".stripMargin
		tree.insert( 'b' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | d | n2 | f | n3]
					|[n1: (n0) b c] [n2: (n0) d e] [n3: (n0) f g]""".stripMargin
		tree.insert( 'a' )
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | d | n2]
					|[n1: (n0) n3 | b | n4] [n2: (n0) n5 | f | n6]
					|[n3: (n1) a] [n4: (n1) b c] [n5: (n2) d e] [n6: (n2) f g]""".stripMargin
	}
	
	"random insertion" in	{
		val tree = new BPlusTree[Char, Null]( 3 )
	
		for (k <- Vector( 'v', 't', 'u', 'j', 'g', 'w', 'y', 'c', 'n', 'l', 'a', 'r', 'b', 's', 'e', 'f', 'i', 'z', 'h', 'd', 'p', 'x', 'm', 'k', 'o', 'q' ))
			tree.insert( k )
			
		tree.prettyString shouldBe
			"""	|[n0: (null) n1 | g | n2 | r | n3]
					|[n1: (n0) n4 | e | n5] [n2: (n0) n6 | j | n7 | n | n8] [n3: (n0) n9 | u | n10 | w | n11]
					|[n4: (n1) n12 | c | n13] [n5: (n1) n14 | f | n15] [n6: (n2) n16 | h | n17] [n7: (n2) n18 | l | n19] [n8: (n2) n20 | o | n21 | p | n22] [n9: (n3) n23 | s | n24] [n10: (n3) n25 | v | n26] [n11: (n3) n27 | y | n28]
					|[n12: (n4) a b] [n13: (n4) c d] [n14: (n5) e] [n15: (n5) f] [n16: (n6) g] [n17: (n6) h i] [n18: (n7) j k] [n19: (n7) l m] [n20: (n8) n] [n21: (n8) o] [n22: (n8) p q] [n23: (n9) r] [n24: (n9) s t] [n25: (n10) u] [n26: (n10) v] [n27: (n11) w x] [n28: (n11) y z]""".stripMargin
	}
}