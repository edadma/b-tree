package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.Assertions._

import util.Random
import collection.mutable.HashSet


class FileSpecificTests extends FreeSpec with PropertyChecks with Matchers {
	
	"inserting maps" in {
		val t = new FileBPlusTree[Int, Any]( newfile, 3 )
		
		t.insert( 1, Map(10 -> "ten", 11 -> "eleven", 12 -> "twelve") )
		t.search( 1 ).toString shouldBe "Some(Map(10 -> ten, 11 -> eleven, 12 -> twelve))"
		t.search( 1 ).get.asInstanceOf[collection.Map[Int, Any]](11) shouldBe "eleven"
	}

	"inserting/deleting/retrieving long strings" in {
		val t = new FileBPlusTree[Int, String]( newfile, 3 )
		val set = new HashSet[String]
		val size = 100
		val len = 1000
		
		def string: String = {
			val s = Random.nextString( len )
			
			if (set contains s)
				string
			else {
				set += s
				s
			}
		}
			
		for (k <- 1 to size) {
			t.insert( k, string ) shouldBe false
			t.wellConstructed shouldBe "true"
		}
		
		for (k <- 1 to size/2) {
			t.search( k ) match {
				case None => fail
				case Some( v ) => set -= v
			}
			
			t.delete( k ) shouldBe true
			t.wellConstructed shouldBe "true"
		}
			
		for (k <- size + 1 to 2*size) {
			t.insert( k, string ) shouldBe false
			t.wellConstructed shouldBe "true"
		}
		
		for (k <- size/2 + 1 to size) {
			t.search( k ) match {
				case None => fail
				case Some( v ) => set -= v
			}
			
			t.delete( k ) shouldBe true
			t.wellConstructed shouldBe "true"
		}
		
		assert( t.keysIterator.toList == (size + 1 to 2*size) )
		assert( t.valuesIterator.toSet == set )
	}

	"inserting/deleting/retrieving maps" in {
		val t = new FileBPlusTree[Int, collection.Map[Int, String]]( newfile, 3 )
		val sset = new HashSet[String]
		val mset = new HashSet[collection.Map[Int, String]]
		val size = 100
		val len = 1000
		
		def map: collection.Map[Int, String] = {
			val s = Random.nextString( len )
			
			if (sset contains s)
				map
			else {
				val res = Map( 1 -> s )
				sset += s
				mset += res
				res
			}
		}
			
		for (k <- 1 to size) {
			t.insert( k, map ) shouldBe false
			t.wellConstructed shouldBe "true"
		}
		
		for (k <- 1 to size/2) {
			t.search( k ) match {
				case None => fail
				case Some( v ) =>
					mset -= v.toMap
					sset -= v(1)
			}
			
			t.delete( k ) shouldBe true
			t.wellConstructed shouldBe "true"
		}
			
		for (k <- size + 1 to 2*size) {
			t.insert( k, map ) shouldBe false
			t.wellConstructed shouldBe "true"
		}
		
		for (k <- size/2 + 1 to size) {
			t.search( k ) match {
				case None => fail
				case Some( v ) =>
					mset -= v
					sset -= v(1)
			}
			
			t.delete( k ) shouldBe true
			t.wellConstructed shouldBe "true"
		}
		
		assert( t.keysIterator.toList == (size + 1 to 2*size) )
		assert( t.valuesIterator.toSet == mset )
	}

}