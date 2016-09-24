package xyz.hyperreal.btree

import util.Random

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._


class StressTests extends FreeSpec with PropertyChecks with Matchers {

	val tests =
		Table(
			("object generator", 													"storage",		"order",   "size"),
			//----------------                               -------     -----      ----
			(() => new MemoryBPlusTree[Int, Any]( 3 ),    "in memory", 3, 1000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 3, true ),    "on disk", 3, 1000),
			(() => new MemoryBPlusTree[Int, Any]( 4 ),    "in memory", 4, 1000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 4, true ),    "on disk", 4, 1000),
			(() => new MemoryBPlusTree[Int, Any]( 5 ),    "in memory", 5, 2000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 5, true ),    "on disk", 5, 2000),
			(() => new MemoryBPlusTree[Int, Any]( 6 ),    "in memory", 6, 2000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 6, true ),    "on disk", 6, 2000),
			(() => new MemoryBPlusTree[Int, Any]( 49 ),    "in memory", 49, 10000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 49, true ),    "on disk", 49, 10000),
			(() => new MemoryBPlusTree[Int, Any]( 50 ),    "in memory", 50, 10000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 50, true ),    "on disk", 50, 10000),
			(() => new MemoryBPlusTree[Int, Any]( 99 ),    "in memory", 99, 20000),
//			(() => new FileBPlusTree[Int, Any]( newfile, 99, true ),    "on disk", 99, 20000),
			(() => new MemoryBPlusTree[Int, Any]( 100 ),    "in memory", 100, 20000)
//			(() => new FileBPlusTree[Int, Any]( newfile, 100, true ),    "on disk", 100, 20000)
 			)
	
	forAll (tests) { (gen, storage, order, size) =>
		val t = gen()
		
		(storage + ", " + order + ", " + size) in {
			t.insertKeysAndCheck( Random.shuffle(1 to 20000): _* ) shouldBe "true"
		
			for (k <- Random.shuffle( 1 to 20000 ))
				t.delete( k ) shouldBe true
				
			t.wellConstructed shouldBe "true"
		}
	}
		
}