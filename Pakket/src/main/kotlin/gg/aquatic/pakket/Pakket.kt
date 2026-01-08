package gg.aquatic.pakket

import gg.aquatic.pakket.nms_1_21_9.NMSHandlerImpl

object Pakket {

    val handler by lazy {
        when (NMSVersion.ofAquatic()) {
            NMSVersion.V_1_21_9 -> NMSHandlerImpl
            else -> throw IllegalStateException("Unsupported server version.")
        }
    }

}