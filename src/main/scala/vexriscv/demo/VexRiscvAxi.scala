package vexriscv.demo

import vexriscv.plugin._
import vexriscv.{VexRiscv, plugin, VexRiscvConfig}
import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb.Apb3
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.avalon.AvalonMM
import spinal.lib.eda.altera.{ResetEmitterTag, InterruptReceiverTag, QSysify}

/**
 * Created by spinalvm on 14.07.17.
 */


object VexRiscvAxi{
  def main(args: Array[String]) {
    val report = SpinalVerilog{

      //CPU configuration
      val cpuConfig = VexRiscvConfig(
        plugins = List(
          new PcManagerSimplePlugin(
            resetVector = 0x00000000l,
            relaxedPcCalculation = false
          ),
          new DBusSimplePlugin(
            catchAddressMisaligned = false,
            catchAccessFault = false
          ),
          new IBusSimplePlugin(
            interfaceKeepData = false,
            catchAccessFault = false
          ),
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
            executeInsertion = true
          ),
          new FullBarrielShifterPlugin,
          new MulPlugin,
          new DivPlugin,
          new HazardSimplePlugin(
            bypassExecute           = true,
            bypassMemory            = true,
            bypassWriteBack         = true,
            bypassWriteBackBuffer   = true,
            pessimisticUseSrc       = false,
            pessimisticWriteRegFile = false,
            pessimisticAddressMatch = false
          ),
          new BranchPlugin(
            earlyBranch = false,
            catchAddressMisaligned = true,
            prediction = STATIC
          ),
          new CsrPlugin(
            CsrPluginConfig.all
          ),
          new YamlPlugin("cpu0.yaml")
        )
      )

      val cpu = new VexRiscv(cpuConfig)
      cpu.rework {
        var iBus : Axi4ReadOnly = null
        var dBus : Axi4 = null
        for (plugin <- cpuConfig.plugins) plugin match {
          case plugin: IBusSimplePlugin => {
            plugin.iBus.asDirectionLess() //Unset IO properties of iBus
            iBus = master(plugin.iBus.toAxi4ReadOnly())
              .setName("iBusAxi")
              .addTag(ClockDomainTag(ClockDomain.current)) //Specify a clock domain to the iBus (used by QSysify)
          }
          case plugin: IBusCachedPlugin => {
            plugin.iBus.asDirectionLess() //Unset IO properties of iBus
            iBus = master(plugin.iBus.toAxi4ReadOnly())
              .setName("iBusAxi")
              .addTag(ClockDomainTag(ClockDomain.current)) //Specify a clock domain to the iBus (used by QSysify)
          }
          case plugin: DBusSimplePlugin => {
            plugin.dBus.asDirectionLess()
            master(plugin.dBus.toAxi4())
              .setName("dBusAxi")
              .addTag(ClockDomainTag(ClockDomain.current))
          }
          case plugin: DBusCachedPlugin => {
            plugin.dBus.asDirectionLess()
            master(plugin.dBus.toAxi4())
              .setName("dBusAxi")
              .addTag(ClockDomainTag(ClockDomain.current))
          }
          case plugin: DebugPlugin => {
            plugin.io.bus.asDirectionLess()
            slave(plugin.io.bus.fromAvalon())
              .setName("debugBusAvalon")
              .addTag(ClockDomainTag(plugin.debugClockDomain))
              .parent = null  //Avoid the io bundle to be interpreted as a QSys conduit
            plugin.io.resetOut
              .addTag(ResetEmitterTag(plugin.debugClockDomain))
              .parent = null //Avoid the io bundle to be interpreted as a QSys conduit
          }
          case _ =>
        }
      }
      cpu
    }
  }
}
