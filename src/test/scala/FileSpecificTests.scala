package xyz.hyperreal.btree

import org.scalatest._
import prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.Assertions._

import util.Random
import collection.mutable.HashSet


class FileSpecificTests extends FreeSpec with PropertyChecks with Matchers {

	"persistence" in {
		val f = newfile
		val t = new FileBPlusTree[Int, Any]( f, 3 )
		
		t.insert( 1, Seq(3, 4, 5) )
		t.close
		
		val t1 = FileBPlusTree[Int, Any]( f )
		
		t1.search(1).get shouldBe Seq(3, 4, 5)
	}
	
	"inserting/deleting/retrieving long strings" in {
		val t = new FileBPlusTree[Int, String]( newfile, 3 )
		val set = new HashSet[String]
		val size = 50
		val len = 500
		
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
		val size = 50
		val len = 500
		
		def map: collection.Map[Int, String] = {
			val s = Random.nextString( len )
			
			if (sset contains s)
				map
			else {
				val res = Map( 1 -> s, 2 -> "two", 3 -> "three" )
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
					mset -= v
					sset -= v(1)
					assert( v(2) == "two" )
					assert( v(3) == "three" )
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
					assert( v(2) == "two" )
					assert( v(3) == "three" )
			}
			
			t.delete( k ) shouldBe true
			t.wellConstructed shouldBe "true"
		}
		
		assert( t.keysIterator.toList == (size + 1 to 2*size) )
		assert( t.valuesIterator.toSet == mset )
	}

	"inserting/deleting/retrieving arrays" in {
		val t = new FileBPlusTree[Int, collection.Seq[String]]( newfile, 3 )
		val sset = new HashSet[String]
		val lset = new HashSet[collection.Seq[String]]
		val size = 50
		val len = 500
		
		def seq: collection.Seq[String] = {
			val s = Random.nextString( len )
			
			if (sset contains s)
				seq
			else {
				val res = Seq( s, "two", "three" )
				sset += s
				lset += res
				res
			}
		}
		
		for (k <- 1 to size) {
			t.insert( k, seq ) shouldBe false
			t.wellConstructed shouldBe "true"
		}
		
		for (k <- 1 to size/2) {
			t.search( k ) match {
				case None => fail
				case Some( v ) =>
					lset -= v
					sset -= v(0)
			}
			
			t.delete( k ) shouldBe true
			t.wellConstructed shouldBe "true"
		}
			
		for (k <- size + 1 to 2*size) {
			t.insert( k, seq ) shouldBe false
			t.wellConstructed shouldBe "true"
		}
		
		for (k <- size/2 + 1 to size) {
			t.search( k ) match {
				case None => fail
				case Some( v ) =>
					lset -= v
					sset -= v(0)
					assert( v(1) == "two" )
					assert( v(2) == "three" )
			}
			
			t.delete( k ) shouldBe true
			t.wellConstructed shouldBe "true"
		}
		
		assert( t.keysIterator.toList == (size + 1 to 2*size) )
		assert( t.valuesIterator.toSet == lset )
	}

}