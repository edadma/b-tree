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
			(() => new FileBPlusTree[String, Any]( "btree", 3, true ),    "on disk")
 			)
	
	forAll (order3) { (gen, storage) =>
		val t = gen()
		
		("deletion (leaf merge, 2 level tree): " + storage + ", order 3") in {
			t.build( """
			(
				[a] b [c]
			)
			""" ).prettyString shouldBe
			"""	|[n0: (null, null, null) n1 | b | n2]
					|[n1: (null, n0, n2) a] [n2: (n1, n0, null) c]""".stripMargin

			t.wellConstructed shouldBe "true"
			t.delete( "c" ) shouldBe true
			t.prettyString shouldBe "[n0: (null, null, null) a]"
			t.wellConstructed shouldBe "true"
		}
	}
}