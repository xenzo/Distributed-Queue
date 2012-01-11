package com.tmax.probus.nio

import java.nio.channels.{SelectableChannel, Selector}
import collection.JavaConversions
import collection.immutable.List

/**
 * Base selector
 */
class DqBaseSelector extends Runnable {
    private[this] val fSelector: Selector = Selector.open()

    def register(aChannel: SelectableChannel, aOp: Int, aBody: () => Unit) {
        val callback = {() => {aBody}}
        aChannel.register(fSelector, aOp, callback)
    }

    def executeCallbacks(aCallbacks: List[() => Unit]) {
        aCallbacks.foreach {_()}
    }

    def selectOne(aTimeout: Long) {
        fSelector.select(aTimeout)
        val keySet = fSelector.selectedKeys
        val keys = JavaConversions.asScalaSet(keySet).toList
        fSelector.selectedKeys.clear
        keys.foreach {_.interestOps(0)}
        val callbacks: List[() => Unit] = keys map (_.attachment.asInstanceOf[() => Unit])
        executeCallbacks(callbacks)
    }

    def selectLoop(aIsContinueProcessing: => Boolean) {
        while (aIsContinueProcessing)
            selectOne(0)
    }

    override def run() {
        selectLoop(true)
    }
}
