package xyz.hyperreal.btree

import util.Random

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._


class StressTests extends FreeSpec with PropertyChecks with Matchers {

	val shorter =
		Table(
			("object generator", 													"storage",		"order",   "size"),
			//----------------                               -------     -----      ----
			(() => new MemoryBPlusTree[Int, Any]( 3 ),    "in memory", 3, 1000),
			(() => new FileBPlusTree[Int, Any]( newfile, 3 ),    "on disk", 3, 50),
			(() => new MemoryBPlusTree[Int, Any]( 4 ),    "in memory", 4, 1000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 4 ),    "on disk", 4, 1000),
 			(() => new MemoryBPlusTree[Int, Any]( 5 ),    "in memory", 5, 2000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 5 ),    "on disk", 5, 2000),
			(() => new MemoryBPlusTree[Int, Any]( 6 ),    "in memory", 6, 2000)
//			(() => new FileBPlusTree[Int, Any]( newfile, 6 ),    "on disk", 6, 2000)
			)

	val tests =
		Table(
			("object generator", 													"storage",		"order",   "size"),
			//----------------                               -------     -----      ----
			(() => new MemoryBPlusTree[Int, Any]( 3 ),    "in memory", 3, 1000),
			(() => new FileBPlusTree[Int, Any]( newfile, 3 ),    "on disk", 3, 1000),
			(() => new MemoryBPlusTree[Int, Any]( 4 ),    "in memory", 4, 1000),
			(() => new FileBPlusTree[Int, Any]( newfile, 4 ),    "on disk", 4, 1000),
			(() => new MemoryBPlusTree[Int, Any]( 5 ),    "in memory", 5, 2000),
			(() => new FileBPlusTree[Int, Any]( newfile, 5 ),    "on disk", 5, 2000),
			(() => new MemoryBPlusTree[Int, Any]( 6 ),    "in memory", 6, 2000),
			(() => new FileBPlusTree[Int, Any]( newfile, 6 ),    "on disk", 6, 2000),
			(() => new MemoryBPlusTree[Int, Any]( 49 ),    "in memory", 49, 10000),
			(() => new FileBPlusTree[Int, Any]( newfile, 49 ),    "on disk", 49, 10000),
			(() => new MemoryBPlusTree[Int, Any]( 50 ),    "in memory", 50, 10000),
			(() => new FileBPlusTree[Int, Any]( newfile, 50 ),    "on disk", 50, 10000),
			(() => new MemoryBPlusTree[Int, Any]( 99 ),    "in memory", 99, 20000),
			(() => new FileBPlusTree[Int, Any]( newfile, 99 ),    "on disk", 99, 20000),
			(() => new MemoryBPlusTree[Int, Any]( 100 ),    "in memory", 100, 20000),
			(() => new FileBPlusTree[Int, Any]( newfile, 100 ),    "on disk", 100, 20000)
 			)
	
	forAll (shorter) { (gen, storage, order, size) =>
		val t = gen()
		
		("random insertion/deletion: " + storage + ", " + order + ", " + size) in {
			t.insertKeysAndCheck( Random.shuffle(1 to size): _* ) shouldBe "true"
		
			for (k <- Random.shuffle( 1 to size )) {
				t.delete( k ) shouldBe true
				t.wellConstructed shouldBe "true"
			}
			
			t.isEmpty shouldBe true
			t.iterator.isEmpty shouldBe true
		}
	}
		
	forAll (shorter) { (gen, storage, order, size) =>
		val t = gen()
		
		("ascending insertion/deletion: " + storage + ", " + order + ", " + size) in {
			t.insertKeysAndCheck( (1 to size): _* ) shouldBe "true"
		
			for (k <- Random.shuffle( 1 to size )) {
				t.delete( k ) shouldBe true
				t.wellConstructed shouldBe "true"
			}
			
			t.isEmpty shouldBe true
			t.iterator.isEmpty shouldBe true
		}
	}
	
	forAll (shorter) { (gen, storage, order, size) =>
		val t = gen()
		
		("random insertion/deletion twice: " + storage + ", " + order + ", " + size) in {
			t.insertKeysAndCheck( Random.shuffle(1 to size): _* ) shouldBe "true"
		
			for (k <- 1 to size/2) {
				t.delete( k ) shouldBe true
				t.wellConstructed shouldBe "true"
			}
			
			t.insertKeysAndCheck( Random.shuffle(size + 1 to 2*size): _* ) shouldBe "true"
		
			for (k <- size/2 + 1 to size) {
				t.delete( k ) shouldBe true
				t.wellConstructed shouldBe "true"
			}
		
			t.keysIterator.toList == (size + 1 to 2*size) shouldBe true
		}
	}
	
}