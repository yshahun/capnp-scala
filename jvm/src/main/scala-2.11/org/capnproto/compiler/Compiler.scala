package org.capnproto.compiler

import java.nio.channels.Channels

import org.capnproto.runtime.Serialize
import org.capnproto.compiler.CapnpSchema.CodeGeneratorRequest

object Compiler {
  def main(args: Array[String]): Unit = {
    val chan = Channels.newChannel(System.in)
    val messageReader = Serialize.read(chan)

    val request = messageReader.getRoot(CodeGeneratorRequest)

    for (node <- request.nodes) {
      println(s"Node: ${node.displayname.toString}")
    }
  }
}
