package com.lumen.player.player.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeBoostControllerTest {

    @Test
    fun boostOffMeansMaxLevelIsOneHundredPercent() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = false, maxPercent = 200)
        assertEquals(1f, boost.maxLevel, 0f)
    }

    @Test
    fun gainIsClampedToOneWhileBoostIsOff() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = false, maxPercent = 200)
        boost.setGain(2f)
        assertEquals(1f, boost.gain, 0f)
    }

    @Test
    fun gainIsClampedToTheChosenCap() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 150)
        boost.setGain(2f)
        assertEquals(1.5f, boost.gain, 0f)
        assertEquals(1.5f, boost.maxLevel, 0f)
    }

    @Test
    fun loweringTheCapLowersTheCurrentGain() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 200)
        boost.setGain(2f)
        boost.applySettings(enabled = true, maxPercent = 120)
        assertEquals(1.2f, boost.gain, 1e-6f)
    }

    @Test
    fun disablingTheBoostResetsGainToOne() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 200)
        boost.setGain(1.8f)
        boost.applySettings(enabled = false, maxPercent = 200)
        assertEquals(1f, boost.gain, 0f)
    }

    @Test
    fun capIsKeptWithinOneHundredToTwoHundredPercent() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 300)
        assertEquals(2f, boost.maxBoost, 0f)
        boost.applySettings(enabled = true, maxPercent = 50)
        assertEquals(1f, boost.maxBoost, 0f)
    }
}
