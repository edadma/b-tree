package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.Assertions._


class MiscTests extends FreeSpec with PropertyChecks with Matchers {

	"max/min" in {
		val t = new MemoryBPlusTree[Int, Symbol]( 3 )
		
		t.min shouldBe None
		t.max shouldBe None
		t.minKey shouldBe None
		t.maxKey shouldBe None
		t.insert( 5, 'five )
		t.min shouldBe Some((5, 'five))
		t.max shouldBe Some((5, 'five))
		t.minKey shouldBe Some(5)
		t.maxKey shouldBe Some(5)
		t.insert( 3, 'three )
		t.min shouldBe Some((3, 'three))
		t.max shouldBe Some((5, 'five))
		t.minKey shouldBe Some(3)
		t.maxKey shouldBe Some(5)
	}
	
}