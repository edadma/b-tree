package xyz.hyperreal

import java.io.File


package object btree {
	def newfile = {
		val f = File.createTempFile( "testfile", ".btree" )
		
		f.deleteOnExit
		f.getPath
	}
}