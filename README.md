b-tree
======

*b-tree* is a B+ Tree implementation in Scala. *b-tree* is designed to be both generic (type parameters for keys and values, and an abstract type for node references) and general (doesn't care how the tree is stored). An extending class needs to implement a number of simple methods and node type that provide storage abstraction.

Scaladoc library documentation can be found at http://edadma.github.io/b-tree.

The following code

    import xyz.hyperreal.btree.{MemoryBPlusTree, FileBPlusTree}


    object Example1 extends App {
      val memoryTree = new MemoryBPlusTree[String, Any]( 5 )
      
      memoryTree.insertKeys( "k", "z", "p", "d", "b", "v", "h", "x", "o", "y", "c", "t", "j", "n", "f", "l", "s", "q", "i", "m", "e", "u", "w", "a", "g", "r" )
      memoryTree.diagram( "memoryTree" )
      
      val fileTree = new FileBPlusTree( "btree", 5, true )
      
      fileTree.insertKeys( "k", "z", "p", "d", "b", "v", "h", "x", "o", "y", "c", "t", "j", "n", "f", "l", "s", "q", "i", "m", "e", "u", "w", "a", "g", "r" )
      fileTree.diagram( "fileTree" )
    }
	
produces identical B+ Tree structures in memory and on disk. The tree that is produced is

![tree](tree1.png)