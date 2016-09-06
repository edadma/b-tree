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
		
			t.iterator shouldBe "empty iterator"
			t.insertKeys( "v", "t", "u", "j", "g", "w", "y", "c", "n", "l", "a", "r", "b", "s", "e", "f", "i", "z", "h", "d", "p", "x", "m", "k", "o", "q" )
			t.iterator.map( {case (k, _) => k} ).mkString shouldBe "abcdefghijklmnopqrstuvwxyz"
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