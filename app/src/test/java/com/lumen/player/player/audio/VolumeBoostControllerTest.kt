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
    fun levelIsClampedToOneWhileBoostIsOff() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = false, maxPercent = 200)
        boost.setGain(2f)
        assertEquals(1f, boost.level, 0f)
        assertEquals(1f, boost.linearGain, 0f)
    }

    @Test
    fun levelIsClampedToTheChosenCap() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 150)
        boost.setGain(2f)
        assertEquals(1.5f, boost.level, 0f)
        assertEquals(1.5f, boost.maxLevel, 0f)
        assertEquals(3.375f, boost.linearGain, 1e-5f)
    }

    @Test
    fun loweringTheCapLowersTheCurrentLevel() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 200)
        boost.setGain(2f)
        boost.applySettings(enabled = true, maxPercent = 120)
        assertEquals(1.2f, boost.level, 1e-6f)
    }

    @Test
    fun disablingTheBoostResetsLevelToOne() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 200)
        boost.setGain(1.8f)
        boost.applySettings(enabled = false, maxPercent = 200)
        assertEquals(1f, boost.level, 0f)
        assertEquals(1f, boost.linearGain, 0f)
    }

    @Test
    fun capIsKeptWithinOneHundredToTwoHundredPercent() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 300)
        assertEquals(2f, boost.maxBoost, 0f)
        boost.applySettings(enabled = true, maxPercent = 50)
        assertEquals(1f, boost.maxBoost, 0f)
    }

    @Test
    fun gainFollowsVlcCubicCurveAboveOneHundredPercent() {
        assertEquals(1f, VolumeBoostController.levelToGain(0.5f), 0f)
        assertEquals(1f, VolumeBoostController.levelToGain(1f), 0f)
        assertEquals(3.375f, VolumeBoostController.levelToGain(1.5f), 1e-5f)
        assertEquals(8f, VolumeBoostController.levelToGain(2f), 1e-5f)
    }

    @Test
    fun softwareGainIsTheFullGainWhenNoDeviceEffectIsAttached() {
        val boost = VolumeBoostController()
        boost.applySettings(enabled = true, maxPercent = 200)
        boost.setGain(1.5f)
        assertEquals(3.375f, boost.softwareGain, 1e-5f)
    }
}
