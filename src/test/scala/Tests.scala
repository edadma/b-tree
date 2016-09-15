package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._


class Tests extends FreeSpec with PropertyChecks with Matchers {
	
	val treeImplementations =
		Table(
			("object generator", 													"storage", 		"order"),
			//----------------                               -------	       -----
			(() => new MemoryBPlusTree[String, Any]( 3 ),    "in memory",  3),
			(() => new MemoryBPlusTree[String, Any]( 4 ),    "in memory", 	4),
			(() => new MemoryBPlusTree[String, Any]( 5 ),    "in memory", 	5),
			(() => new MemoryBPlusTree[String, Any]( 6 ),    "in memory", 	6),
			(() => new FileBPlusTree( "btree", 3, true ),    "on disk", 		3),
			(() => new FileBPlusTree( "btree", 4, true ),    "on disk", 		4),
			(() => new FileBPlusTree( "btree", 5, true ),    "on disk", 		5),
			(() => new FileBPlusTree( "btree", 6, true ),    "on disk", 		6)
 			)
	
	forAll (treeImplementations) { (gen, storage, order) =>
		val t = gen()
		
		("iterator: " + storage + ", order " + order) in {
		
			t.iterator.isEmpty shouldBe true
			t.boundedIterator( ('>=, "a") ).isEmpty shouldBe true
			t.boundedIterator( ('>=, "a"), ('<=, "z") ).isEmpty shouldBe true
			t.boundedIterator( ('<=, "z") ).isEmpty shouldBe true
			t.insertKeysAndCheck( "v", "t", "u", "j", "g", "w", "y", "c", "n", "a", "r", "b", "s", "e", "f", "i", "z", "d", "p", "x", "m", "k", "o", "q" ) shouldBe "true"
			t.keysIterator.mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('>=, "a") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('>, "a") ).mkString shouldBe "bcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('>=, "A") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('>, "A") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('<=, "z") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('<, "z") ).mkString shouldBe "abcdefgijkmnopqrstuvwxy"
			t.boundedKeysIterator( ('<=, "{") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('<, "{") ).mkString shouldBe "abcdefgijkmnopqrstuvwxyz"
			t.boundedKeysIterator( ('>=, "a"), ('<=, "d") ).mkString shouldBe "abcd"
			t.boundedKeysIterator( ('>, "a"), ('<=, "d") ).mkString shouldBe "bcd"
			t.boundedKeysIterator( ('>=, "a"), ('<, "d") ).mkString shouldBe "abc"
			t.boundedKeysIterator( ('>, "a"), ('<, "d") ).mkString shouldBe "bc"
			t.boundedKeysIterator( ('<, "d"), ('>, "a") ).mkString shouldBe "bc"
			t.boundedKeysIterator( ('>, "a"), ('<, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "a"), ('<, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "a"), ('<=, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "a"), ('<=, "a") ).mkString shouldBe "a"
			t.boundedKeysIterator( ('>=, "c"), ('<=, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "c"), ('<=, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "c"), ('<, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "c"), ('<, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "h"), ('<, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "h"), ('<, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "h"), ('<=, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "h"), ('<=, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "h"), ('<=, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "h"), ('<=, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "h"), ('<, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "h"), ('<, "a") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "k"), ('<=, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "k"), ('<=, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "k"), ('<, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>, "k"), ('<, "h") ).mkString shouldBe ""
			t.boundedKeysIterator( ('>=, "h"), ('<=, "l") ).mkString shouldBe "ijk"
			t.boundedKeysIterator( ('>, "h"), ('<=, "l") ).mkString shouldBe "ijk"
			t.boundedKeysIterator( ('>=, "h"), ('<, "l") ).mkString shouldBe "ijk"
			t.boundedKeysIterator( ('>, "h"), ('<, "l") ).mkString shouldBe "ijk"
		}
	}
	
	forAll (treeImplementations) { (gen, storage, order) =>
		val t = gen()
		
		("bulk loading: " + storage + ", order " + order) in {
			t.load( ("h",8), ("i",9), ("d",4), ("b",2), ("j",10), ("f",6), ("g",7), ("a",1), ("c",3), ("e",5) )
			t.wellConstructed shouldBe "true"
			t.iterator mkString ", " shouldBe "(a,1), (b,2), (c,3), (d,4), (e,5), (f,6), (g,7), (h,8), (i,9), (j,10)"
			t.load( ("p",16), ("r",18), ("l",12), ("k",11), ("o",15), ("s",19), ("q",17), ("t",20), ("m",13), ("n",14) )
			t.wellConstructed shouldBe "true"
			t.iterator mkString ", " shouldBe "(a,1), (b,2), (c,3), (d,4), (e,5), (f,6), (g,7), (h,8), (i,9), (j,10), (k,11), (l,12), (m,13), (n,14), (o,15), (p,16), (q,17), (r,18), (s,19), (t,20)"
			a [RuntimeException] should be thrownBy {t.load( ("A", 0) )}
		}
	}
}