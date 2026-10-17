package dev.androidisland.domain

import org.junit.Assert.*
import org.junit.Test

class DomainTest {
    @Test fun arithmeticPrecedenceAndRightAssociativePower() {
        assertEquals(14.0, Expression.evaluate("2+3*4").getOrThrow(), 0.0)
        assertEquals(512.0, Expression.evaluate("2^3^2").getOrThrow(), 0.0)
        assertEquals(-4.0, Expression.evaluate("-2^2").getOrThrow(), 0.0)
    }
    @Test fun functionsAndVariableAreExplicit() {
        assertEquals(5.0, Expression.evaluate("sin(pi/2)*5").getOrThrow(), 1e-9)
        assertEquals(3.0, Expression.evaluate("floor(x)", 3.8).getOrThrow(), 0.0)
        assertTrue(Expression.evaluate("x").isFailure)
    }
    @Test fun rejectsUndefinedAndExecutableInput() {
        listOf("1/0", "sqrt(-1)", "Runtime.exec(1)", "1;2", "sin(2", "2hello").forEach {
            assertTrue(it, Expression.evaluate(it).isFailure)
        }
    }
    @Test fun evaluationHasDepthAndLengthLimits() {
        assertTrue(Expression.evaluate("(".repeat(60)+"1"+")".repeat(60)).isFailure)
        assertTrue(Expression.evaluate("1".repeat(513)).isFailure)
    }
    @Test fun screenshotGraphHasTwoBoundedSeries() {
        val series = GraphSampler.sample("y=sin(x*2)*5,y2=floor(x)")
        assertEquals(2, series.size)
        assertEquals(241, series.first().points.size)
        assertEquals(-10.0, series.first().points.first().x, 0.0)
    }
    @Test fun graphPreservesUndefinedSamples() {
        assertNull(GraphSampler.sample("y=1/x").first().points[120].y)
        assertTrue(GraphSampler.sample("y=arbitrary(x)").isEmpty())
    }
    @Test fun conversionsCheckDimensionsAndTemperatureDomain() {
        assertEquals(1.0, UnitConverter.convert("1000 m to km")!!.first, 0.0)
        assertEquals(32.0, UnitConverter.convert("0 c to f")!!.first, 0.0)
        assertNull(UnitConverter.convert("4 kg to km"))
        assertNull(UnitConverter.convert("-1 k to c"))
    }
    @Test fun rankingHonorsAliasesVisibilityAndStableTieBreaks() {
        val entries = listOf(SearchEntry("b", "YouTube", "App", setOf("yt")),
            SearchEntry("a", "YouTube", "App"), SearchEntry("hidden", "yt", "App", hidden = true))
        assertEquals("b", SearchRanker.search(entries, "YT").single().id)
        assertEquals(listOf("a", "b"), SearchRanker.search(entries, "YouTube").map { it.id })
    }
    @Test fun terminalAndStaleAgentEventsAreRejected() {
        val running = AgentTask("1", 2, "Test", AgentStatus.RUNNING, 40)
        assertFalse(running.accepts(running.copy(revision = 1)))
        assertFalse(running.accepts(running.copy(revision = 3, status = AgentStatus.QUEUED)))
        val done = running.copy(revision = 3, status = AgentStatus.COMPLETED, progress = 100)
        assertTrue(running.accepts(done))
        assertFalse(done.accepts(running.copy(revision = 4)))
    }
    @Test fun privacyOverridesExpandedActivityAndClearsMemory() {
        val engine = IslandEngine()
        engine.put(IslandItem("media", ActivityKind.MEDIA, "Music", "Playing"))
        engine.expand("media")
        assertNull(engine.select(0, false))
        assertEquals(0, engine.size)
    }
    @Test fun expiryAndSelectionAreDeterministic() {
        val engine = IslandEngine()
        engine.put(IslandItem("media", ActivityKind.MEDIA, "Music", "Playing"))
        engine.put(IslandItem("charge", ActivityKind.CHARGING, "Charging", "Connected", 5))
        assertEquals("charge", engine.select(4, true)?.id)
        assertEquals("media", engine.select(5, true)?.id)
        engine.remove("media")
        assertNull(engine.select(6, true))
    }
    @Test fun floodRemainsBoundedAndKeepsExpandedSelection() {
        val engine = IslandEngine()
        engine.put(IslandItem("selected", ActivityKind.MEDIA, "Music", "Playing"))
        engine.expand("selected")
        repeat(1000) { engine.put(IslandItem("$it", ActivityKind.AGENT, "Task", "Running")) }
        assertEquals(32, engine.size)
        assertEquals("selected", engine.select(0, true)?.id)
    }
}
