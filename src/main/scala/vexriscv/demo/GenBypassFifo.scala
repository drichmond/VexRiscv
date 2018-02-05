package vexriscv.demo

import vexriscv.plugin._
import vexriscv.{plugin, VexRiscv, VexRiscvConfig}
import vexriscv.plugin.fifo._
import spinal.core._
import spinal.lib._



/**
  * GenBypassFifo - A simple RISC-V processor with an independent FIFO interface
  * 
  * The FIFO input is directly connected to the FIFO output - the
  * RISC-V processor is not involved in FIFO management 
  */
object GenBypassFifo{
  def main(args: Array[String]) {
    val report = SpinalVerilog {

      val config = VexRiscvConfig(
        plugins = List(
          new PcManagerSimplePlugin(
            resetVector = 0x00000000l,
            relaxedPcCalculation = false
          ),
          new FifoBypassPlugin(),
          new IBusSimplePlugin(
            interfaceKeepData = false,
            catchAccessFault = false
          ),
          new DBusSimplePlugin(
            catchAddressMisaligned = false,
            catchAccessFault = false
          ),
          new CsrPlugin(CsrPluginConfig.smallest),
          new DecoderSimplePlugin(
            catchIllegalInstruction = false
          ),
          new RegFilePlugin(
            regFileReadyKind = plugin.SYNC,
            zeroBoot = false
          ),
          new IntAluPlugin,
          new SrcPlugin(
            separatedAddSub = false,
            executeInsertion = false
          ),
          new LightShifterPlugin,
          new HazardSimplePlugin(
            bypassExecute           = false,
            bypassMemory            = false,
            bypassWriteBack         = false,
            bypassWriteBackBuffer   = false,
            pessimisticUseSrc       = false,
            pessimisticWriteRegFile = false,
            pessimisticAddressMatch = false
          ),
          new BranchPlugin(
            earlyBranch = false,
            catchAddressMisaligned = false,
            prediction = NONE
          )
        )
      )
      val cpu = new VexRiscv(config)
      cpu.setDefinitionName("GenBypassFifo")
      cpu
    }
  }
}

