package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._


class IteratorTests extends FreeSpec with PropertyChecks with Matchers {
	
	val treeImplementations =
		Table(
			("object generator", 													"storage", 		"order"),
			//----------------														 -------			 -----
			(() => new MemoryBPlusTree[String, Any]( 3 ), "in memory", 	3),
			(() => new MemoryBPlusTree[String, Any]( 4 ), "in memory", 	4),
			(() => new MemoryBPlusTree[String, Any]( 5 ), "in memory", 	5),
			(() => new MemoryBPlusTree[String, Any]( 6 ), "in memory", 	6),
			(() => new FileBPlusTree( "btree", 3, true ), "on disk", 		3),
			(() => new FileBPlusTree( "btree", 4, true ), "on disk", 		4),
			(() => new FileBPlusTree( "btree", 5, true ), "on disk", 		5),
			(() => new FileBPlusTree( "btree", 6, true ), "on disk", 		6)
 			)
	
	forAll (treeImplementations) { (gen, storage, order) =>
		val t = gen()
		
		("iterator: " + storage + ", order " + order) in {
		
			t.iterator.isEmpty shouldBe true
			t.boundedIterator( ('>=, "a") ).isEmpty shouldBe true
			t.boundedIterator( ('>=, "a"), ('<=, "z") ).isEmpty shouldBe true
			t.boundedIterator( ('<=, "z") ).isEmpty shouldBe true
			t.insertKeys( "v", "t", "u", "j", "g", "w", "y", "c", "n", "a", "r", "b", "s", "e", "f", "i", "z", "d", "p", "x", "m", "k", "o", "q" )
			t.wellConstructed shouldBe "true"
			t.iteratorOverKeys.mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('>=, "a") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('>, "a") ).mkString shouldBe "bcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('>=, "A") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('>, "A") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('<=, "z") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('<, "z") ).mkString shouldBe "abcdefgijkmnopqrstuvwxy"
			t.boundedIteratorOverKeys( ('<=, "{") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('<, "{") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedIteratorOverKeys( ('>=, "a"), ('<=, "d") ).mkString shouldBe "abcd"
			t.boundedIteratorOverKeys( ('>, "a"), ('<=, "d") ).mkString shouldBe "bcd"
			t.boundedIteratorOverKeys( ('>=, "a"), ('<, "d") ).mkString shouldBe "abc"
			t.boundedIteratorOverKeys( ('>, "a"), ('<, "d") ).mkString shouldBe "bc"
			t.boundedIteratorOverKeys( ('<, "d"), ('>, "a") ).mkString shouldBe "bc"
			t.boundedIteratorOverKeys( ('>, "a"), ('<, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "a"), ('<, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "a"), ('<=, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "a"), ('<=, "a") ).mkString shouldBe "a"
			t.boundedIteratorOverKeys( ('>=, "c"), ('<=, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "c"), ('<=, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "c"), ('<, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "c"), ('<, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "h"), ('<, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "h"), ('<, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "h"), ('<=, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "h"), ('<=, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "h"), ('<=, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "h"), ('<=, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "h"), ('<, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "h"), ('<, "a") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "k"), ('<=, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "k"), ('<=, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "k"), ('<, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>, "k"), ('<, "h") ).mkString shouldBe ""
			t.boundedIteratorOverKeys( ('>=, "h"), ('<=, "l") ).mkString shouldBe "ijk"
			t.boundedIteratorOverKeys( ('>, "h"), ('<=, "l") ).mkString shouldBe "ijk"
			t.boundedIteratorOverKeys( ('>=, "h"), ('<, "l") ).mkString shouldBe "ijk"
			t.boundedIteratorOverKeys( ('>, "h"), ('<, "l") ).mkString shouldBe "ijk"
		}
	}
	
// 	forAll (treeImplementations) { (gen, storage, order) =>
// 		val t = gen()
// 		
// 		("iterator1: " + storage + ", order " + order) in {
// 		
// 			t.insert( "a" )
// 			t.prettyString shouldBe "[n0: (null, null, null) a]"
// 		}
// 	}
}