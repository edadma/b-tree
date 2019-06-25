package xyz.hyperreal

import java.io.File

import org.scalatest.Tag

package object btree {

	def newfile = {
		val f = File.createTempFile( "testfile", ".btree" )

		f.deleteOnExit
		f.getPath
	}

	object BasicTest extends Tag("basic")
	object SlowTest extends Tag("slow")

}
