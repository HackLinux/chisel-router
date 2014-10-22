package router

import Chisel._
import utils.{Fifo, DecoupledFifoIO}

class InputPort(n: Int) extends Module {
  val io = new Bundle {
    val fifo = new DecoupledFifoIO(PacketData())

    val direction = UInt(INPUT, width = 5)
    val xDest     = UInt(OUTPUT, width = 4)
    val yDest     = UInt(OUTPUT, width = 4)
  }

  val queue = Module(new Fifo(PacketData(), n))
  io.fifo <> queue.io

  io.xDest := io.fifo.out.bits.xDest
  io.yDest := io.fifo.out.bits.yDest
}

class InputPortTest(p: InputPort) extends Tester(p) {

  def testFifoIntegration() {
    poke(p.io.fifo.in.valid, 1)
    poke(p.io.fifo.in.bits, 10)
    step(1)
    expect(p.io.fifo.out.bits, 10)
  }

  def testInputPortRequest() {
    poke(p.io.direction, South.value.litValue())
    poke(p.io.fifo.out.ready, 0)

    // Set signals to start reading
    poke(p.io.fifo.in.valid, 0)
    poke(p.io.fifo.out.ready, 1)

    step(1)
    expect(p.io.fifo.out.valid, 0)
  }

  def testXYPacketDestOfInputPackage() {
    poke(p.io.fifo.in.valid, 1)
    poke(p.io.fifo.out.ready, 0)

    val packet = PacketData.create(yDest = 9, xDest = 15, ySender = 5, xSender = 3)
    poke(p.io.fifo.in.bits, packet.litValue())
    step(1)
    expect(p.io.yDest, 9)
    expect(p.io.xDest, 15)
  }

  testFifoIntegration()
  testInputPortRequest()
  testXYPacketDestOfInputPackage()
}
