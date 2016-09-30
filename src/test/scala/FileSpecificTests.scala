package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._


class FileSpecificTests extends FreeSpec with PropertyChecks with Matchers {
	
	"inserting maps" in {
		val t = new FileBPlusTree[Int, Any]( newfile, 3 )
		
		t.insert( 1, Map(10 -> "ten", 11 -> "eleven", 12 -> "twelve") )
		t.search( 1 ).toString shouldBe "Some(Map(10 -> ten, 11 -> eleven, 12 -> twelve))"
		t.search( 1 ).get.asInstanceOf[collection.Map[Int, Any]](11) shouldBe "eleven"
	}

}