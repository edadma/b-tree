import xyz.hyperreal.btree.{MemoryBPlusTree, FileBPlusTree}


object Example1 extends App {
	val memoryTree = new MemoryBPlusTree[String, Any]( 5 )
	
	memoryTree.insertKeys( "k", "z", "p", "d", "b", "v", "h", "x", "o", "y", "c", "t", "j", "n", "f", "l", "s", "q", "i", "m", "e", "u", "w", "a", "g", "r" )

	val fileTree = new FileBPlusTree[String, Any]( "btree", 5 )
	
	fileTree.insertKeys( "k", "z", "p", "d", "b", "v", "h", "x", "o", "y", "c", "t", "j", "n", "f", "l", "s", "q", "i", "m", "e", "u", "w", "a", "g", "r" )
	fileTree.diagram( "fileTree" )
	fileTree.close
	
	println( memoryTree.boundedKeysIterator(('>, "c"), ('<, "l")).mkString(", ") )
	println( memoryTree.reverseKeysIterator.mkString(", ") )
}