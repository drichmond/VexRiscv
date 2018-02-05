package vexriscv.plugin.fifo

import vexriscv.{plugin, VexRiscv, VexRiscvConfig}
import vexriscv.plugin.Plugin
import spinal.core._
import spinal.lib._

case class FifoOut() extends Bundle with IMasterSlave {
  val out = Stream(Bits(32 bits))
  override def asMaster(): Unit = {
    master(out)
  }
}

case class FifoIn() extends Bundle with IMasterSlave {
  val in = Stream(Bits(32 bits))
  override def asMaster(): Unit = {
    master(in)
  }
}

/**
  * FifoBypassPlugin - A simple fifo plugin that does not interact
  * with the RISC-V processor
  * 
  */

class FifoBypassPlugin() extends Plugin[VexRiscv] {
  var out : FifoOut = null
  var in : FifoIn = null

  // var in = slave(Stream(Bits(32 bits)))
  // var out = master(Stream(Bits(32 bits)))
  override def setup(pipeline: VexRiscv): Unit = {
      out = master(FifoOut()).setName("M_AXIS_FIFO")
      in = slave(FifoIn()).setName("S_AXIS_FIFO")
  }
  override def build(pipeline: VexRiscv): Unit = {
    in.in >> out.out;
  }

}
